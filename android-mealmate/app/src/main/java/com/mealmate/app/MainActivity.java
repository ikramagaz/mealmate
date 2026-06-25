package com.mealmate.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 32;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.addJavascriptInterface(new MealMateBridge(), "MealMateAndroid");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectAndroidBackupBridge();
            }
        });

        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] result = resultCode == RESULT_OK && data != null && data.getData() != null
                    ? new Uri[]{data.getData()}
                    : null;
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    private void injectAndroidBackupBridge() {
        String script = "(function(){"
                + "if(!window.MealMateAndroid||!window.appStateSnapshot)return;"
                + "window.exportMealMateBackup=function(){try{"
                + "if(window.saveAppStateNow)window.saveAppStateNow();"
                + "var data=JSON.stringify(window.appStateSnapshot(),null,2);"
                + "window.MealMateAndroid.saveBackup(data);"
                + "var status=document.getElementById('backupStatus');"
                + "if(status)status.textContent=(window.tr?window.tr('backupReady'):'Backup saved');"
                + "}catch(e){alert('Backup error');}};"
                + "})();";
        webView.evaluateJavascript(script, null);
    }

    public class MealMateBridge {
        @JavascriptInterface
        public void saveBackup(String json) {
            runOnUiThread(() -> {
                try {
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    String fileName = "MealMate-backup-" + date + ".json";
                    OutputStream stream;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                        values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
                        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                        ContentResolver resolver = getContentResolver();
                        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        if (uri == null) throw new Exception("Could not create file");
                        stream = resolver.openOutputStream(uri);
                    } else {
                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        if (!dir.exists()) dir.mkdirs();
                        stream = new FileOutputStream(new File(dir, fileName));
                    }
                    if (stream == null) throw new Exception("Could not open file");
                    stream.write(json.getBytes(StandardCharsets.UTF_8));
                    stream.close();
                    Toast.makeText(MainActivity.this, "Backup saved in Downloads", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Backup could not be saved", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}

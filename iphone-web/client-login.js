
(function(){
  var SESSION_KEY = 'mealmate_client_session_v1';
  function accountList(){ return Array.isArray(window.MEALMATE_CLIENT_ACCOUNTS) ? window.MEALMATE_CLIENT_ACCOUNTS : []; }
  function findAccount(username, password){
    return accountList().find(function(acc){ return acc.username === username && acc.password === password; });
  }
  function getSession(){
    try { return JSON.parse(localStorage.getItem(SESSION_KEY) || 'null'); } catch(e) { return null; }
  }
  function sessionAccount(){
    var session = getSession();
    if (!session || !session.username) return null;
    return accountList().find(function(acc){ return acc.username === session.username; }) || null;
  }
  function ensureLogout(account){
    if (!account || document.getElementById('clientLogoutBtn')) return;
    var controls = document.querySelector('header .controls');
    if (!controls) return;
    var btn = document.createElement('button');
    btn.id = 'clientLogoutBtn';
    btn.className = 'client-logout-btn';
    btn.type = 'button';
    btn.textContent = (account.name || account.username) + ' - Logout';
    btn.onclick = function(){
      localStorage.removeItem(SESSION_KEY);
      location.reload();
    };
    controls.appendChild(btn);
  }
  function unlock(account){
    document.body.classList.remove('client-locked');
    var gate = document.getElementById('clientLoginGate');
    if (gate) gate.remove();
    ensureLogout(account);
  }
  function renderGate(){
    var account = sessionAccount();
    if (account) { unlock(account); return; }
    document.body.classList.add('client-locked');
    if (document.getElementById('clientLoginGate')) return;
    var gate = document.createElement('div');
    gate.id = 'clientLoginGate';
    gate.className = 'client-login-gate';
    gate.innerHTML = '<form class="client-login-card" autocomplete="on">'+
      '<div class="client-login-mark">🍽</div>'+
      '<h1>MealMate</h1>'+
      '<p>تسجيل الدخول / Connexion / Login<br>استعملي الحساب وكلمة السر التي وصلتك.</p>'+
      '<label class="client-login-field"><span>الحساب / Username</span><input id="clientUsername" autocomplete="username" inputmode="text" required></label>'+
      '<label class="client-login-field"><span>كلمة السر / Password</span><input id="clientPassword" type="password" autocomplete="current-password" required></label>'+
      '<button class="client-login-button" type="submit">دخول / Entrer / Login</button>'+
      '<div class="client-login-error" id="clientLoginError"></div>'+
      '<div class="client-login-note">افتحي الرابط في Safari ثم Share > Add to Home Screen</div>'+
    '</form>';
    document.body.appendChild(gate);
    gate.querySelector('form').addEventListener('submit', function(evt){
      evt.preventDefault();
      var username = document.getElementById('clientUsername').value.trim();
      var password = document.getElementById('clientPassword').value;
      var matched = findAccount(username, password);
      if (!matched) {
        document.getElementById('clientLoginError').textContent = 'الحساب أو كلمة السر غير صحيحة';
        return;
      }
      localStorage.setItem(SESSION_KEY, JSON.stringify({ username: matched.username, at: Date.now() }));
      unlock(matched);
    });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', renderGate);
  else renderGate();
})();

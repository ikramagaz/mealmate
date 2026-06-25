$ErrorActionPreference = "Stop"

$source = Split-Path -Parent $MyInvocation.MyCommand.Path
$desktop = [System.IO.Path]::GetFullPath((Join-Path $env:USERPROFILE "Desktop"))
$target = [System.IO.Path]::GetFullPath((Join-Path $desktop "mealmate-iphone-secure-worker"))

if (-not $target.StartsWith($desktop + [System.IO.Path]::DirectorySeparatorChar)) {
  throw "Unexpected deploy folder: $target"
}

if (Test-Path -LiteralPath $target) {
  Remove-Item -LiteralPath $target -Recurse -Force
}

Copy-Item -LiteralPath $source -Destination $target -Recurse -Force
Set-Location -LiteralPath $target

Write-Host ""
Write-Host "Deploying MealMate from:"
Write-Host $target
Write-Host ""

npx wrangler deploy --config .\wrangler.toml

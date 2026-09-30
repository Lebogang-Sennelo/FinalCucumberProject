param(
    [switch]$Headless
)

$ErrorActionPreference = "Stop"
$securePassword = $null
$exitCode = 1

try {
    $localCredentialPath = Join-Path $PSScriptRoot "local.credentials.json"
    if (Test-Path -LiteralPath $localCredentialPath -PathType Leaf) {
        $localCredentials = Get-Content -LiteralPath $localCredentialPath -Raw | ConvertFrom-Json
        $password = [string]$localCredentials.SITE_PASSWORD
    } else {
        $securePassword = Read-Host "Enter the password for laylayt@gmail.com" -AsSecureString
        $password = (New-Object System.Net.NetworkCredential("", $securePassword)).Password
    }
    if ([string]::IsNullOrWhiteSpace($password)) {
        throw "SITE_PASSWORD is missing or empty in local.credentials.json, or no password was entered."
    }

    $imagePath = Join-Path $env:USERPROFILE "Downloads\DC9EBAC7-2F02-4445-944D-C336462CD73A.JPG"
    if (-not (Test-Path -LiteralPath $imagePath -PathType Leaf)) {
        $imagePath = Read-Host "Enter the full path to your profile picture"
    }
    if (-not (Test-Path -LiteralPath $imagePath -PathType Leaf)) {
        throw "Profile picture file not found: $imagePath"
    }

    $env:SITE_USERNAME = "laylayt@gmail.com"
    $env:SITE_PASSWORD = $password
    $env:PROFILE_PICTURE_PATH = (Resolve-Path -LiteralPath $imagePath).Path

    Push-Location $PSScriptRoot
    try {
        if ($Headless) {
            & mvn --batch-mode --no-transfer-progress "-Dbrowser.headless=true" test
        } else {
            & mvn --batch-mode --no-transfer-progress "-Dbrowser.headless=false" test
        }
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
} finally {
    Remove-Item Env:SITE_USERNAME -ErrorAction SilentlyContinue
    Remove-Item Env:SITE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:PROFILE_PICTURE_PATH -ErrorAction SilentlyContinue
    if ($null -ne $securePassword) {
        $securePassword.Dispose()
    }
}

exit $exitCode

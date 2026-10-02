$ErrorActionPreference = "Stop"

if (-not (Get-Command pre-commit -ErrorAction SilentlyContinue)) {
    if (-not (Get-Command py -ErrorAction SilentlyContinue)) {
        throw "Python is required. Install Python, reopen PowerShell, and rerun this script."
    }

    py -m pip install --user pre-commit
    $scriptsPath = (py -c "import sysconfig; print(sysconfig.get_path('scripts', 'nt_user'))").Trim()
    $env:Path = "$scriptsPath;$env:Path"
}

if (-not (Get-Command pre-commit -ErrorAction SilentlyContinue)) {
    throw "pre-commit was installed but is not on PATH. Add its Python Scripts folder to PATH, reopen PowerShell, and rerun this script."
}

$repositoryRoot = Split-Path -Parent $PSScriptRoot
Push-Location $repositoryRoot
try {
    pre-commit install --install-hooks
}
finally {
    Pop-Location
}

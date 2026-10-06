param([switch]$Test, [switch]$Run, [string]$DataDirectory = 'data/v2', [string]$ReportDirectory = 'reports/v2')
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force build/classes | Out-Null
    $sourceFiles = @(Get-ChildItem -LiteralPath src -Recurse -Filter '*.java' | ForEach-Object FullName)
    if ($Test) { $sourceFiles += @(Get-ChildItem -LiteralPath tests -Recurse -Filter '*.java' | ForEach-Object FullName) }
    & javac --release 17 -encoding UTF-8 -Xlint:all -d build/classes $sourceFiles
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
    if ($Test) { & java -ea -cp build/classes bloodbank.service.SystemTest; if ($LASTEXITCODE -ne 0) { throw 'Tests failed.' } }
    if ($Run) { & java -cp build/classes bloodbank.Main $DataDirectory $ReportDirectory; if ($LASTEXITCODE -ne 0) { throw 'Application failed.' } }
} finally { Pop-Location }

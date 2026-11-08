[CmdletBinding()]
param (
    [string]$SonarHost = "http://localhost:9000",
    [string]$SonarToken = "",
    [switch]$SkipTests = $false
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   OnePilates - SonarQube & JaCoCo Code Analysis Runner   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Configurar JDK 21 automaticamente se necessário (necessário para compatibilidade com Mockito/ByteBuddy)
$jdkCandidates = @(
    "C:\Users\Harison\.jdks\corretto-21.0.6",
    "$env:USERPROFILE\.jdks\corretto-21.0.6",
    "C:\Program Files\Java\jdk-21"
)

$selectedJdk = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javaVer = & "$env:JAVA_HOME\bin\java.exe" -version 2>&1
    if ($javaVer -match 'version "(21[^\"]*)"') {
        $selectedJdk = $env:JAVA_HOME
    }
}

if (-not $selectedJdk) {
    foreach ($candidate in $jdkCandidates) {
        if (Test-Path "$candidate\bin\javac.exe") {
            $selectedJdk = $candidate
            break
        }
    }
}

if ($selectedJdk) {
    $env:JAVA_HOME = $selectedJdk
    $env:PATH = "$selectedJdk\bin;$env:PATH"
    Write-Host "[OK] Utilizando JDK 21 configurado em: $selectedJdk" -ForegroundColor Green
} else {
    Write-Host "[INFO] Utilizando Java configurado no ambiente padrão do sistema." -ForegroundColor Yellow
}

# 2. Verificar conexão com o servidor SonarQube
Write-Host "`n[Verificando SonarQube em $SonarHost]..." -ForegroundColor Cyan
$serverAvailable = $false
try {
    $uri = [System.Uri]$SonarHost
    $tcp = New-Object System.Net.Sockets.TcpClient
    $iar = $tcp.BeginConnect($uri.Host, $uri.Port, $null, $null)
    $success = $iar.AsyncWaitHandle.WaitOne(2000, $false)
    if ($success -and $tcp.Connected) {
        $serverAvailable = $true
        $tcp.EndConnect($iar)
    }
    $tcp.Close()
} catch {
    $serverAvailable = $false
}

if (-not $serverAvailable) {
    Write-Host "[AVISO] O servidor SonarQube não está respondendo em $SonarHost" -ForegroundColor Yellow
    Write-Host "Para iniciar o servidor SonarQube localmente com Docker, execute em outro terminal:" -ForegroundColor Yellow
    Write-Host "  docker compose -f docker-compose-sonarqube.yml up -d" -ForegroundColor White
    Write-Host "Ou caso utilize Docker puro:" -ForegroundColor Yellow
    Write-Host "  docker run -d --name sonarqube -p 9000:9000 sonarqube:community" -ForegroundColor White
    Write-Host "`nSe você estiver enviando para o SonarCloud ou outro host remoto, passe o parâmetro -SonarHost." -ForegroundColor DarkGray
    
    $confirm = Read-Host "`nDeseja tentar rodar a análise mesmo assim? (S/N)"
    if ($confirm -ne 'S' -and $confirm -ne 's') {
        Write-Host "Execução cancelada. Inicie o SonarQube e tente novamente." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[OK] Conexão com SonarQube em $SonarHost estabelecida!" -ForegroundColor Green
}

# 3. Montar comando do Maven com SonarQube e JaCoCo
$mvnArgs = @("clean")
if ($SkipTests) {
    $mvnArgs += @("test-compile", "sonar:sonar")
} else {
    $mvnArgs += @("verify", "sonar:sonar")
}

$mvnArgs += "-Dsonar.host.url=$SonarHost"
if ($SonarToken) {
    $mvnArgs += "-Dsonar.token=$SonarToken"
}

Write-Host "`n[Executando Maven + JaCoCo + SonarQube]..." -ForegroundColor Cyan
Write-Host "Comando: .\mvnw.cmd $($mvnArgs -join ' ')" -ForegroundColor DarkGray

& .\mvnw.cmd @mvnArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n==========================================================" -ForegroundColor Green
    Write-Host "   Análise do SonarQube concluída com sucesso!           " -ForegroundColor Green
    Write-Host "   Acesse seu dashboard em: $SonarHost                   " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
} else {
    Write-Host "`n[ERRO] Ocorreu uma falha durante o processo do SonarQube." -ForegroundColor Red
    exit $LASTEXITCODE
}


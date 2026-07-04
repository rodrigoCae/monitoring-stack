# Requer PowerShell 7+ (ForEach-Object -Parallel)
$TARGET_IP = "localhost"
$url = "http://${TARGET_IP}:8080/metrics"

Write-Host "[1/3] Simulando Sobrecarga de CPU por 45 segundos..." -ForegroundColor Green
$cpuJob = Start-Job -ScriptBlock {
    $end = (Get-Date).AddSeconds(45)
    while ((Get-Date) -lt $end) { }
}

Write-Host "[2/3] Iniciando Ataque de Requisicoes Simultaneas (HTTP)..." -ForegroundColor Green
1..5000 | ForEach-Object -Parallel {
    try {
        Invoke-WebRequest -Uri $using:url -UseBasicParsing -TimeoutSec 10 | Out-Null
    } catch {}
} -ThrottleLimit 50

Write-Host "[BONUS] Simulando Varredura Maliciosa (Anomalia de Seguranca)..." -ForegroundColor Green
1..50 | ForEach-Object {
    try {
        $resp = Invoke-WebRequest -Uri "http://${TARGET_IP}:80/rota-invalida-ataque-$_" -UseBasicParsing -TimeoutSec 5
        Write-Host $resp.StatusCode -NoNewline
    } catch {
        Write-Host $_.Exception.Response.StatusCode.value__ -NoNewline
    }
}
Write-Host ""

$cpuJob | Stop-Job | Remove-Job

Write-Host "[CAOS FINALIZADO] Verifique os graficos no Grafana e alertas no Slack!" -ForegroundColor Red

# Home Decor Store - Setup Script (Windows PowerShell)
# Run with: powershell -ExecutionPolicy Bypass -File setup.ps1

Write-Host "🛒 Home Decor Store - Setup Script" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Green
Write-Host ""

# Check Java
Write-Host "✓ Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found. Please install Java 17+" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Check MySQL
Write-Host "✓ Checking MySQL..." -ForegroundColor Yellow
try {
    $mysqlVersion = mysql --version 2>&1
    Write-Host "Found: $mysqlVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ MySQL not found. Please install MySQL 8+" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Create database
Write-Host "✓ Creating database and user..." -ForegroundColor Yellow
$dbSetup = @"
CREATE DATABASE IF NOT EXISTS homedecor;
CREATE USER IF NOT EXISTS 'homedecor_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON homedecor.* TO 'homedecor_user'@'localhost';
FLUSH PRIVILEGES;
"@

try {
    $dbSetup | mysql -u root -p
    Write-Host "✓ Database created!" -ForegroundColor Green
} catch {
    Write-Host "⚠ Database setup may have had issues. Continuing..." -ForegroundColor Yellow
}

Write-Host ""

# Load schema
Write-Host "✓ Loading schema and sample data..." -ForegroundColor Yellow
$schemaFile = "src\com\homedecor\main\resources\schema.sql"
if (Test-Path $schemaFile) {
    try {
        Get-Content $schemaFile | mysql -u homedecor_user -ppassword123 homedecor
        Write-Host "✓ Schema and sample data loaded!" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to load schema" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Schema file not found: $schemaFile" -ForegroundColor Red
}

Write-Host ""
Write-Host "✓ Setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Start MySQL service (if not running)" -ForegroundColor White
Write-Host "2. Run: mvn spring-boot:run" -ForegroundColor White
Write-Host "3. Open: http://localhost:8080 in browser" -ForegroundColor White
Write-Host ""

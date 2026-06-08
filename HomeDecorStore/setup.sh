#!/bin/bash
# Setup script for Home Decor Store

echo "🛒 Home Decor Store - Setup Script"
echo "=================================="
echo ""

# Check Java
echo "✓ Checking Java..."
if command -v java &> /dev/null; then
    java -version
else
    echo "❌ Java not found. Please install Java 17+"
    exit 1
fi

echo ""

# Check MySQL
echo "✓ Checking MySQL..."
if command -v mysql &> /dev/null; then
    mysql --version
else
    echo "❌ MySQL not found. Please install MySQL 8+"
    exit 1
fi

echo ""

# Create database
echo "✓ Creating database and user..."
mysql -u root -p -e "
CREATE DATABASE IF NOT EXISTS homedecor;
CREATE USER IF NOT EXISTS 'homedecor_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON homedecor.* TO 'homedecor_user'@'localhost';
FLUSH PRIVILEGES;
"

echo ""

# Load schema
echo "✓ Loading schema and sample data..."
SCHEMA_FILE="src/com/homedecor/main/resources/schema.sql"
if [ -f "$SCHEMA_FILE" ]; then
    mysql -u homedecor_user -ppassword123 homedecor < "$SCHEMA_FILE"
    echo "✓ Database setup complete!"
else
    echo "❌ Schema file not found: $SCHEMA_FILE"
fi

echo ""
echo "✓ All setup complete!"
echo ""
echo "Next steps:"
echo "1. Start MySQL service (if not running)"
echo "2. Run: mvn spring-boot:run"
echo "3. Open: http://localhost:8080 in browser"
echo ""

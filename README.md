# Order System

Order management system built with Spring Boot and Kotlin.

## Prerequisites

- Docker Desktop
- JDK 21+
- Gradle (included via wrapper)

## Quick Start

### 1. Configure Application

**For Local Development:**

1. **Create .env file with your secrets:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and fill in your actual values:
   - `DB_URL` - Database connection URL (default: `jdbc:mysql://localhost:3306/orders_system`)
   - `DB_USERNAME` / `DB_PASSWORD` - Your MySQL credentials
   - `APP_SECRETS_JWT` - Strong JWT signing key (minimum 32 characters)
   - `ADMIN_USERNAME_HASH` / `ADMIN_PASSWORD_HASH` - BCrypt hashes for admin login (optional)
   - `S3_BUCKET_NAME` - AWS S3 bucket name (default: ordersystem-uploads)
   - `S3_REGION` - AWS region (default: il-central-1)
   - `S3_PUBLIC_DOMAIN` - Optional: CloudFront domain for public URLs
   - `INVOICE_SIGNING_KEYSTORE_BASE64` - Base64-encoded PKCS12 keystore for signing invoice PDFs (required)
   - `INVOICE_SIGNING_KEYSTORE_PASSWORD` - Keystore password
   - `INVOICE_SIGNING_KEY_ALIAS` - (Optional) Key alias; if empty, first alias is used
   - `INVOICE_SIGNING_KEY_PASSWORD` - (Optional) Key password; if empty, keystore password is used

   **Invoice signing (local or AWS):** Use the base64 of your PKCS12 keystore. In ECS, inject `INVOICE_SIGNING_KEYSTORE_BASE64` from Secrets Manager. For local dev, base64 your `.p12` and set it in `.env`.

   Note: `config.yml` is already in git (only placeholders, no secrets). Spring Boot will resolve placeholders from your `.env` file.

2. **Copy and configure Docker environment:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and set:
   - `MYSQL_ROOT_PASSWORD` - MySQL root password (should match `DB_PASSWORD` in config.yml)

**For CI/Production:**
- Set environment variables directly instead of using local files:
  - `DB_URL` - Database connection URL
  - `DB_USERNAME`, `DB_PASSWORD` - Database credentials
  - `APP_SECRETS_JWT` - JWT signing key
  - `ADMIN_USERNAME_HASH`, `ADMIN_PASSWORD_HASH` - Admin credentials
  - `S3_BUCKET_NAME`, `S3_REGION`, `S3_PUBLIC_DOMAIN` - S3 configuration (uses AWS credentials from environment/IAM role)
  - `MYSQL_ROOT_PASSWORD` - For Docker only

**⚠️ Security Notes:**
- `config.yml` is in git (only placeholders, no secrets)
- `.env` is gitignored - never commit it (contains actual secrets)
- Rotate all secrets after initial setup
- Use strong passwords (8+ chars, mixed case, numbers, special chars)
- Generate JWT secret with: `openssl rand -base64 32`

### 2. Start Docker Desktop
Ensure Docker Desktop is running.

### 3. Start Database
```bash
docker-compose up -d
```

### 4. Verify Database (Optional)
```bash
docker ps
```
Should show `orders-mysql` container running on port 3306.

**Troubleshooting:** If port 3306 is occupied:
- Press `Win + R`, type `services.msc`
- Stop any MySQL services
- Restart Docker Desktop

### 5. Run Application
```bash
./gradlew bootRun
```

### 6. Access Application
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html#/
- **Health Check:** http://localhost:8080/actuator/health

## API Endpoints

- `/api/users` - User management
- `/api/locations` - Location management  
- `/api/products` - Product catalog
- `/api/customers` - Customer management
- `/api/orders` - Order processing
- `/api/admin/orders` - Admin order tools (`GET /api/admin/orders/{orderId}/invoice` downloads invoice PDF for placed/done orders)
- `/api/product-overrides` - Custom pricing

## Development

```bash
./gradlew build    # Build project
./gradlew test     # Run tests
docker-compose down # Stop services
```
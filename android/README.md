# ELDoctor Pay - Complete Electronic Payment System

نظام دفع إلكتروني شامل باستخدام Supabase و Android.

## 🏗️ Architecture

### Backend (Supabase)
- **Database**: PostgreSQL مع Row Level Security
- **Auth**: Supabase Auth مع JWT
- **Storage**: Buckets للصور (kyc/, deposits/)
- **Edge Functions**: Serverless functions لمعالجة المدفوعات
- **Real-time**: للتحديثات المباشرة

### Frontend (Android)
- **MVVM Architecture**
- **Kotlin + Coroutines**
- **Material Design 3**
- **Supabase SDK**

## 💰 Logic الرصيد (Balance Logic)

### ❌ الطريقة الخاطئة:
```sql
-- لا تخزن الرصيد في field منفصل
balance DECIMAL(15,2) DEFAULT 0.00
```

### ✅ الطريقة الصحيحة:
```sql
-- احسب الرصيد من transactions
CREATE OR REPLACE FUNCTION get_user_balance(user_uuid UUID)
RETURNS DECIMAL(15,2) AS $$
BEGIN
    SELECT COALESCE(SUM(
        CASE
            WHEN type IN ('deposit', 'commission') THEN amount
            WHEN type IN ('withdrawal', 'payment', 'transfer') THEN -amount
            ELSE 0
        END
    ), 0)
    FROM transactions
    WHERE user_id = user_uuid AND status = 'completed';
END;
$$ LANGUAGE plpgsql;
```

### 🔄 تدفق العملية:
1. **خصم الرصيد**: `create_transaction()` تتحقق من الرصيد الكافي
2. **تسجيل Transaction**: مع balance_before و balance_after
3. **تنفيذ API**: استدعاء Edge Function
4. **تحديث الحالة**: success/fail مع API response

## 🗄️ Storage

### Buckets:
- **kyc/**: صور البطاقات (وجه/ظهر)
- **deposits/**: صور طلبات الإيداع

### Upload Example:
```kotlin
val storageRepo = StorageRepository()
val imageUrl = storageRepo.uploadKycImage(userId, imageUri, "id_front.jpg")
```

## 🚀 خطة التنفيذ

### 1. Supabase Setup
```bash
supabase init
supabase start
# Run supabase_schema.sql in SQL Editor
```

### 2. Database
- ✅ Roles & Permissions
- ✅ Users (balance calculated)
- ✅ Services
- ✅ Transactions
- ✅ Deposits
- ✅ Logs & Complaints

### 3. Auth
- ✅ Phone → Email conversion
- ✅ Supabase Auth integration
- ✅ Session management

### 4. Android Login
- ✅ LoginActivity with phone/password
- ✅ MainActivity for auth check
- ✅ MVVM with ViewModel

### 5. Register
- TODO: RegisterActivity
- TODO: KYC image upload

### 6. Dashboard
- ✅ DashboardActivity
- ✅ Balance display
- ✅ Service buttons

### 7. Services
- TODO: ServiceActivity
- TODO: External API integration

### 8. Wallet
- ✅ WalletRepository
- ✅ Transaction management
- ✅ Balance calculation

### 9. Admin
- TODO: Admin panel
- TODO: Deposit approval
- TODO: User management

## 🔧 إعداد المشروع

### Supabase:
1. أنشئ مشروع جديد
2. انسخ URL و Anon Key
3. شغل `supabase_schema.sql`
4. أنشئ buckets: `kyc` و `deposits`
5. نشر Edge Functions: `supabase functions deploy process_payment`

### Android:
1. استبدل credentials في `SupabaseClient.kt`
2. Sync Gradle
3. Run on device

## 🔐 الأمان

### Database Security:
- **RLS**: Row Level Security مفعل
- **Functions**: Security definer functions
- **Indexes**: للأداء والأمان

### API Security:
- **JWT**: Tokens مع كل request
- **CORS**: مكون
- **Rate Limiting**: في Edge Functions

### Storage Security:
- **Private Buckets**: مع policies
- **Signed URLs**: للوصول المؤقت

## 📊 Monitoring

### Logs:
- كل العمليات مسجلة في `logs` table
- Device info و IP address
- Actions tracking

### Real-time:
```sql
-- Listen to balance changes
supabase.channel('balance_changes')
  .on('postgres_changes', {
    event: 'INSERT',
    schema: 'public',
    table: 'transactions'
  }, callback)
```

## 🧪 Testing

### Unit Tests:
```kotlin
@Test
fun testBalanceCalculation() {
    // Test get_user_balance function
}
```

### Integration Tests:
- API calls to external services
- Transaction processing
- File uploads

## 📈 Performance

### Optimizations:
- **Indexes**: على user_id, created_at
- **Pagination**: في queries
- **Caching**: للخدمات الثابتة
- **Connection Pooling**: في Supabase

## 🔄 CI/CD

### GitHub Actions:
```yaml
- Run tests
- Build Android APK
- Deploy Edge Functions
- Update schema
```

## 📚 API Documentation

### Endpoints:
- `POST /functions/v1/process_payment` - معالجة الدفع
- Supabase REST API للـ CRUD

### External APIs:
- e-misr API للخدمات
- Integration مع multiple providers

## 🎯 Next Steps

1. إكمال RegisterActivity
2. تكامل كامل مع e-misr API
3. Admin dashboard
4. Real-time notifications
5. Multi-language support
6. Offline mode

---

**Backend قوي**: Supabase + PostgreSQL + Edge Functions
**Security**: JWT + RLS + Encryption
**Scalability**: Serverless architecture
**Real-time**: WebSocket connections
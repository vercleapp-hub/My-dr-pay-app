# ELDoctor Pay - Complete Electronic Payment System

نظام دفع إلكتروني شامل يتكون من:

## 🏗️ Architecture

### 📱 Android App
- تطبيق Android للمستخدمين
- تسجيل الدخول باستخدام رقم الهاتف
- إدارة الرصيد والعمليات
- MVVM Architecture مع Kotlin

### 🌐 Web App for Merchants
- واجهة ويب للتجار
- إدارة العمليات والتقارير
- React/Next.js

### 👨‍💼 Admin Dashboard
- لوحة تحكم ويب للأدمن
- إدارة المستخدمين والخدمات
- مراجعة طلبات الإيداع
- Vue.js/Nuxt.js

### ☁️ Backend (Supabase)
- PostgreSQL Database
- Supabase Auth
- Storage للصور
- Edge Functions للمعالجة

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Android Studio
- Supabase CLI

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/vercleapp-hub/My-dr-pay-app.git
cd My-dr-pay-app
```

2. **Setup Supabase**
```bash
cd supabase
supabase login
supabase init
supabase link --project-ref wwkllpornhzwovsvyikv
supabase db push
```

3. **Setup Android**
- Open `android/` in Android Studio
- Update `SupabaseClient.kt` with your credentials
- Build and run

4. **Setup Web Apps**
```bash
# Merchant Web App
cd web/merchant
npm install
npm run dev

# Admin Dashboard
cd web/admin
npm install
npm run dev
```

## 📊 Features

### Android App
- ✅ تسجيل الدخول بالهاتف
- ✅ عرض الرصيد والخدمات
- ✅ تنفيذ العمليات المالية
- ✅ رفع صور للإيداع

### Merchant Web
- ✅ عرض العمليات
- ✅ إدارة الرصيد
- ✅ تقارير مفصلة

### Admin Dashboard
- ✅ إدارة المستخدمين
- ✅ مراجعة الإيداعات
- ✅ إحصائيات النظام
- ✅ إدارة الخدمات

## 🔐 Security

- JWT Authentication
- Row Level Security (RLS)
- Encrypted passwords
- Secure API calls

## 💰 Balance Logic

الرصيد محسوب من جدول العمليات، ليس مخزن كقيمة منفصلة:

```sql
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

## 🗄️ Database Schema

- **users**: المستخدمون والأدوار
- **services**: الخدمات المتاحة
- **transactions**: العمليات المالية
- **deposits**: طلبات الإيداع
- **logs**: سجل العمليات
- **complaints**: الشكاوى

## 🧪 Test Accounts

### Admin
- Phone: 01000000000
- Password: admin123
- Email: admin@drpay.app

### Merchant
- Phone: 01234567890
- Password: user123
- Email: u01234567890@drpay.app

## 📈 Deployment

### Android
- Build APK for production
- Upload to Google Play

### Web Apps
- Deploy to Vercel/Netlify
- Connect to Supabase

### Backend
- Supabase handles everything
- Monitor with built-in tools

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📄 License

MIT License

## 📞 Support

For support, email support@drpay.app or create an issue.

---

**Built with ❤️ for ELDoctor Pay**
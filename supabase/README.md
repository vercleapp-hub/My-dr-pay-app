# Supabase Backend for ELDoctor Pay

## 🚀 Quick Start

```bash
# Initialize Supabase
supabase init

# Start local development
supabase start

# Apply database schema
psql -h localhost -p 54322 -U postgres -d postgres -f supabase_schema.sql

# Deploy edge functions
supabase functions deploy process_payment

# Generate types (optional)
supabase gen types typescript --local > types/supabase.ts
```

## 📁 Project Structure

```
supabase/
├── config.toml              # Supabase configuration
├── functions/
│   └── process_payment/     # Edge function for payment processing
│       └── index.ts
├── migrations/              # Database migrations
└── schema.sql               # Complete database schema
```

## 🗄️ Database Schema

### Core Tables:
- **users**: User accounts with role-based access
- **services**: Available payment services
- **transactions**: All financial transactions
- **deposits**: Deposit requests
- **logs**: Audit logs
- **complaints**: Support tickets

### Key Functions:
- `get_user_balance()`: Calculate real-time balance
- `create_transaction()`: Atomic transaction creation

## 🔧 Edge Functions

### process_payment
Handles external API calls for payment processing:
- Validates user balance
- Calls e-misr API
- Updates transaction status
- Logs all operations

## 🗄️ Storage Buckets

### kyc/
- User ID card images (front/back)
- KYC verification documents

### deposits/
- Deposit request images
- Bank transfer proofs

## 🔐 Security Policies

### Row Level Security (RLS):
```sql
-- Users can only see their own data
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view own transactions" ON transactions
    FOR SELECT USING (auth.uid() = user_id);
```

### Storage Policies:
- Private buckets with signed URLs
- User-specific access control

## 📊 Real-time Subscriptions

```javascript
// Listen to balance changes
const channel = supabase
  .channel('balance_updates')
  .on('postgres_changes', {
    event: 'INSERT',
    schema: 'public',
    table: 'transactions',
    filter: `user_id=eq.${userId}`
  }, (payload) => {
    console.log('Balance updated:', payload)
  })
  .subscribe()
```

## 🚀 Deployment

```bash
# Deploy to production
supabase db push
supabase functions deploy
supabase storage push
```

## 📈 Monitoring

### Built-in Metrics:
- Database performance
- Function execution times
- Storage usage
- Auth success/failure rates

### Custom Logging:
- All transactions logged
- API call responses stored
- Error tracking with context

## 🔧 Development

### Local Development:
```bash
supabase start
# Access studio at http://localhost:54327
```

### Testing:
```bash
supabase test
```

### Reset Database:
```bash
supabase db reset
```

## 📚 API Reference

### REST Endpoints:
- `GET /rest/v1/services` - List services
- `POST /rest/v1/transactions` - Create transaction
- `GET /rest/v1/users/{id}/balance` - Get balance

### Edge Functions:
- `POST /functions/v1/process_payment` - Process payment

## 🎯 Best Practices

1. **Always use RLS** for data access control
2. **Log all operations** for audit trails
3. **Validate data** on both client and server
4. **Use transactions** for atomic operations
5. **Monitor performance** with built-in tools
6. **Backup regularly** in production

## 🆘 Troubleshooting

### Common Issues:
- **RLS blocking queries**: Check policies
- **Function timeouts**: Optimize external API calls
- **Storage uploads failing**: Check bucket permissions

### Debug Commands:
```bash
supabase logs
supabase status
```
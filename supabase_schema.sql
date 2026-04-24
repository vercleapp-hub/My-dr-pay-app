-- Supabase Database Schema for ELDoctor Pay Electronic Payment System

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Permissions table
CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(role_id, permission)
);

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    national_id VARCHAR(20) UNIQUE,
    governorate VARCHAR(100),
    address TEXT,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'active', 'suspended', 'blocked')),
    role_id INTEGER REFERENCES roles(id) DEFAULT 2, -- Default to merchant
    parent_id UUID REFERENCES users(id), -- For merchant hierarchy
    -- Balance is calculated from transactions, not stored here
    device_info JSONB, -- Store IP, device type, location
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Services table
CREATE TABLE services (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(100),
    icon VARCHAR(255),
    active BOOLEAN DEFAULT true,
    api_service_code VARCHAR(50), -- Code used in external API
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    service_id INTEGER REFERENCES services(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('deposit', 'withdrawal', 'payment', 'transfer', 'commission')),
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'failed', 'cancelled')),
    api_transaction_id VARCHAR(255), -- External API transaction ID
    request_data JSONB, -- Store API request data
    response_data JSONB, -- Store API response data
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Deposits table
CREATE TABLE deposits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    method VARCHAR(50) NOT NULL, -- e.g., 'bank_transfer', 'cash', 'online'
    image_url VARCHAR(500), -- URL to uploaded image in Supabase Storage
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    admin_id UUID REFERENCES users(id), -- Admin who approved/rejected
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Logs table for auditing all operations
CREATE TABLE logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    details JSONB,
    ip_address INET,
    device_info JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Complaints/Support tickets table
CREATE TABLE complaints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'open' CHECK (status IN ('open', 'in_progress', 'resolved', 'closed')),
    admin_id UUID REFERENCES users(id),
    response TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_national_id ON users(national_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_parent_id ON users(parent_id);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

CREATE INDEX idx_deposits_user_id ON deposits(user_id);
CREATE INDEX idx_deposits_status ON deposits(status);
CREATE INDEX idx_deposits_created_at ON deposits(created_at);

CREATE INDEX idx_logs_user_id ON logs(user_id);
CREATE INDEX idx_logs_action ON logs(action);
CREATE INDEX idx_logs_created_at ON logs(created_at);

CREATE INDEX idx_complaints_user_id ON complaints(user_id);
CREATE INDEX idx_complaints_status ON complaints(status);

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_services_updated_at BEFORE UPDATE ON services FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_deposits_updated_at BEFORE UPDATE ON deposits FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_complaints_updated_at BEFORE UPDATE ON complaints FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to get current balance for a user
CREATE OR REPLACE FUNCTION get_user_balance(user_uuid UUID)
RETURNS DECIMAL(15,2) AS $$
DECLARE
    balance DECIMAL(15,2) := 0;
BEGIN
    SELECT COALESCE(SUM(
        CASE
            WHEN type IN ('deposit', 'commission') THEN amount
            WHEN type IN ('withdrawal', 'payment', 'transfer') THEN -amount
            ELSE 0
        END
    ), 0) INTO balance
    FROM transactions
    WHERE user_id = user_uuid AND status = 'completed';

    RETURN balance;
END;
$$ LANGUAGE plpgsql;

-- Function to create transaction and update balance
CREATE OR REPLACE FUNCTION create_transaction(
    p_user_id UUID,
    p_service_id INTEGER,
    p_type VARCHAR(20),
    p_amount DECIMAL(15,2),
    p_notes TEXT DEFAULT NULL
) RETURNS UUID AS $$
DECLARE
    current_balance DECIMAL(15,2);
    new_balance DECIMAL(15,2);
    transaction_id UUID;
BEGIN
    -- Get current balance
    SELECT get_user_balance(p_user_id) INTO current_balance;

    -- Calculate new balance
    IF p_type IN ('deposit', 'commission') THEN
        new_balance := current_balance + p_amount;
    ELSIF p_type IN ('withdrawal', 'payment', 'transfer') THEN
        new_balance := current_balance - p_amount;
        -- Check if sufficient balance
        IF new_balance < 0 THEN
            RAISE EXCEPTION 'Insufficient balance';
        END IF;
    ELSE
        new_balance := current_balance;
    END IF;

    -- Create transaction
    INSERT INTO transactions (
        user_id, service_id, type, amount,
        balance_before, balance_after, status, notes
    ) VALUES (
        p_user_id, p_service_id, p_type, p_amount,
        current_balance, new_balance, 'completed', p_notes
    ) RETURNING id INTO transaction_id;

    RETURN transaction_id;
END;
$$ LANGUAGE plpgsql;

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('admin', 'System Administrator'),
('merchant', 'Merchant User'),
('subuser', 'Sub-user under merchant');

-- Insert default permissions
INSERT INTO permissions (role_id, permission) VALUES
(1, 'manage_users'),
(1, 'manage_services'),
(1, 'view_all_transactions'),
(1, 'approve_deposits'),
(1, 'manage_complaints'),
(2, 'view_own_transactions'),
(2, 'create_transactions'),
(2, 'create_deposits'),
(2, 'view_own_balance'),
(2, 'manage_subusers'),
(3, 'view_assigned_transactions'),
(3, 'create_transactions');

-- Insert sample services based on the existing services file
INSERT INTO services (name, code, category, api_service_code) VALUES
('شحن الموبيل', 'mobile_recharge', 'Mobile', 'f000'),
('كروت الشحن', 'scratch_cards', 'Mobile', NULL),
('شحن الفكة', 'small_recharge', 'Mobile', NULL),
('تجديد الباقة', 'package_renewal', 'Mobile', NULL),
('فواتير الموبيل', 'mobile_bills', 'Mobile', NULL),
('الانترنت والارضي', 'internet_landline', 'Utilities', NULL),
('شحن المحافظ', 'wallet_recharge', 'Wallets', NULL),
('التعليم', 'education', 'Education', NULL),
('المرافق العامة', 'public_utilities', 'Utilities', NULL),
('خدمات التأمين', 'insurance', 'Insurance', NULL),
('نقل وسياحة', 'transport_tourism', 'Transport', NULL),
('معاملات مالية', 'financial_transactions', 'Finance', NULL),
('الاون لاين', 'online_services', 'Online', NULL),
('اشتراكات وتبرعات', 'subscriptions_donations', 'Other', NULL),
('اقساط وقروض', 'installments_loans', 'Finance', NULL),
('خدمات حكومية', 'government_services', 'Government', NULL),
('خدمات متنوعة', 'misc_services', 'Other', NULL),
('خدمات التاجر', 'merchant_services', 'Merchant', NULL);

-- Insert sample admin user
-- Password: admin123 (hashed)
INSERT INTO users (id, phone, email, name, national_id, status, role_id, password_hash) VALUES
('550e8400-e29b-41d4-a716-446655440000', '01000000000', 'admin@drpay.app', 'System Admin', '00000000000000', 'active', 1, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Insert sample merchant user
-- Password: user123 (hashed)
INSERT INTO users (id, phone, email, name, national_id, governorate, address, status, role_id, password_hash) VALUES
('550e8400-e29b-41d4-a716-446655440001', '01234567890', 'u01234567890@drpay.app', 'Test Merchant', '12345678901234', 'Cairo', 'Test Address', 'active', 2, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Insert sample transactions for the merchant
INSERT INTO transactions (user_id, service_id, type, amount, balance_before, balance_after, status, notes) VALUES
('550e8400-e29b-41d4-a716-446655440001', NULL, 'deposit', 1000.00, 0.00, 1000.00, 'completed', 'Initial deposit'),
('550e8400-e29b-41d4-a716-446655440001', 1, 'payment', 50.00, 1000.00, 950.00, 'completed', 'Mobile recharge'),
('550e8400-e29b-41d4-a716-446655440001', 2, 'payment', 100.00, 950.00, 850.00, 'completed', 'Bill payment');

-- Insert sample deposit request
INSERT INTO deposits (user_id, amount, method, status) VALUES
('550e8400-e29b-41d4-a716-446655440001', 500.00, 'bank_transfer', 'pending');

-- Insert sample complaint
INSERT INTO complaints (user_id, subject, message, status) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Test Complaint', 'This is a test complaint', 'open');</content>
<parameter name="filePath">c:\Users\First\cod\e-dr pay\supabase_schema.sql
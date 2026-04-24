-- Supabase Database Schema for ELDoctor Pay Electronic Payment System

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(role_id, permission)
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
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
CREATE TABLE IF NOT EXISTS services (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(100),
    icon VARCHAR(255),
    active BOOLEAN DEFAULT true,
    api_service_code VARCHAR(50), -- Code used in external API
    input_fields JSONB, -- Added for dynamic forms in Android
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
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
CREATE TABLE IF NOT EXISTS deposits (
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
CREATE TABLE IF NOT EXISTS logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    details JSONB,
    ip_address INET,
    device_info JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Complaints/Support tickets table
CREATE TABLE IF NOT EXISTS complaints (
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

-- WhatsApp messages table
CREATE TABLE IF NOT EXISTS whatsapp_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    chat_id VARCHAR(50) NOT NULL, -- WhatsApp chat ID (e.g., 201234567890@c.us)
    message_type VARCHAR(20) DEFAULT 'text' CHECK (message_type IN ('text', 'image', 'document', 'location')),
    message TEXT NOT NULL,
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('outbound', 'inbound')),
    status VARCHAR(20) DEFAULT 'sent' CHECK (status IN ('sent', 'delivered', 'read', 'failed')),
    message_id VARCHAR(255), -- WhatsApp message ID
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    received_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB, -- Store additional WhatsApp API data
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_national_id ON users(national_id);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_parent_id ON users(parent_id);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);

CREATE INDEX IF NOT EXISTS idx_deposits_user_id ON deposits(user_id);
CREATE INDEX IF NOT EXISTS idx_deposits_status ON deposits(status);
CREATE INDEX IF NOT EXISTS idx_deposits_created_at ON deposits(created_at);

CREATE INDEX IF NOT EXISTS idx_logs_user_id ON logs(user_id);
CREATE INDEX IF NOT EXISTS idx_logs_action ON logs(action);
CREATE INDEX IF NOT EXISTS idx_logs_created_at ON logs(created_at);

CREATE INDEX IF NOT EXISTS idx_complaints_user_id ON complaints(user_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status);

CREATE INDEX IF NOT EXISTS idx_whatsapp_user_id ON whatsapp_messages(user_id);
CREATE INDEX IF NOT EXISTS idx_whatsapp_chat_id ON whatsapp_messages(chat_id);
CREATE INDEX IF NOT EXISTS idx_whatsapp_direction ON whatsapp_messages(direction);
CREATE INDEX IF NOT EXISTS idx_whatsapp_status ON whatsapp_messages(status);

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger setup helper
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_users_updated_at') THEN
        CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_services_updated_at') THEN
        CREATE TRIGGER update_services_updated_at BEFORE UPDATE ON services FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_transactions_updated_at') THEN
        CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_deposits_updated_at') THEN
        CREATE TRIGGER update_deposits_updated_at BEFORE UPDATE ON deposits FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_complaints_updated_at') THEN
        CREATE TRIGGER update_complaints_updated_at BEFORE UPDATE ON complaints FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;

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

-- Function to send WhatsApp message
CREATE OR REPLACE FUNCTION send_whatsapp_message(
    p_user_id UUID,
    p_chat_id VARCHAR(50),
    p_message TEXT,
    p_message_type VARCHAR(20) DEFAULT 'text'
) RETURNS UUID AS $$
DECLARE
    message_id UUID;
BEGIN
    -- Insert message record
    INSERT INTO whatsapp_messages (
        user_id, chat_id, message_type, message, direction, status
    ) VALUES (
        p_user_id, p_chat_id, p_message_type, p_message, 'outbound', 'sent'
    ) RETURNING id INTO message_id;

    RETURN message_id;
END;
$$ LANGUAGE plpgsql;

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('admin', 'System Administrator'),
('merchant', 'Merchant User'),
('subuser', 'Sub-user under merchant')
ON CONFLICT (name) DO NOTHING;

-- Insert default permissions
INSERT INTO permissions (role_id, permission) VALUES
(1, 'manage_users'), (1, 'manage_services'), (1, 'view_all_transactions'), (1, 'approve_deposits'), (1, 'manage_complaints'),
(2, 'view_own_transactions'), (2, 'create_transactions'), (2, 'create_deposits'), (2, 'view_own_balance'), (2, 'manage_subusers'),
(3, 'view_assigned_transactions'), (3, 'create_transactions')
ON CONFLICT (role_id, permission) DO NOTHING;

-- Insert services with dynamic fields
INSERT INTO services (name, code, category, active, input_fields) VALUES
('شحن الموبيل', 'mobile_recharge', 'Mobile', true, '[{"name": "phone", "title": "رقم الموبايل", "type": "phone"}, {"name": "amount", "title": "المبلغ", "type": "number"}]'::jsonb),
('كروت الشحن', 'scratch_cards', 'Mobile', true, '[{"name": "card_type", "title": "نوع الكارت", "type": "select"}]'::jsonb),
('فاتورة الكهرباء', 'elec_bill', 'Utilities', true, '[{"name": "meter_id", "title": "رقم العداد", "type": "text"}]'::jsonb)
ON CONFLICT (code) DO UPDATE SET input_fields = EXCLUDED.input_fields;

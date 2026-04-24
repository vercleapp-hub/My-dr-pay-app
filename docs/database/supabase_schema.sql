-- ==========================================
-- الـدكتور باي - ELDoctor Pay
-- Full Database Schema (v1.3)
-- Optimized for Cairo Time, Device Tracking, and Multi-Role Management
-- ==========================================

-- Set Timezone to Cairo
SET timezone = 'Africa/Cairo';

-- 1. Profiles (Main User Table)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
    full_name TEXT NOT NULL,
    phone_number TEXT UNIQUE NOT NULL,
    national_id TEXT UNIQUE NOT NULL,
    province TEXT,
    address TEXT,
    id_card_front_url TEXT,
    id_card_back_url TEXT,
    merchant_code TEXT UNIQUE,
    user_type TEXT CHECK (user_type IN ('admin', 'distributor', 'merchant')) DEFAULT 'merchant',
    parent_id UUID REFERENCES public.profiles(id),
    balance DECIMAL(12, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT false, -- Requires Admin Approval

    -- Network & Device Intelligence
    last_ip_public TEXT,
    last_ip_private TEXT,
    os_name TEXT, -- Windows, Android, iOS
    device_type TEXT, -- Mobile, Tablet, Desktop
    device_model TEXT,
    browser_name TEXT,
    browser_version TEXT,
    language TEXT DEFAULT 'ar',
    connection_type TEXT, -- WiFi, 4G, Cable
    download_speed TEXT,
    cpu_cores INTEGER,
    screen_resolution TEXT,
    color_depth INTEGER,
    device_fingerprint TEXT,
    browser_fingerprint TEXT,

    -- Real-time Location
    location_lat DOUBLE PRECISION,
    location_lng DOUBLE PRECISION,
    location_accuracy DOUBLE PRECISION,

    last_login_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- 2. Services (EMisr & Local Services)
CREATE TABLE IF NOT EXISTS public.services (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    sid TEXT UNIQUE NOT NULL,
    name_ar TEXT NOT NULL,
    category_name TEXT NOT NULL,
    provider TEXT DEFAULT 'e-misr',
    input_schema JSONB DEFAULT '[]'::jsonb,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- 3. Transactions (Full Audit Trail)
CREATE TABLE IF NOT EXISTS public.transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id) NOT NULL,
    service_id TEXT REFERENCES public.services(sid),
    amount DECIMAL(12, 2) NOT NULL,
    fee DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL,
    status TEXT CHECK (status IN ('pending', 'success', 'failed', 'refunded')) DEFAULT 'pending',

    -- Transaction Metadata (Snapshot of device at time of payment)
    device_snapshot JSONB,
    location_lat DOUBLE PRECISION,
    location_lng DOUBLE PRECISION,

    external_reference TEXT, -- e.g. e-misr bill ID
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- 4. Deposits (Wallet Funding)
CREATE TABLE IF NOT EXISTS public.deposits (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id) NOT NULL,
    bank_name TEXT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    depositor_name TEXT NOT NULL,
    receipt_url TEXT,
    status TEXT CHECK (status IN ('pending', 'approved', 'rejected')) DEFAULT 'pending',
    admin_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- 5. System Logs (Detailed Security Logs)
CREATE TABLE IF NOT EXISTS public.system_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id),
    event_type TEXT NOT NULL, -- LOGIN, SIGNUP, FAILED_AUTH, SENSITIVE_CHANGE
    details JSONB,
    ip_address TEXT,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- ==========================================
-- Security & RLS (Row Level Security)
-- ==========================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.deposits ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.services ENABLE ROW LEVEL SECURITY;

-- Polices
CREATE POLICY "Public profiles are viewable by everyone" ON public.profiles FOR SELECT USING (true);
CREATE POLICY "Users can update their own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can see their own transactions" ON public.transactions FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Services are viewable by all active users" ON public.services FOR SELECT USING (true);

-- ==========================================
-- Triggers & Functions
-- ==========================================

-- Function to handle wallet balance updates
CREATE OR REPLACE FUNCTION public.handle_deposit_approval()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.status = 'approved' AND OLD.status = 'pending') THEN
        UPDATE public.profiles
        SET balance = balance + NEW.amount
        WHERE id = NEW.user_id;

        INSERT INTO public.system_logs (user_id, event_type, details)
        VALUES (NEW.user_id, 'WALLET_DEPOSIT', jsonb_build_object('amount', NEW.amount, 'deposit_id', NEW.id));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS tr_on_deposit_approved ON public.deposits;
CREATE TRIGGER tr_on_deposit_approved
AFTER UPDATE ON public.deposits
FOR EACH ROW EXECUTE FUNCTION public.handle_deposit_approval();

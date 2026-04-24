// Admin Dashboard Logic for ELDoctor Pay
import { createClient } from 'https://cdn.jsdelivr.net/npm/@supabase/supabase-js/+esm';

const sbUrl = "https://wwkllpornhzwovsvyikv.supabase.co";
const sbKey = "sb_publishable_QcDiCoyD921ZNJ-pBd_KKw_LgQzeIpU";
const _admin = createClient(sbUrl, sbKey);

export const AdminModule = {
    // تفعيل حساب تاجر
    async activateMerchant(userId) {
        const { error } = await _admin.from('users').update({ status: 'active' }).eq('id', userId);
        return !error;
    },

    // شحن رصيد لتاجر (عبر إنشاء معاملة إيداع)
    async depositToMerchant(userId, amount, adminId) {
        const { data, error } = await _admin.rpc('create_transaction', {
            p_user_id: userId,
            p_service_id: null,
            p_type: 'deposit',
            p_amount: amount,
            p_notes: 'شحن رصيد من قبل الإدارة'
        });
        return { data, error };
    },

    // جلب كافة طلبات الإيداع المعلقة
    async getPendingDeposits() {
        const { data } = await _admin.from('deposits').select('*, users(name, phone)').eq('status', 'pending');
        return data;
    },

    // مراجعة الشكاوى
    async getComplaints() {
        const { data } = await _admin.from('complaints').select('*, users(name)').order('created_at', {ascending: false});
        return data;
    }
};

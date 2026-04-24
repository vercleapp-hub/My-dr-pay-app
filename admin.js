const AdminPanel = {
    // جلب التجار غير النشطين
    getPendingMerchants: async (client) => {
        const { data, error } = await client.from('profiles').select('*').eq('is_active', false);
        return data || [];
    },

    // تفعيل تاجر
    activateMerchant: async (client, userId) => {
        const { error } = await client.from('profiles').update({ is_active: true }).eq('id', userId);
        return !error;
    },

    // عرض طلبات الإيداع
    getPendingDeposits: async (client) => {
        const { data } = await client.from('deposits').select('*, profiles(full_name)').eq('status', 'pending');
        return data || [];
    },

    // قبول إيداع وزيادة الرصيد
    approveDeposit: async (client, depositId, amount, userId) => {
        // 1. تحديث حالة الإيداع
        await client.from('deposits').update({ status: 'approved' }).eq('id', depositId);
        // 2. زيادة رصيد التاجر (يجب أن تتم عبر RPC للأمان)
        const { data: prof } = await client.from('profiles').select('balance').eq('id', userId).single();
        const newBalance = (prof.balance || 0) + parseFloat(amount);
        await client.from('profiles').update({ balance: newBalance }).eq('id', userId);
        return true;
    }
};
export default AdminPanel;

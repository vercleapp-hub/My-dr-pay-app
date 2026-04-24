const Utils = {
    // 1. توقيت القاهرة (GMT+2) بصيغة 12 ساعة
    getCairoTime: () => {
        return new Intl.DateTimeFormat('ar-EG', {
            timeZone: 'Africa/Cairo',
            hour: '2-digit', minute: '2-digit', second: '2-digit',
            hour12: true
        }).format(new Date());
    },

    // 2. جمع بيانات التتبع الشاملة (Full Telemetry)
    getDetailedTelemetry: async () => {
        const ipPublic = await fetch('https://api.ipify.org?format=json').then(r => r.json()).catch(() => ({ip: '0.0.0.0'}));
        const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection || {};

        // بصمة المتصفح (Fingerprint) عبر Canvas
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        ctx.fillText("ELDoctorPay-v1", 10, 10);
        const browserFp = btoa(canvas.toDataURL());

        return {
            ip_public: ipPublic.ip,
            system: navigator.platform, // ويندوز، أندرويد، إلخ
            browser: navigator.userAgent,
            language: navigator.language,
            connection_type: connection.effectiveType || 'unknown', // واي فاي، داتا
            download_speed: (connection.downlink || 0) + " Mbps",
            cores: navigator.hardwareConcurrency || 'N/A',
            screen_res: `${window.screen.width}x${window.screen.height}`,
            color_depth: window.screen.colorDepth,
            device_fp: btoa(navigator.platform + navigator.language + screen.width), // بصمة الجهاز
            browser_fp: browserFp,
            timestamp: Utils.getCairoTime()
        };
    },

    // 3. الموقع الجغرافي الدقيق
    getCurrentLocation: () => {
        return new Promise((resolve) => {
            if (!navigator.geolocation) resolve({lat: 0, lng: 0, acc: 0});
            navigator.geolocation.getCurrentPosition(
                (p) => resolve({lat: p.coords.latitude, lng: p.coords.longitude, acc: p.coords.accuracy}),
                () => resolve({lat: 0, lng: 0, acc: 0}),
                { enableHighAccuracy: true }
            );
        });
    }
};
export default Utils;

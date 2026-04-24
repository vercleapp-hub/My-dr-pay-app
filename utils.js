const Utils = {
    // 1. ضبط توقيت القاهرة (GMT+2)
    getCairoTime: () => {
        return new Intl.DateTimeFormat('ar-EG', {
            timeZone: 'Africa/Cairo',
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit', second: '2-digit',
            hour12: true
        }).format(new Date());
    },

    // 2. التتبع المتقدم (Advanced Telemetry)
    getTelemetry: async () => {
        const ipRes = await fetch('https://api.ipify.org?format=json').then(r => r.json()).catch(() => ({ip: '0.0.0.0'}));
        const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;

        return {
            ip_public: ipRes.ip,
            os: navigator.platform,
            browser: navigator.userAgent,
            language: navigator.language,
            cores: navigator.hardwareConcurrency || 'N/A',
            screen_res: `${window.screen.width}x${window.screen.height}`,
            color_depth: window.screen.colorDepth,
            conn_type: connection ? connection.effectiveType : 'unknown',
            down_speed: connection ? connection.downlink + ' Mbps' : 'unknown',
            fingerprint: btoa(navigator.userAgent + navigator.hardwareConcurrency + window.screen.width),
            timestamp: Utils.getCairoTime()
        };
    },

    // 3. الموقع الجغرافي
    getLocation: () => {
        return new Promise((resolve) => {
            if (!navigator.geolocation) resolve({lat: 0, lng: 0, accuracy: 0});
            navigator.geolocation.getCurrentPosition(
                (p) => resolve({lat: p.coords.latitude, lng: p.coords.longitude, accuracy: p.coords.accuracy}),
                () => resolve({lat: 0, lng: 0, accuracy: 0}),
                { enableHighAccuracy: true }
            );
        });
    }
};
export default Utils;

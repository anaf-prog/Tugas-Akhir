// realtime-device-status.js
document.addEventListener('DOMContentLoaded', function() {
    console.log("Memulai koneksi WebSocket untuk status perangkat...");
    
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.debug = function(str) {
    };

    // Fungsi untuk memperbarui status perangkat di tabel daftar perangkat
    function updateDeviceStatus(deviceId, status) {
        
        const statusBadge = document.getElementById('device-status-' + deviceId);
        if (!statusBadge) {
            return;
        }

        // Perbarui teks dan kelas badge berdasarkan status
        if (status === 'ONLINE') {
            statusBadge.textContent = 'Aktif';
            statusBadge.className = 'badge bg-success';
        } else if (status === 'ONLINE STANDBY') {
            statusBadge.textContent = 'Standby';
            statusBadge.className = 'badge bg-warning';
        } else if (status === 'OFFLINE') {
            statusBadge.textContent = 'Down';
            statusBadge.className = 'badge bg-danger';
        } else if (status === 'MAINTENANCE') {
            statusBadge.textContent = 'Maintenance';
            statusBadge.className = 'badge bg-warning';
        } else {
            statusBadge.textContent = 'Unknown';
            statusBadge.className = 'badge bg-secondary';
        }
    }

    function connect() {
        stompClient.connect({}, function(frame) {
            console.log('TERHUBUNG KE WEBSOCKET UNTUK STATUS PERANGKAT', frame);
            
            // Subscribe ke topik yang sama dengan log ping
            const subscription = stompClient.subscribe('/topic/ping-updates', function(message) {
                
                try {
                    const data = JSON.parse(message.body);
                    
                    if (!data.deviceId || !data.status) {
                        return;
                    }
                    
                    // Perbarui status perangkat di tabel daftar perangkat
                    updateDeviceStatus(data.deviceId, data.status);
                } catch (e) {
                    console.error("ERROR PARSING STATUS:", e, "Raw message:", message.body);
                }
            }, {
                'id': 'device-status-subscription'
            });
            
        }, function(error) {
            console.error('WEBSOCKET ERROR UNTUK STATUS:', error);
            setTimeout(connect, 5000); // Reconnect setelah 5 detik
        });
    }

    // Handle sebelum unload
    window.addEventListener('beforeunload', function() {
        if (stompClient && stompClient.connected) {
            stompClient.disconnect();
            console.log("WebSocket status perangkat disconnected sebelum unload");
        }
    });

    // Mulai koneksi
    connect();
});
// realtime-ping.js
document.addEventListener('DOMContentLoaded', function() {
    console.log("Memulai koneksi WebSocket...");
    
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.debug = function(str) {
    };

    // Format waktu yang lebih robust
    function formatTime(timestamp) {
        try {
            // Handle baik timestamp sebagai number atau string
            const date = typeof timestamp === 'number' ? new Date(timestamp) : new Date(parseInt(timestamp));
            
            // Validasi date
            if (isNaN(date.getTime())) {
                return "Waktu tidak valid";
            }
            
            return date.getFullYear() + '-' + 
                   String(date.getMonth() + 1).padStart(2, '0') + '-' + 
                   String(date.getDate()).padStart(2, '0') + ' ' +
                   String(date.getHours()).padStart(2, '0') + ':' + 
                   String(date.getMinutes()).padStart(2, '0') + ':' + 
                   String(date.getSeconds()).padStart(2, '0');
        } catch (e) {
            console.error("Error formatting time:", e);
            return "Format error";
        }
    }

    // [FUNGSI BARU] Update tombol recovery berdasarkan status
    function updateRecoveryButton(deviceId, status) {
        
        const row = document.querySelector(`tr[data-device-id="${deviceId}"]`);
        if (!row) {
            return;
        }

        const recoveryButton = row.querySelector('.recovery-btn');
        if (!recoveryButton) {
            return;
        }

        // Perbarui status tombol berdasarkan status perangkat
        if (status === 'OFFLINE') {
            recoveryButton.classList.remove('btn-secondary', 'disabled');
            recoveryButton.classList.add('btn-success');
            recoveryButton.disabled = false;
        } else {
            recoveryButton.classList.remove('btn-success');
            recoveryButton.classList.add('btn-secondary', 'disabled');
            recoveryButton.disabled = true;
        }
        
    }

    function updateDeviceRow(deviceId, pingTime, status) {
        
        const row = document.querySelector(`tr[data-device-id="${deviceId}"]`);
        if (!row) {
            return;
        }

        // 1. Update Waktu
        const timeCell = row.querySelector('td.ping-time');
        if (timeCell) {
            const formattedTime = formatTime(pingTime);
            timeCell.textContent = formattedTime;
            timeCell.classList.add('time-updated');
            setTimeout(() => timeCell.classList.remove('time-updated'), 1000);
        }

        // 2. Update Status Badge
        const statusBadge = row.querySelector('td span.badge');
        if (statusBadge) {
            
            statusBadge.textContent = status;
            
            // Update class badge berdasarkan status
            if (status === 'ONLINE') {
                statusBadge.className = 'badge bg-success';
            } else if (status === 'ONLINE STANDBY') {
                statusBadge.className = 'badge bg-warning';
            } else {
                statusBadge.className = 'badge bg-danger';
            }
        }

        // 3. Update Ping Indicator
        const indicator = row.querySelector('.ping-indicator');
        if (indicator) {
            
            if (status === 'ONLINE') {
                indicator.className = 'ping-indicator ping-active';
            } else if (status === 'ONLINE STANDBY') {
                indicator.className = 'ping-indicator ping-backup';
            } else {
                indicator.className = 'ping-indicator ping-inactive';
            }
        }

        // 4. [BARU] Update Tombol Recovery
        updateRecoveryButton(deviceId, status);
    }

    function connect() {
        stompClient.connect({}, function(frame) {
            console.log('TERHUBUNG KE WEBSOCKET', frame);
            
            // Subscribe dengan callback error
            const subscription = stompClient.subscribe('/topic/ping-updates', function(message) {
                
                try {
                    const data = JSON.parse(message.body);
                    
                    if (!data.deviceId || !data.pingTime) {
                        return;
                    }
                    
                    updateDeviceRow(data.deviceId, data.pingTime, data.status);
                } catch (e) {
                    console.error("ERROR PARSING:", e, "Raw message:", message.body);
                }
            }, {
                'id': 'ping-updates-subscription' // Beri ID untuk memudahkan debugging
            });
            
            console.log("Subscribed dengan ID:", subscription.id);
            
        }, function(error) {
            console.error('WEBSOCKET ERROR:', error);
            setTimeout(connect, 5000); // Reconnect setelah 5 detik
        });
    }

    // Handle sebelum unload
    window.addEventListener('beforeunload', function() {
        if (stompClient && stompClient.connected) {
            stompClient.disconnect();
        }
    });

    // Mulai koneksi
    connect();
});
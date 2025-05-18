// notifikasi-websocket.js

// Fungsi untuk menampilkan notifikasi real-time
function showRealTimeNotification(data) {
    const notificationDiv = document.createElement('div');
    notificationDiv.className = `alert alert-${data.type === 'DEVICE_DOWN' ? 'danger' : 'success'} alert-dismissible fade show notification-item`;
    notificationDiv.role = 'alert';
    
    const icon = data.type === 'DEVICE_DOWN' ? 'fa-exclamation-triangle' : 'fa-check-circle';
    const title = data.type === 'DEVICE_DOWN' ? 'Perangkat Down!' : 'Perangkat Pulih';
    
    notificationDiv.innerHTML = `
        <div class="d-flex">
            <div><i class="fas ${icon} me-2"></i></div>
            <div>
                <strong>${title}</strong><br>
                ${data.deviceName} (${data.ipAddress})<br>
                <small>${data.message}</small>
                <small class="d-block text-muted">${new Date(data.timestamp).toLocaleString()}</small>
            </div>
        </div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    const container = document.getElementById('realTimeNotifications');
    if (container) {
        container.appendChild(notificationDiv);
        
        // Auto-hide setelah 10 detik
        setTimeout(() => {
            $(notificationDiv).alert('close');
        }, 10000);
    }
}

// Koneksi WebSocket
function initializeWebSocket() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        
        stompClient.subscribe('/topic/notifications', function(notification) {
            const data = JSON.parse(notification.body);
            showRealTimeNotification(data);
        });
    });
}

// Inisialisasi ketika dokumen siap
$(document).ready(function() {
    initializeWebSocket();
});
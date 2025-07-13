document.addEventListener('DOMContentLoaded', function() {
    // Sidebar toggle
    $('#sidebarCollapse').on('click', function() {
        $('#sidebar').toggleClass('active');
        $('#content').toggleClass('active');
        
        // Force redraw untuk mencegah bug visual
        document.body.style.overflow = 'hidden';
        setTimeout(function() {
            document.body.style.overflow = '';
        }, 10);
    });

    // Matrix animation for each card
    document.querySelectorAll('.matrix-animation').forEach(canvas => {
    initWifiAnimation(canvas);
    });

    // Modal show event
    const deviceModal = new bootstrap.Modal(document.getElementById('deviceModal'));
    document.querySelectorAll('.device-card').forEach(card => {
        card.addEventListener('click', function() {
            try {
                // Ambil data JSON langsung dari atribut
                const jsonData = this.getAttribute('data-device-data');
                console.log("Raw JSON data:", jsonData); // Untuk debugging
                
                const deviceData = JSON.parse(jsonData);
                console.log("Parsed device data:", deviceData); // Untuk debugging
                
                // Populate modal
                document.getElementById('modalDeviceName').textContent = deviceData.deviceName || 'N/A';
                document.getElementById('modalIpAddress').textContent = deviceData.ipAddress || 'N/A';
                document.getElementById('modalMacAddress').textContent = deviceData.macAddress || 'N/A';
                document.getElementById('modalDeviceType').textContent = deviceData.deviceType || 'N/A';
                document.getElementById('modalLastChecked').textContent = deviceData.lastChecked || 'N/A';
                
                // Update status badge
                const statusBadge = document.getElementById('modalStatusBadge');
                if (deviceData.statusDevice) {
                    statusBadge.className = 'badge ' + 
                        (deviceData.statusDevice === 'ONLINE' ? 'badge-online' : 'badge-offline');
                    statusBadge.textContent = deviceData.statusDevice;
                }
                
                // Tampilkan modal
                const deviceModal = new bootstrap.Modal(document.getElementById('deviceModal'));
                deviceModal.show();
                
            } catch (e) {
                console.error("Error parsing device data:", e);
                alert("Gagal memuat data perangkat. Silakan coba lagi.");
            }
        });
    });
});

function initWifiAnimation(container) {
    
    container.style.minWidth = '40px';
    container.style.minHeight = '40px';
    
    const canvas = document.createElement('canvas');
    container.appendChild(canvas);

    function updateCanvasSize() {
        canvas.width = container.offsetWidth;
        canvas.height = container.offsetHeight;
    }
    updateCanvasSize();
    
    const ctx = canvas.getContext('2d');
    let centerX = canvas.width / 2;
    let centerY = canvas.height / 2;

    const maxBars = 4;
    let currentBars = 0;
    const barHeight = Math.min(canvas.width, canvas.height) * 0.15; // Ukuran relatif
    const dotRadius = Math.max(3, barHeight * 0.3); // Radius titik proporsional

    function drawSignal() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        ctx.lineWidth = Math.max(2, barHeight * 0.2);
        ctx.strokeStyle = '#000';
        ctx.fillStyle = '#000';

        // Gambar dari bar terkecil (bawah) ke terbesar
        for (let i = 0; i < currentBars; i++) {
            const radius = (i + 1) * barHeight;
            const startAngle = Math.PI * 1.2;
            const endAngle = Math.PI * 1.8;
            
            // Hitung posisi Y dasar untuk semua arc
            const baseY = centerY + (barHeight * maxBars * 0.7);

            ctx.beginPath();
            ctx.arc(centerX, baseY, radius, startAngle, endAngle);
            ctx.stroke();
        }

        // Gambar titik di bagian bawah (SELALU ADA)
        const dotY = centerY + (barHeight * maxBars * 0.7) + barHeight * 0.3;
        ctx.beginPath();
        ctx.arc(centerX, dotY, dotRadius, 0, Math.PI * 2);
        ctx.fill();
    }

    // Animasi lebih lambat (1 detik per tahap)
    let interval = setInterval(() => {
        currentBars = (currentBars % maxBars) + 1;
        drawSignal();
    }, 1000);

    // Handle resize
    window.addEventListener('resize', () => {
        updateCanvasSize();
        centerX = canvas.width / 2;
        centerY = canvas.height / 2;
        drawSignal();
    });

    drawSignal();
}
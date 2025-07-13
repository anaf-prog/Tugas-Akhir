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
        initMatrixAnimation(canvas);
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

function initMatrixAnimation(container) {
    // Create canvas element
    const canvas = document.createElement('canvas');
    container.appendChild(canvas);
    canvas.width = container.offsetWidth;
    canvas.height = container.offsetHeight;
    
    const ctx = canvas.getContext('2d');
    
    // Matrix characters - campuran angka dan biner
    const chars = "01abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    // Ukuran font dan kolom
    const fontSize = 14;
    const columns = canvas.width / fontSize;
    
    // Array untuk menyimpan posisi Y setiap kolom
    const drops = [];
    for (let i = 0; i < columns; i++) {
        drops[i] = Math.random() * -100; // Mulai dari atas dengan offset acak
    }
    
    // Warna hijau matrix
    const matrixGreen = '#20C20E';
    
    // Animation loop
    function draw() {
        // Set semi-transparent background untuk efek trail
        ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        
        // Set warna dan font
        ctx.fillStyle = matrixGreen;
        ctx.font = fontSize + 'px monospace';
        
        // Gambar karakter untuk setiap kolom
        for (let i = 0; i < drops.length; i++) {
            // Ambil karakter acak
            const text = chars[Math.floor(Math.random() * chars.length)];
            
            // Gambar karakter
            ctx.fillText(text, i * fontSize, drops[i] * fontSize);
            
            // Reset drop ke atas jika mencapai bawah atau secara acak
            if (drops[i] * fontSize > canvas.height && Math.random() > 0.975) {
                drops[i] = 0;
            }
            
            // Pindahkan ke bawah
            drops[i]++;
        }
    }
    
    // Jalankan animasi setiap 33ms (~30fps)
    setInterval(draw, 50);
    
    // Handle resize
    window.addEventListener('resize', function() {
        canvas.width = container.offsetWidth;
        canvas.height = container.offsetHeight;
    });
}
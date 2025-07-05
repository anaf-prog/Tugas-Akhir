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

    // Network animation for each card
    document.querySelectorAll('.network-animation').forEach(canvas => {
        initNetworkAnimation(canvas);
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

function initNetworkAnimation(container) {
    // Create canvas element
    const canvas = document.createElement('canvas');
    container.appendChild(canvas);
    canvas.width = container.offsetWidth;
    canvas.height = container.offsetHeight;
    
    const ctx = canvas.getContext('2d');
    const nodes = [];
    const connections = [];
    
    // Create nodes based on device type
    const deviceType = container.closest('.device-card').getAttribute('data-device-type');
    let nodeCount = 5;
    
    if (deviceType === 'ROUTER') nodeCount = 8;
    else if (deviceType === 'SWITCH') nodeCount = 6;
    else if (deviceType === 'SERVER') nodeCount = 4;
    
    // Create nodes
    for (let i = 0; i < nodeCount; i++) {
        nodes.push({
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            vx: Math.random() * 0.5 - 0.25,
            vy: Math.random() * 0.5 - 0.25,
            radius: 3 + Math.random() * 3
        });
    }
    
    // Create connections
    for (let i = 0; i < nodes.length; i++) {
        for (let j = i + 1; j < nodes.length; j++) {
            if (Math.random() > 0.7) {
                connections.push({ from: i, to: j });
            }
        }
    }
    
    // Animation loop
    function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Update nodes
        nodes.forEach(node => {
            node.x += node.vx;
            node.y += node.vy;
            
            // Bounce off walls
            if (node.x < 0 || node.x > canvas.width) node.vx *= -1;
            if (node.y < 0 || node.y > canvas.height) node.vy *= -1;
        });
        
        // Draw connections
        ctx.strokeStyle = 'rgba(100, 100, 255, 0.2)';
        ctx.lineWidth = 1;
        connections.forEach(conn => {
            const from = nodes[conn.from];
            const to = nodes[conn.to];
            ctx.beginPath();
            ctx.moveTo(from.x, from.y);
            ctx.lineTo(to.x, to.y);
            ctx.stroke();
        });
        
        // Draw nodes
        nodes.forEach(node => {
            ctx.beginPath();
            ctx.arc(node.x, node.y, node.radius, 0, Math.PI * 2);
            ctx.fillStyle = '#4285F4';
            ctx.fill();
        });
        
        requestAnimationFrame(animate);
    }
    
    animate();
    
    // Handle resize
    window.addEventListener('resize', function() {
        canvas.width = container.offsetWidth;
        canvas.height = container.offsetHeight;
    });
}
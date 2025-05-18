// Pindahkan inisialisasi chart ke fungsi terpisah
function initializeCharts() {
    // Pie Chart (Jenis Perangkat)
    const deviceTypeCtx = document.getElementById('deviceTypeChart');
    if (deviceTypeCtx) {
        const deviceTypeData = document.getElementById('deviceTypeData');
        const routerCount = parseInt(deviceTypeData?.dataset?.routerCount) || 0;
        const switchCount = parseInt(deviceTypeData?.dataset?.switchCount) || 0;
        const serverCount = parseInt(deviceTypeData?.dataset?.serverCount) || 0;

        new Chart(deviceTypeCtx, {
            type: 'pie',
            data: {
                labels: ['Router', 'Switch', 'Server'],
                datasets: [{
                    data: [routerCount, switchCount, serverCount],
                    backgroundColor: [
                        '#4e73df',
                        '#1cc88a',
                        '#36b9cc'
                    ],
                    hoverBorderColor: "rgba(234, 236, 244, 1)",
                }]
            },
            options: {
                responsive: true, // Tambahkan ini
                maintainAspectRatio: false, // Tambahkan ini
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

    // Line Chart (Status Perangkat)
    const statusCtx = document.getElementById('deviceStatusChart');
    if (statusCtx) {
        new Chart(statusCtx, {
            type: 'line',
            data: {
                labels: ['Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu'],
                datasets: [{
                    label: 'Perangkat Aktif',
                    data: [15, 18, 16, 17, 15, 19, 20],
                    backgroundColor: 'rgba(78, 115, 223, 0.05)',
                    borderColor: '#4e73df',
                    tension: 0.3
                }, {
                    label: 'Perangkat Down',
                    data: [2, 1, 3, 0, 1, 2, 0],
                    backgroundColor: 'rgba(231, 74, 59, 0.05)',
                    borderColor: '#e74a3b',
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
}

// Sidebar toggle
$(document).ready(function () {
    $('#sidebarCollapse').on('click', function () {
        $('#sidebar').toggleClass('active');
        $('#content').toggleClass('active');
    });

    // Inisialisasi chart
    initializeCharts();

    // Fungsi untuk update status perangkat
    function updateDeviceStatus() {
        if (typeof isDownDevicesModalOpen !== 'undefined' && isDownDevicesModalOpen) {
            return;
        }
        
        $.get(window.location.href, function(data) {
            var newDoc = $(data);

            // Update card dashboard
            $('.card-stats .card-body h2').each(function(index) {
                var newValue = newDoc.find('.card-stats .card-body h2').eq(index).text();
                $(this).text(newValue);
            });
            
            // Update tabel perangkat
            $('.table-responsive:first table tbody').html(
                newDoc.find('.table-responsive:first table tbody').html()
            );
            
            // Update tabel log ping
            $('.table-responsive:last table tbody').html(
                newDoc.find('.table-responsive:last table tbody').html()
            );
        });
    }
    
    // Jalankan setiap 30 detik
    setInterval(updateDeviceStatus, 5000);
    
    // Animasi ping indicator
    setInterval(function() {
        $('.ping-indicator').each(function() {
            var isOnline = $(this).hasClass('ping-active');
            var $light = $(this).find('.ping-light');
            
            $light.css('opacity', 0.3);
            
            if (isOnline) {
                setTimeout(function() {
                    $light.animate({opacity: 1}, 200);
                }, 200);
            }
        });
    }, 1000);
});
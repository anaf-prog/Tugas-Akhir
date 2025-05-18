// File: js/cardDashboard.js

// Variabel untuk menyimpan state modal
let isDownDevicesModalOpen = false;

function showDownDevices() {
    // Set state modal terbuka
    isDownDevicesModalOpen = true;
    
    // Tampilkan loading spinner
    $('#downDevicesList').html('<tr><td colspan="8" class="text-center"><div class="spinner-border text-warning" role="status"><span class="visually-hidden">Loading...</span></div></td></tr>');
    
    // Ambil data perangkat down dari server
    $.ajax({
        url: '/dashboard/devices/down',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
            renderDownDevices(data);
            
            // Tampilkan modal
            var modal = new bootstrap.Modal(document.getElementById('downDevicesModal'));
            modal.show();
        },
        error: function(xhr, status, error) {
            $('#downDevicesList').html(`
                <tr>
                    <td colspan="8" class="text-center text-danger">
                        Gagal memuat data: ${xhr.statusText}
                    </td>
                </tr>
            `);
            console.error("Error:", error);
            
            // Tetap tampilkan modal meski error
            var modal = new bootstrap.Modal(document.getElementById('downDevicesModal'));
            modal.show();
        }
    });
}

// Fungsi untuk render data perangkat down
function renderDownDevices(data) {
    var tableBody = $('#downDevicesList');
    tableBody.empty();
    
    if (!data || data.length === 0) {
        tableBody.append('<tr><td colspan="8" class="text-center">Tidak ada perangkat down</td></tr>');
    } else {
        data.forEach(function(device) {
            // Format tanggal terakhir checked
            const lastChecked = device.last_checked ? 
                new Date(device.last_checked).toLocaleString() : '-';
            
            tableBody.append(`
                <tr>
                    <td>${device.device_name || '-'}</td>
                    <td>${device.ip_address || '-'}</td>
                    <td>${device.device_type || '-'}</td>
                    <td>${device.location?.location_name || '-'}</td>
                    <td>${device.location?.room || '-'}</td>
                    <td>${device.location?.floor || '-'}</td>
                    <td><span class="badge bg-danger">${device.status_device || 'OFFLINE'}</span></td>
                    <td>${lastChecked}</td>
                </tr>
            `);
        });
    }
}

// Inisialisasi card yang bisa diklik
$(document).ready(function() {
    $('.clickable-card').css('cursor', 'pointer');
    $('.clickable-card').hover(
        function() { 
            $(this).addClass('shadow-lg').css('transform', 'translateY(-2px)'); 
        },
        function() { 
            $(this).removeClass('shadow-lg').css('transform', 'none'); 
        }
    );
    
    // Event listener untuk modal
    $('#downDevicesModal').on('hidden.bs.modal', function () {
        isDownDevicesModalOpen = false;
    });
});
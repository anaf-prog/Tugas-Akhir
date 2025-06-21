// Variabel untuk menyimpan interval ID
let countdownInterval = null;

// Fungsi untuk mengupdate semua countdown
function updateAllCountdowns() {
    let allCompleted = true;
    let activeCountdowns = 0;
    
    console.log("--- Running updateAllCountdowns ---");
    
    $('.scheduled-time').each(function(index) {
        const $container = $(this);
        const scheduledTimeStr = $container.attr('data-scheduled');
        const countdownElement = $container.find('.countdown');
        
        console.log(`Processing element #${index}`, {
            scheduledTimeStr: scheduledTimeStr,
            element: $container
        });
        
        if (!scheduledTimeStr || scheduledTimeStr === 'null') {
            console.log(`Skipping element #${index} - no scheduled time`);
            return;
        }
        
        activeCountdowns++;
        
        try {
            const scheduledDate = new Date(scheduledTimeStr);
            const now = new Date();
            const diffMs = scheduledDate - now;
            
            console.log(`Time comparison for element #${index}:`, {
                scheduled: scheduledDate,
                now: now,
                diffMs: diffMs
            });
            
            if (isNaN(diffMs)) {
                console.error(`Invalid date calculation for element #${index}`);
                return;
            }
            
            if (diffMs <= 0) {
                // Waktu maintenance sudah lewat
                console.log(`Maintenance time reached for element #${index}`);
                countdownElement.text('Waktu maintenance telah tiba');
                countdownElement.removeClass('soon').addClass('completed');
                return;
            }
            
            // Jika masih ada yang belum selesai
            allCompleted = false;
            
            // Hitung sisa waktu
            const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
            const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
            const diffSeconds = Math.floor((diffMs % (1000 * 60)) / 1000);
            
            // Format tampilan
            let countdownText = '';
            if (diffDays > 0) {
                countdownText += diffDays + 'd ';
            }
            countdownText += diffHours.toString().padStart(2, '0') + ':' + 
                             diffMinutes.toString().padStart(2, '0') + ':' + 
                             diffSeconds.toString().padStart(2, '0');
            
            // Update element
            countdownElement.text(countdownText);
            
            // Tambah class soon jika kurang dari 1 jam
            if (diffMs < 3600000) {
                countdownElement.addClass('soon');
            } else {
                countdownElement.removeClass('soon');
            }
            
        } catch (e) {
            console.error(`Error processing countdown for element #${index}:`, e);
        }
    });
    
    console.log(`Active countdowns: ${activeCountdowns}, allCompleted: ${allCompleted}`);
    
    // Hentikan interval jika semua countdown sudah selesai
    if (allCompleted && countdownInterval) {
        console.log('All countdowns completed - stopping interval');
        clearInterval(countdownInterval);
        countdownInterval = null;
    }
}

// Fungsi untuk inisialisasi countdown
function initCountdown() {
    console.log('Initializing countdown...');
    
    // Hentikan interval yang sudah ada
    if (countdownInterval) {
        console.log('Clearing existing interval');
        clearInterval(countdownInterval);
    }
    
    // Jalankan sekali untuk inisialisasi
    updateAllCountdowns();
    
    // Mulai interval baru
    countdownInterval = setInterval(function() {
        console.log('Interval tick - updating countdowns');
        updateAllCountdowns();
    }, 1000);
    
    console.log('Countdown initialized');
}

// Inisialisasi pertama kali
$(document).ready(function() {
    console.log('Document ready - starting countdown');
    initCountdown();
    
    // Jika halaman menggunakan DataTables, inisialisasi ulang saat draw
    if ($.fn.DataTable) {
        console.log('DataTables detected - setting up draw event');
        $('#maintenanceTable').on('draw.dt', function() {
            console.log('DataTables draw event - reinitializing countdown');
            initCountdown();
        });
    }
});

// Fungsi untuk disable device (pastikan ini ada)
function disableDevice(deviceId) {
    console.log(`Attempting to disable device ${deviceId}`);
    $.ajax({
        url: `/api/maintenance/device/${deviceId}/disable`,
        type: 'PUT',
        success: function(response) {
            console.log(`Device ${deviceId} disabled successfully`, response);
            showAlert('success', `Perangkat ${deviceId} berhasil dinonaktifkan`);
            setTimeout(() => location.reload(), 2000);
        },
        error: function(xhr) {
            console.error(`Failed to disable device ${deviceId}`, xhr.responseJSON);
            showAlert('danger', 'Gagal menonaktifkan perangkat');
        }
    });
}

// Fungsi untuk mengecek dan menonaktifkan perangkat
function checkAndDisableCompleted() {
    console.log('Checking for completed maintenance...');
    $('.scheduled-time').each(function() {
        const $container = $(this);
        const scheduledTimeStr = $container.attr('data-scheduled');
        const countdownElement = $container.find('.countdown');
        
        if (!scheduledTimeStr) return;
        
        try {
            const scheduledDate = new Date(scheduledTimeStr);
            const now = new Date();
            const diffMs = scheduledDate - now;
            
            if (diffMs <= 0 && !countdownElement.hasClass('completed')) {
                const deviceId = $container.closest('tr').data('device-id');
                console.log(`Maintenance time reached for device ${deviceId}`);
                if (deviceId) {
                    disableDevice(deviceId);
                }
                countdownElement.addClass('completed');
            }
        } catch (e) {
            console.error('Error checking maintenance completion:', e);
        }
    });
}

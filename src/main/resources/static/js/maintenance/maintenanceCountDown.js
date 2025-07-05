// Variabel untuk menyimpan interval ID
let countdownInterval = null;

// Fungsi untuk mengupdate semua countdown
function updateAllCountdowns() {
    let allCompleted = true;
    
    console.log("--- Running updateAllCountdowns ---");
    
    // Update countdown untuk waktu mulai maintenance
    $('.scheduled-time').each(function(index) {
        const $container = $(this);
        const scheduledTimeStr = $container.attr('data-scheduled');
        const countdownElement = $container.find('.countdown');
        const deviceId = $container.closest('tr').data('device-id');
        const completionTimeStr = $container.closest('tr').find('.repair-completion').attr('data-completion');
        
        console.log(`Processing scheduled time for element #${index}`, {
            scheduledTimeStr: scheduledTimeStr,
            element: $container
        });
        
        if (!scheduledTimeStr || scheduledTimeStr === 'null') {
            console.log(`Skipping element #${index} - no scheduled time`);
            return;
        }
        
        try {
            const scheduledDate = new Date(scheduledTimeStr);
            const now = new Date();
            const diffMs = scheduledDate - now;

            // Cek apakah maintenance sudah selesai
            if (completionTimeStr && completionTimeStr !== 'null') {
                const completionDate = new Date(completionTimeStr);
                if (now >= completionDate) {
                    // Jika maintenance sudah selesai, hilangkan tulisan "Proses maintenance..."
                    console.log(`Maintenance completed for element #${index} - hiding process message`);
                    countdownElement.text('');
                    return;
                }
            }
            
            if (isNaN(diffMs)) {
                console.error(`Invalid date calculation for element #${index}`);
                return;
            }
            
            if (diffMs <= 0) {
                // Waktu maintenance sudah lewat
                console.log(`Maintenance time reached for element #${index}`);
                countdownElement.text('Proses maintenance...');
                countdownElement.removeClass('soon').addClass('completed');
                
                // Nonaktifkan perangkat jika diperlukan
                if (deviceId && !countdownElement.hasClass('disabled')) {
                    disableDevice(deviceId);
                    countdownElement.addClass('disabled');
                }
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
            console.error(`Error processing scheduled time for element #${index}:`, e);
        }
    });
    
    // Update countdown untuk waktu selesai perbaikan
    $('.repair-completion').each(function(index) {
        const $container = $(this);
        const completionTimeStr = $container.attr('data-completion');
        const countdownElement = $container.find('.completion-countdown');
        const deviceId = $container.closest('tr').data('device-id');
        
        console.log(`Processing repair completion for element #${index}`, {
            completionTimeStr: completionTimeStr,
            element: $container
        });
        
        if (!completionTimeStr || completionTimeStr === 'null') {
            console.log(`Skipping element #${index} - no completion time`);
            return;
        }
        
        try {
            const completionDate = new Date(completionTimeStr);
            const now = new Date();
            const diffMs = completionDate - now;
            
            if (isNaN(diffMs)) {
                console.error(`Invalid date calculation for element #${index}`);
                return;
            }
            
            if (diffMs <= 0) {
                // Waktu perbaikan selesai
                console.log(`Repair completion time reached for element #${index}`);
                countdownElement.text('Perbaikan selesai');
                countdownElement.removeClass('soon').addClass('completed');
                
                // Aktifkan perangkat jika diperlukan
                if (deviceId && !countdownElement.hasClass('enabled')) {
                    checkAndEnableDevice(deviceId);
                    countdownElement.addClass('enabled');
                }
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
            console.error(`Error processing repair completion for element #${index}:`, e);
        }
    });
    
    // Hentikan interval jika semua countdown sudah selesai
    if (allCompleted && countdownInterval) {
        console.log('All countdowns completed - stopping interval');
        clearInterval(countdownInterval);
        countdownInterval = null;
    }
}

// Fungsi untuk mengecek dan mengaktifkan perangkat
function checkAndEnableDevice(deviceId) {
    console.log(`Checking if device ${deviceId} needs to be enabled`);
    
    $.ajax({
        url: `/api/devices/${deviceId}/status`,
        type: 'GET',
        success: function(status) {
            if (status === 'OFFLINE') {
                console.log(`Device ${deviceId} is offline - attempting to enable`);
                $.ajax({
                    url: `/api/maintenance/device/${deviceId}/enable`,
                    type: 'PUT',
                    success: function(response) {
                        console.log(`Device ${deviceId} enabled successfully`, response);
                        showAlert('success', `Perangkat ${response.deviceId} berhasil diaktifkan kembali`);
                    },
                    error: function(xhr) {
                        console.error(`Failed to enable device ${deviceId}`, xhr.responseJSON);
                        showAlert('danger', `Gagal mengaktifkan perangkat ${deviceId}`);
                    }
                });
            }
        },
        error: function(xhr) {
            console.error(`Failed to check status for device ${deviceId}`, xhr.responseJSON);
        }
    });
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

// Fungsi untuk disable device
function disableDevice(deviceId) {
    console.log(`Attempting to disable device ${deviceId}`);
    $.ajax({
        url: `/api/maintenance/device/${deviceId}/disable`,
        type: 'PUT',
        success: function(response) {
            console.log(`Device ${deviceId} disabled successfully`, response);
            showAlert('success', `Perangkat ${response.deviceId} berhasil dinonaktifkan`);
        },
        error: function(xhr) {
            console.error(`Failed to disable device ${deviceId}`, xhr.responseJSON);
            showAlert('danger', 'Gagal menonaktifkan perangkat');
        }
    });
}
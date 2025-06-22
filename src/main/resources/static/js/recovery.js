/**
 * recovery.js - Handle device recovery functionality dengan Modal Notifikasi (Updated)
 */

// Fungsi untuk menampilkan modal notifikasi
function showRecoveryModal(type, message) {
    const modal = $('#recoveryModal');
    const modalTitle = $('#recoveryModalTitle');
    const modalBody = $('#recoveryModalBody');
    
    // Set icon berdasarkan type
    const icon = type === 'success' 
        ? '<i class="fas fa-check-circle text-success me-2"></i>' 
        : '<i class="fas fa-exclamation-triangle text-danger me-2"></i>';
    
    // Set warna header modal
    modal.find('.modal-header').removeClass('bg-success bg-danger');
    if (type === 'success') {
        modal.find('.modal-header').addClass('bg-success text-white');
    } else {
        modal.find('.modal-header').addClass('bg-danger text-white');
    }
    
    modalTitle.html(icon + (type === 'success' ? 'Berhasil' : 'Gagal'));
    modalBody.html(message);
    
    // Tampilkan modal
    const modalInstance = new bootstrap.Modal(modal[0]);
    modalInstance.show();
    
    // Auto close setelah 3 detik untuk success
    if (type === 'success') {
        setTimeout(() => {
            modalInstance.hide();
            // Refresh data setelah modal ditutup
            updateDeviceStatus();
        }, 3000);
    }
}

// Fungsi untuk handle recovery device
function setupRecoveryButtons() {
    $(document).off('click', '.recovery-btn').on('click', '.recovery-btn', function(e) {
        e.preventDefault();
        
        const button = $(this);
        const row = button.closest('tr');
        const isOnline = row.find('.badge').hasClass('bg-success');
        
        // Jika perangkat online, hentikan proses
        if (isOnline) {
            return false;
        }
        
        const ipAddress = button.data('ip');
        const deviceId = row.data('device-id');
        const deviceName = row.find('td:first').text();
        
        console.log('Attempting recovery for:', deviceName, 'IP:', ipAddress);
        
        // Tampilkan loading state
        button.prop('disabled', true);
        button.html('<i class="fas fa-spinner fa-spin"></i> Memproses...');
        
        // Kirim request recovery ke API
        $.ajax({
            url: '/api/simulation/recover',
            method: 'POST',
            data: { ipAddress: ipAddress },
            success: function(response) {
                console.log('Recovery response:', response);
                
                if (response.success) {
                    // Update UI secara langsung tanpa menunggu refresh
                    row.find('.badge')
                        .removeClass('bg-danger')
                        .addClass('bg-success')
                        .text('ONLINE');
                    
                    row.find('.ping-indicator')
                        .removeClass('ping-inactive')
                        .addClass('ping-active');
                    
                    button.removeClass('btn-success')
                          .addClass('btn-secondary disabled');
                    
                    showRecoveryModal('success', response.message);
                } else {
                    showRecoveryModal('danger', response.message || 'Recovery gagal');
                }
            },
            error: function(xhr) {
                console.error('Recovery error:', xhr.responseText);
                const errorMsg = xhr.responseJSON?.message || 
                               'Gagal terhubung ke server. Silakan coba lagi.';
                showRecoveryModal('danger', errorMsg);
            },
            complete: function() {
                button.prop('disabled', false)
                      .html('<i class="fas fa-sync-alt"></i> Recovery');
            }
        });
    });
}

// Fungsi untuk update status perangkat (optional)
function updateDeviceStatus() {
    console.log('Updating device status...');
    $.get(window.location.href, function(data) {
        const newContent = $(data).find('.table-responsive:last').html();
        $('.table-responsive:last').html(newContent);
        setupRecoveryButtons(); // Re-init buttons setelah update
    });
}

// Inisialisasi dengan error handling
$(document).ready(function() {
    try {
        console.log('Recovery JS initialized');
        setupRecoveryButtons();
        
        // MutationObserver untuk handle dynamic content
        if (typeof MutationObserver !== 'undefined') {
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.addedNodes.length) {
                        $(mutation.addedNodes).find('.recovery-btn').each(function() {
                            if (!$(this).hasClass('initialized')) {
                                setupRecoveryButtons();
                                $(this).addClass('initialized');
                            }
                        });
                    }
                });
            });
            
            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
        }
    } catch (error) {
        console.error('Error initializing recovery JS:', error);
    }
});
/**
 * recovery.js - Handle device recovery functionality dengan Modal Notifikasi
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
        }, 5000);
    }
}

// Fungsi untuk handle recovery device
function setupRecoveryButtons() {
    $(document).off('click', '.recovery-btn').on('click', '.recovery-btn', function(e) {
        // Cek status perangkat dari badge status, bukan dari data attribute
        const isOnline = $(this).closest('tr').find('.badge').hasClass('bg-success');
        
        // Jika perangkat online, hentikan proses
        if (isOnline) {
            e.preventDefault();
            e.stopPropagation();
            return false;
        }
        
        const ipAddress = $(this).data('ip');
        const button = $(this);
        
        console.log('Attempting recovery for IP:', ipAddress);
        
        // Tampilkan loading state
        button.prop('disabled', true);
        button.html('<i class="fas fa-spinner fa-spin"></i> Memproses...');
        
        // Kirim request recovery ke API
        $.ajax({
            url: '/api/simulation/recover',
            method: 'POST',
            data: { ipAddress: ipAddress },
            success: function(response) {
                console.log('Recovery successful:', response);
                showRecoveryModal('success', response.message);
                
                // Hanya reset tombol, biarkan monitoring scheduled update status
                button.prop('disabled', false)
                      .html('<i class="fas fa-sync-alt"></i> Recovery');
                
                // Verifikasi dengan data terbaru dari server
                updateDeviceStatus();
            },
            error: function(xhr) {
                console.error('Recovery failed:', xhr.responseText);
                const errorMsg = xhr.responseJSON?.message || xhr.responseText || 'Terjadi kesalahan';
                
                showRecoveryModal('danger', 'Gagal melakukan recovery: ' + errorMsg);
                button.prop('disabled', false)
                      .html('<i class="fas fa-sync-alt"></i> Recovery');
            }
        });
    });
}

// Fungsi untuk update status perangkat
function updateDeviceStatus() {
    $.get('/dashboard?partial=true', function(data) {
        const newContent = $(data).find('.table-responsive:last').html();
        $('.table-responsive:last').html(newContent);
    });
}

// Inisialisasi
$(document).ready(function() {
    console.log('Recovery JS initialized');
    setupRecoveryButtons();
    
    // MutationObserver untuk handle dynamic content
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
});
// maintenanceDevice.js

let deviceToDisableId = null;

function showAlert(type, message) {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    $('.container.mt-3').html(alertHtml);
    setTimeout(() => $('.alert').alert('close'), 5000);
}

// Handle tombol nonaktifkan perangkat
$(document).on('click', '.disable-device-btn', function() {
    const deviceId = $(this).data('device-id');
    if (!deviceId) {
        showAlert('danger', 'Perangkat tidak valid atau tidak ditemukan');
        return;
    }
    
    deviceToDisableId = deviceId; // Simpan ID perangkat
    $('#disableDeviceModal').modal('show'); // Tampilkan modal
});

// Handle tombol konfirmasi nonaktifkan
$('#confirmDisableBtn').on('click', function() {
    if (deviceToDisableId) {
        const $button = $(this); // Simpan referensi tombol
        
        $.ajax({
            url: `/api/maintenance/device/${deviceToDisableId}/disable`,
            type: 'PUT',
            beforeSend: function() {
                // Tampilkan loading state
                $button.prop('disabled', true);
                $button.html(`
                    <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                    Memproses...
                `);
            },
            success: function(response) {
                $('#disableDeviceModal').modal('hide');
                showAlert('success', response.message);
                setTimeout(() => location.reload(), 1500);
            },
            error: function(xhr) {
                console.error("Error response:", xhr);
                let errorMsg = 'Gagal menonaktifkan perangkat';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                }
                showAlert('danger', errorMsg);
                $button.prop('disabled', false);
                $button.html('Nonaktifkan');
            }
        });
    }
});

// Reset deviceToDisableId ketika modal ditutup
$('#disableDeviceModal').on('hidden.bs.modal', function() {
    deviceToDisableId = null;
    $('#confirmDisableBtn').prop('disabled', false).html('Nonaktifkan');
});
/**
 * Fungsi untuk mengatur notifikasi yang hilang otomatis
 * @param {number} delay - Waktu dalam milidetik sebelum notifikasi hilang (default: 5000ms / 5 detik)
 */
function setupAutoDismissAlerts(delay = 10000) {
    // Untuk notifikasi yang sudah ada di HTML (dari flash attributes)
    document.addEventListener('DOMContentLoaded', function() {
        const alerts = document.querySelectorAll('.alert-dismissible');
        
        alerts.forEach(alert => {
            // Set timeout untuk auto-hide
            const timeout = setTimeout(() => {
                dismissAlert(alert);
            }, delay);
            
            // Jika user mengklik close manual, batalkan timeout
            const closeButton = alert.querySelector('.btn-close');
            if (closeButton) {
                closeButton.addEventListener('click', () => {
                    clearTimeout(timeout);
                });
            }
        });
    });
    
    /**
     * Animasi untuk menghilangkan alert
     * @param {HTMLElement} alert - Elemen alert yang akan dihilangkan
     */
    function dismissAlert(alert) {
        alert.classList.remove('show');
        alert.classList.add('fade');
        
        setTimeout(() => {
            alert.remove();
        }, 1500);
    }
}

// Fungsi untuk menampilkan notifikasi dinamis
function showDynamicAlert(message, type = 'success') {
    const alertContainer = document.querySelector('.container.mt-3');
    
    // Buat elemen alert baru
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Tambahkan ke container
    alertContainer.prepend(alertDiv);
    
    // Aktifkan animasi
    setTimeout(() => {
        alertDiv.classList.add('show');
    }, 10);
    
    // Set timeout untuk auto-dismiss
    setTimeout(() => {
        dismissAlert(alertDiv);
    }, 10000);
    
    // Handle close button
    const closeBtn = alertDiv.querySelector('.btn-close');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            dismissAlert(alertDiv);
        });
    }
}

// Panggil fungsi dengan default 5 detik
setupAutoDismissAlerts();

// Export fungsi untuk digunakan di file lain
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { showDynamicAlert };
} else {
    window.showAlert = showDynamicAlert;
}
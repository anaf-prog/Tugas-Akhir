// /js/notifikasi.js

/**
 * Fungsi untuk mengatur notifikasi yang hilang otomatis
 * @param {number} delay - Waktu dalam milidetik sebelum notifikasi hilang (default: 5000ms / 5 detik)
 */
function setupAutoDismissAlerts(delay = 5000) {
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
        }, 150);
    }
}

// Panggil fungsi dengan default 5 detik
setupAutoDismissAlerts();
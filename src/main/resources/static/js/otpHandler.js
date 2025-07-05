/**
 * Kelas untuk menangani logika terkait OTP
 */
class OtpHandler {
    constructor() {
        // Inisialisasi modal
        this.modal = new bootstrap.Modal(document.getElementById('otpModal'));
        this.modalBody = document.getElementById('otpModalBody');
        
        // Bind event untuk tombol kirim ulang OTP
        document.getElementById('resendOtpLink').addEventListener('click', (e) => {
            e.preventDefault();
            this.resendOtp();
        });
    }
    
    /**
     * Menampilkan pesan di modal
     * @param {string} message - Pesan yang akan ditampilkan
     * @param {boolean} isError - Apakah pesan merupakan error
     */
    showMessage(message, isError = false) {
        this.modalBody.textContent = message;
        
        // Jika error, ubah warna header modal
        const modalHeader = document.querySelector('#otpModal .modal-header');
        if (isError) {
            modalHeader.classList.add('bg-danger', 'text-white');
        } else {
            modalHeader.classList.remove('bg-danger', 'text-white');
        }
        
        this.modal.show();
    }
    
    /**
     * Fungsi untuk mengirim ulang OTP
     */
    resendOtp() {
        // Tampilkan pesan loading
        this.showMessage('Sedang mengirim OTP...');
        
        // Kirim request untuk mengirim ulang OTP
        fetch('/resend-otp', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.showMessage('OTP baru telah dikirim ke email Anda');
            } else {
                this.showMessage('Gagal mengirim ulang OTP. Silakan coba kembali.', true);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            this.showMessage('Terjadi kesalahan saat mengirim ulang OTP', true);
        });
    }
}

// Inisialisasi kelas OtpHandler ketika dokumen siap
document.addEventListener('DOMContentLoaded', () => {
    new OtpHandler();
});
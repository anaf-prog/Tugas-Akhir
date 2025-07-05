function loadUtilityFunctions() {
    // Fungsi untuk menampilkan notifikasi
    window.showAlert = function(type, message) {
        const alertHtml = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        
        // Tempatkan notifikasi di atas konten utama
        $('.container.mt-3').html(alertHtml);
        
        // Auto close setelah 5 detik
        setTimeout(() => {
            $('.alert').alert('close');
        }, 5000);
    };
    
    // Format datetime untuk input datetime-local
    window.formatDateTimeForInput = function(dateTimeStr) {
        const date = new Date(dateTimeStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    };
}
$(document).ready(function() {
    // Sidebar toggle
    $('#sidebarCollapse').on('click', function() {
        $('#sidebar').toggleClass('active');
        $('#content').toggleClass('active');
        
        // Force redraw untuk mencegah bug visual
        document.body.style.overflow = 'hidden';
        setTimeout(function() {
            document.body.style.overflow = '';
        }, 10);
    });
    
    // Inisialisasi DataTable (hanya sekali)
    const reportsTable = initDataTable();
    
    // Inisialisasi flatpickr
    initDatePickers();
    
    // Setup form validation
    setupFormValidation(reportsTable);

    // Setup filter functionality
    setupFilters(reportsTable);
});

function showAlert(message, type) {
    // Hapus alert sebelumnya
    $('.alert-dynamic').remove();
    
    // Buat alert baru
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show alert-dynamic" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    // Tambahkan alert ke container notifikasi
    $('.container.mt-3').prepend(alertHtml);
    
    // Hilangkan alert setelah 5 detik
    setTimeout(() => {
        $('.alert-dynamic').alert('close');
    }, 10000);
}
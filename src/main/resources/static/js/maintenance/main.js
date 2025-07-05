$(document).ready(function() {
    // Inisialisasi DataTable
    initializeDataTable();
    
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
    
    // Load modul-modul lainnya
    loadAddMaintenanceModule();
    loadEditMaintenanceModule();
    loadDeleteModule();
    loadUtilityFunctions();
});
$(document).ready(function() {
    // Inisialisasi DataTable
    initializeDataTable();
    
    // Toggle sidebar
    $('#sidebarCollapse').on('click', function() {
        $('#sidebar').toggleClass('active');
    });
    
    // Load modul-modul lainnya
    loadAddMaintenanceModule();
    loadEditMaintenanceModule();
    loadDeleteModule();
    loadUtilityFunctions();
});
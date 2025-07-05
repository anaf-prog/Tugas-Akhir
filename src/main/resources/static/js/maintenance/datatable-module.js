function initializeDataTable() {
    // Hapus DataTable jika sudah ada
    if ($.fn.DataTable.isDataTable('#maintenanceTable')) {
        $('#maintenanceTable').DataTable().destroy();
    }

    // Inisialisasi DataTable hanya untuk sorting (tanpa search dan paging)
    $('#maintenanceTable').DataTable({
        searching: false, 
        paging: false,   
        info: false,     
        ordering: false,  
        dom: 't'         
    });
}

function handleSearch() {
    const searchInput = $('#manualSearchInput');
    const searchButton = $('#searchButton');

    // Handle klik tombol search
    searchButton.on('click', function() {
        performSearch();
    });

    // Handle tekan Enter di input search
    searchInput.on('keyup', function(e) {
        if (e.key === 'Enter') {
            performSearch();
        }
    });
}

function performSearch() {
    const searchValue = $('#manualSearchInput').val();
    const currentUrl = new URL(window.location.href);
    
    if (searchValue.trim() === '') {
        currentUrl.searchParams.delete('search');
    } else {
        currentUrl.searchParams.set('search', encodeURIComponent(searchValue));
    }
    
    // Reset ke halaman pertama saat melakukan pencarian baru
    currentUrl.searchParams.set('page', '0');
    window.location.href = currentUrl.toString();
}

function handlePagination() {
    $(document).on('click', '.page-link', function(e) {
        e.preventDefault();
        const url = $(this).attr('href');
        const searchValue = $('#manualSearchInput').val();
        
        const newUrl = new URL(url, window.location.origin);
        if (searchValue) {
            newUrl.searchParams.set('search', encodeURIComponent(searchValue));
        } else {
            newUrl.searchParams.delete('search');
        }
        
        window.location.href = newUrl.toString();
    });
}

$(document).ready(function() {
    initializeDataTable();
    handleSearch();
    handlePagination();
    
    // Isi nilai search dari URL jika ada
    const urlParams = new URLSearchParams(window.location.search);
    const searchParam = urlParams.get('search');
    if (searchParam) {
        $('#manualSearchInput').val(decodeURIComponent(searchParam));
    }
});
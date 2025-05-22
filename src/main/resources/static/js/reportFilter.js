function setupFilters(table) {
    if (!table) return;
    
    // Fungsi untuk apply filter
    function applyFilters() {
        const deviceFilter = $('#deviceFilter').val().toLowerCase();
        const technicianFilter = $('#technicianFilter').val().toLowerCase();
        const startDate = $('#startDateFilter').val();
        const endDate = $('#endDateFilter').val();
        
        table.rows().every(function() {
            const row = this.node();
            const deviceName = $(row).find('td:eq(1)').text().toLowerCase();
            const technician = $(row).find('td:eq(4)').text().toLowerCase();
            const issueDateStr = $(row).find('td:eq(2)').text();
            const repairDateStr = $(row).find('td:eq(5)').text();
            
            // Parse tanggal dari format "dd-MM-yyyy HH:mm"
            const issueDateParts = issueDateStr.split(' ')[0].split('-');
            const issueDate = issueDateParts.length === 3 ? 
                new Date(issueDateParts[2], issueDateParts[1]-1, issueDateParts[0]) : null;
            
            const repairDateParts = repairDateStr.split(' ')[0].split('-');
            const repairDate = repairDateParts.length === 3 ? 
                new Date(repairDateParts[2], repairDateParts[1]-1, repairDateParts[0]) : null;
            
            // Filter perangkat
            const deviceMatch = !deviceFilter || deviceName.includes(deviceFilter);
            
            // Filter teknisi
            const technicianMatch = !technicianFilter || technician.includes(technicianFilter);
            
            // Filter tanggal
            let dateMatch = true;
            if (startDate || endDate) {
                const startParts = startDate ? startDate.split('-') : null;
                const endParts = endDate ? endDate.split('-') : null;
                
                const start = startParts ? new Date(startParts[2], startParts[1]-1, startParts[0]) : null;
                const end = endParts ? new Date(endParts[2], endParts[1]-1, endParts[0]) : null;
                
                dateMatch = false;
                
                // Cek apakah issue date atau repair date berada dalam range
                if (issueDate) {
                    if ((!start || issueDate >= start) && (!end || issueDate <= end)) {
                        dateMatch = true;
                    }
                }
                
                if (!dateMatch && repairDate) {
                    if ((!start || repairDate >= start) && (!end || repairDate <= end)) {
                        dateMatch = true;
                    }
                }
            }
            
            // Tampilkan/sembunyikan baris berdasarkan filter
            if (deviceMatch && technicianMatch && dateMatch) {
                $(row).show();
            } else {
                $(row).hide();
            }
        });
    }
    
    // Event handler untuk tombol apply filter
    $('#applyFilter').on('click', function() {
        applyFilters();
    });
    
    // Event handler untuk tombol reset filter
    $('#resetFilterLaporan').on('click', function() {
        $('#deviceFilter').val('');
        $('#technicianFilter').val('');
        $('#startDateFilter').val('');
        $('#endDateFilter').val('');
        table.rows().every(function() {
            $(this.node()).show();
        });
    });
    
    // Enable filter saat tekan enter di input teknisi
    $('#technicianFilter').on('keyup', function(e) {
        if (e.key === 'Enter') {
            applyFilters();
        }
    });
}
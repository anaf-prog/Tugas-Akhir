jQuery.extend(jQuery.fn.dataTableExt.oSort, {
    "date-eu-pre": function(a) {
        if (a == '-' || a == '') return 0;
        
        // Handle format: "dd-MM-yyyy HH:mm:ss"
        var dateParts = a.split(/[- :]/);
        // dateParts[0] = hari, [1] = bulan, [2] = tahun, [3] = jam, dst
        return Date.UTC(dateParts[2], dateParts[1]-1, dateParts[0], dateParts[3], dateParts[4], dateParts[5]);
    },
    "date-eu-asc": function(a, b) { return a - b; },
    "date-eu-desc": function(a, b) { return b - a; }
});

$(document).ready(function() {
    // Inisialisasi DataTable
    var table = $('#monitoringLogsTable').DataTable({
        "language": {
            "lengthMenu": "",
            "zeroRecords": "Data tidak ditemukan",
            "info": "",
            "infoEmpty": "Tidak ada data tersedia",
            "infoFiltered": "",
            "paginate": {
                "first": "Pertama",
                "last": "Terakhir",
                "next": "Selanjutnya",
                "previous": "Sebelumnya"
            }
        },
        "pageLength": 10,
        "order": [[5, "desc"]],
        "columnDefs": [
            { "targets": [0], "visible": false },
            { "orderable": false, "targets": [1, 6] },
            { 
                "type": "date-eu",
                "targets": 5        
            }
        ],
        "dom": '<"top"<"row"<"col-md-6"l><"col-md-6"f>>>' +
               'rt' +
               '<"bottom"<"row"<"col-md-12"p>>>', // Perubahan disini
        "initComplete": function() {
            // Pindahkan pagination ke container baru di tengah
            var pagination = $('.dataTables_paginate');
            pagination.addClass('text-center');
            pagination.wrap('<div class="text-center"></div>');
            
            // Sembunyikan search box bawaan DataTables
            $('.dataTables_filter').hide();
        }
    });

    // Filter berdasarkan nama perangkat
    $('#deviceFilterLog').on('change', function() {
        var deviceName = $(this).val();
        if (deviceName) {
            table.column(0).search('^' + deviceName + '$', true, false).draw();
        } else {
            table.column(0).search('').draw();
        }
    });

    // Filter berdasarkan tanggal (versi lebih akurat)
    $('#dateFilter').on('change', function() {
        var selectedDate = $(this).val();
        if (selectedDate) {
            // Konversi dari yyyy-mm-dd ke dd-mm-yyyy untuk pencarian
            var formattedDate = selectedDate.split('-').reverse().join('-');
            table.column(5).search(formattedDate).draw(); // Kolom 5 = Waktu Monitoring
        } else {
            table.column(5).search('').draw();
        }
    });

    // Tombol reset
    $('#resetFilter').on('click', function() {
        $('#deviceFilterLog').val('');
        $('#dateFilter').val('');
        table.search('').columns().search('').draw();
    });
});
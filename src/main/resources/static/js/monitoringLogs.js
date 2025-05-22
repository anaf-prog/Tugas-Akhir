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
            { "orderable": false, "targets": [1, 6] }
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
    $('#deviceFilter').on('change', function() {
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
            // Cari tanggal dalam format yang sesuai dengan tampilan (dd-MM-yyyy)
            table.column(4).search(selectedDate.split('-').reverse().join('-')).draw();
        } else {
            table.column(4).search('').draw();
        }
    });

    // Tombol reset
    $('#resetFilter').on('click', function() {
        $('#deviceFilter').val('');
        $('#dateFilter').val('');
        table.search('').columns().search('').draw();
    });
});
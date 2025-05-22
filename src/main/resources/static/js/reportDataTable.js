function initDataTable() {
    if ($('#reportsTable').length && !$.fn.DataTable.isDataTable('#reportsTable')) {
        return $('#reportsTable').DataTable({
            language: {
                "decimal": "",
                "emptyTable": "Tidak ada data tersedia",
                "info": "",
                "infoEmpty": "Menampilkan 0 sampai 0 dari 0 entri",
                "infoFiltered": "(disaring dari _MAX_ total entri)",
                "infoPostFix": "",
                "thousands": ".",
                "lengthMenu": "",
                "loadingRecords": "Memuat...",
                "processing": "Memproses...",
                "search": "Cari:",
                "zeroRecords": "Tidak ditemukan data yang sesuai",
                "paginate": {
                    "first": "Pertama",
                    "last": "Terakhir",
                    "next": "Selanjutnya",
                    "previous": "Sebelumnya"
                }
            },
            // Tambahan untuk layout pagination dan komponen lainnya
            dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                 'rt' +
                 '<"row"<"col-sm-12 col-md-6"i><"col-sm-12 text-center"p>>'
        });
    }
    return null;
}
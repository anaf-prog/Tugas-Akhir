$(document).ready(function() {
    // Inisialisasi DataTable (hanya sekali)
    const reportsTable = initDataTable();
    
    // Inisialisasi flatpickr
    initDatePickers();
    
    // Setup form validation
    setupFormValidation(reportsTable);
});

function initDataTable() {
    if ($('#reportsTable').length && !$.fn.DataTable.isDataTable('#reportsTable')) {
        return $('#reportsTable').DataTable({
            language: {
                "decimal": "",
                "emptyTable": "Tidak ada data tersedia",
                "info": "Menampilkan _START_ sampai _END_ dari _TOTAL_ entri",
                "infoEmpty": "Menampilkan 0 sampai 0 dari 0 entri",
                "infoFiltered": "(disaring dari _MAX_ total entri)",
                "infoPostFix": "",
                "thousands": ".",
                "lengthMenu": "Tampilkan _MENU_ entri",
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
            }
        });
    }
    return null;
}

function initDatePickers() {
    if (typeof flatpickr !== 'undefined') {
        flatpickr("#issueDate", {
            enableTime: true,
            dateFormat: "Y-m-d H:i",
            time_24hr: true,
            locale: "id",
            defaultDate: new Date()
        });
        
        flatpickr("#repairDate", {
            enableTime: true,
            dateFormat: "Y-m-d H:i",
            time_24hr: true,
            locale: "id",
            defaultDate: new Date()
        });
    }
}

function setupFormValidation(dataTable) {
    $("#reportForm").on('submit', function(e) {
        e.preventDefault();
        
        // Validasi tanggal
        const issueDate = new Date($("#issueDate").val());
        const repairDate = new Date($("#repairDate").val());
        
        if (repairDate < issueDate) {
            showAlert('Tanggal perbaikan tidak boleh sebelum tanggal masalah!', 'danger');
            return false;
        }
        
        // Tampilkan loading
        const submitBtn = $(this).find('button[type="submit"]');
        submitBtn.prop('disabled', true);
        submitBtn.html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Menyimpan...');
        
        // Kirim data via AJAX
        $.ajax({
        url: $(this).attr('action'),
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            deviceId: $("#deviceId").val(),
            issueDate: $("#issueDate").val(),
            issueDescription: $("#issueDescription").val(),
            repairDate: $("#repairDate").val(),
            repairDescription: $("#repairDescription").val(),
            technician: $("#technician").val(),
            status: $("#status").val()
        }),
        success: function(response) {
            $('#createReportModal').modal('hide');
            $("#reportForm")[0].reset();
            
            // Tampilkan notifikasi sukses
            $('.container.mt-3').prepend(`
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    Laporan berhasil dibuat!
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            `);

            // Reload halaman untuk update data
            setTimeout(() => {
                location.reload();
            }, 1500);

        },
            error: function(xhr) {
                let errorMsg = 'Gagal membuat laporan';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg += ': ' + xhr.responseJSON.message;
                }
                
                // Tampilkan notifikasi error
                $('.container.mt-3').prepend(`
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        ${errorMsg}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                `);
            },
            complete: function() {
                submitBtn.prop('disabled', false);
                submitBtn.text('Simpan Laporan');
            }
        });

    });
}

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
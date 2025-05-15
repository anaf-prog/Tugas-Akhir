class DeleteModalHandler {
    constructor() {
        this.modal = $('#deleteConfirmModalReport');
        this.confirmBtn = $('#confirmDeleteBtn');
        this.currentReportId = null;
        
        this.initEventHandlers();
    }
    
    initEventHandlers() {
        $(document).on('click', '.delete-btn', (e) => {
            e.preventDefault();
            this.currentReportId = $(e.currentTarget).data('id');
            this.modal.modal('show');
        });
        
        this.confirmBtn.on('click', () => {
            this.deleteReport();
        });
    }
    
    deleteReport() {
        if (!this.currentReportId) return;
        
        // Tampilkan loading
        this.confirmBtn.prop('disabled', true);
        this.confirmBtn.html('<span class="spinner-border spinner-border-sm"></span> Menghapus...');
        
        $.ajax({
            url: '/reports/delete/' + this.currentReportId,
            type: 'POST',
            success: (response) => {
                // Tampilkan notifikasi sukses
                showAlert(response.message, 'success');
                
                // Tunggu 1.5 detik sebelum reload
                setTimeout(() => {
                    location.reload();
                }, 1500);
            },
            error: (xhr) => {
                let errorMsg = 'Gagal menghapus laporan';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                }
                
                // Tampilkan notifikasi error
                showAlert(errorMsg, 'danger');
            },
            complete: () => {
                this.modal.modal('hide');
                this.currentReportId = null;
                this.confirmBtn.prop('disabled', false);
                this.confirmBtn.text('Ya, Hapus');
            }
        });
    }
}

// Inisialisasi
$(document).ready(function() {
    new DeleteModalHandler();
});
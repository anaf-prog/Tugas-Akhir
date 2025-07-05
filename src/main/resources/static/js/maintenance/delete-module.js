function loadDeleteModule() {
    // Variabel untuk menyimpan ID yang akan dihapus
    let deleteId = null;

    // Handle delete button click
    $('.delete-btn').on('click', function() {
        deleteId = $(this).data('id');
        $('#deleteConfirmationModal').modal('show');
    });

    // Handle confirm delete button click
    $('#confirmDeleteBtn').on('click', function() {
        if (deleteId) {
            deleteMaintenanceRecord(deleteId);
        }
    });

    // Reset deleteId ketika modal ditutup
    $('#deleteConfirmationModal').on('hidden.bs.modal', function() {
        deleteId = null;
    });
}

function deleteMaintenanceRecord(id) {
    $.ajax({
        url: `/api/maintenance/${id}`,
        type: 'DELETE',
        beforeSend: function() {
            // Tampilkan loading state
            $('#confirmDeleteBtn').prop('disabled', true);
            $('#confirmDeleteBtn').html(`
                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                Menghapus...
            `);
        },
        success: function(response) {
            $('#deleteConfirmationModal').modal('hide');
            showAlert('success', response.message);
            setTimeout(() => location.reload(), 1500);
        },
        error: function(xhr) {
            const errorMsg = xhr.responseJSON?.message || 'Gagal menghapus data';
            showAlert('danger', errorMsg);
            $('#deleteConfirmationModal').modal('hide');
        },
        complete: function() {
            // Reset button state
            $('#confirmDeleteBtn').prop('disabled', false);
            $('#confirmDeleteBtn').text('Hapus');
        }
    });
}
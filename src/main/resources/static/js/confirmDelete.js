// /js/confirm-delete.js

document.addEventListener('DOMContentLoaded', function() {
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    
    // Tangani klik tombol hapus
    document.querySelectorAll('.delete-btn').forEach(button => {
        button.addEventListener('click', function() {
            const deviceId = this.getAttribute('data-id');
            
            // Set URL hapus dengan Thymeleaf-style URL
            confirmDeleteBtn.href = '/devices/delete/' + deviceId;
            
            // Tampilkan modal
            deleteModal.show();
        });
    });
});
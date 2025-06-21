$(document).ready(function() {
    // Inisialisasi DataTable
    $('#maintenanceTable').DataTable({
        language: {
            decimal: ",",
            emptyTable: "Tidak ada data tersedia",
            info: "Menampilkan _START_ sampai _END_ dari _TOTAL_ entri",
            infoEmpty: "Menampilkan 0 sampai 0 dari 0 entri",
            infoFiltered: "(disaring dari _MAX_ total entri)",
            infoPostFix: "",
            thousands: ".",
            lengthMenu: "Tampilkan _MENU_ entri",
            loadingRecords: "Memuat...",
            processing: "Memproses...",
            search: "Cari:",
            zeroRecords: "Tidak ditemukan data yang sesuai",
            paginate: {
                first: "Pertama",
                last: "Terakhir",
                next: "Selanjutnya",
                previous: "Sebelumnya"
            }
        },
        // order: [[0, 'desc']],
        dom: '<"top"f>rt<"bottom"p>',
        info: false,
        lengthChange: false,
        pagingType: "full_numbers",
        drawCallback: function(settings) {
            const pagination = $(this).closest('.dataTables_wrapper').find('.dataTables_paginate');
            pagination.addClass('custom-pagination');
            pagination.find('.paginate_button.first, .paginate_button.last').remove();
        }
    });
    
    // Toggle sidebar
    $('#sidebarCollapse').on('click', function() {
        $('#sidebar').toggleClass('active');
    });

    // Handle form submission untuk tambah data
    $('#addMaintenanceForm').on('submit', function(e) {
        e.preventDefault();
        
        // Validasi form
        const formData = {
            deviceId: $('#deviceId').val(),
            maintenanceDate: $('#maintenanceDate').val(),
            scheduledTime: $('#scheduledTime').val(),
            autoDisable: $('#autoDisable').is(':checked'),
            technician: $('#technician').val(),
            description: $('#description').val()
        };
        
        // Validasi frontend
        if (!formData.deviceId) {
            showAlert('danger', 'Perangkat harus dipilih');
            return;
        }
        if (!formData.maintenanceDate) {
            showAlert('danger', 'Tanggal maintenance harus diisi');
            return;
        }
        if (!formData.technician) {
            showAlert('danger', 'Teknisi harus diisi');
            return;
        }
        if (!formData.description) {
            showAlert('danger', 'Deskripsi harus diisi');
            return;
        }
        if (formData.autoDisable && !formData.scheduledTime) {
            showAlert('danger', 'Harap isi jadwal maintenance jika ingin mengaktifkan auto-disable');
            return;
        }
        
        // Kirim data
        $.ajax({
            url: '/api/maintenance',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(response) {
                showAlert('success', response.message);
                $('#addMaintenanceModal').modal('hide');
                setTimeout(() => location.reload(), 1500);
            },
            error: function(xhr) {
                let errorMsg = 'Gagal menyimpan data';
                try {
                    // Coba parse response JSON
                    const response = JSON.parse(xhr.responseText);
                    if (response.message) {
                        errorMsg = response.message;
                    }
                } catch (e) {
                    console.error('Error parsing response:', e);
                }
                showAlert('danger', errorMsg);
                console.error('Full error response:', xhr.responseText);
            }
        });
    });
    
    // Load devices untuk modal tambah
    $.get('/api/devices')
    .done(function(devices) {
        $('#deviceId').empty().append('<option value="">Pilih Perangkat</option>');
        
        devices.forEach(function(device) {
            const deviceName = device.name || device.deviceName;
            if (device.id && deviceName) {
                $('#deviceId').append(`<option value="${device.id}">${deviceName}</option>`);
            }
        });
    })
    .fail(function(error) {
        console.error('Gagal memuat perangkat:', error);
        $('#deviceId').html('<option value="">Gagal memuat perangkat</option>');
    });
    
    // Set default datetime untuk maintenanceDate dan scheduledTime
    const now = new Date();
    const nowStr = now.toISOString().slice(0, 16);
    $('#maintenanceDate').val(nowStr);
    $('#scheduledTime').val(nowStr);

    // Handle edit button click
    $('#editMaintenanceModal').on('show.bs.modal', function(event) {
        const button = $(event.relatedTarget);
        const id = button.data('id');
        
        // Load maintenance log data
        $.get(`/api/maintenance/${id}`, function(log) {
            $('#editId').val(log.id);
            $('#editMaintenanceDate').val(formatDateTimeForInput(log.maintenanceDate));
            $('#editTechnician').val(log.technician);
            $('#editDescription').val(log.description);
            
            // Load devices untuk modal edit
            $.get('/api/devices', function(devices) {
                $('#editDeviceId').empty().append('<option value="">Pilih Perangkat</option>');
                devices.forEach(function(device) {
                    // Gunakan device.deviceName jika properti name tidak ada
                    const deviceName = device.name || device.deviceName;
                    $('#editDeviceId').append(
                        `<option value="${device.id}" ${device.id === log.device.id ? 'selected' : ''}>
                            ${deviceName}
                        </option>`
                    );
                });
            });
        }).fail(function(xhr) {
            showAlert('danger', 'Gagal memuat data pemeliharaan');
            $('#editMaintenanceModal').modal('hide');
        });
    });

    // Handle edit form submission
    $('#editMaintenanceForm').on('submit', function(e) {
        e.preventDefault();
        
        const formData = {
            deviceId: $('#editDeviceId').val(),
            maintenanceDate: $('#editMaintenanceDate').val(),
            technician: $('#editTechnician').val(),
            description: $('#editDescription').val()
        };
        
        const id = $('#editId').val();

        $.ajax({
            url: `/api/maintenance/${id}`,
            type: 'PUT',
            contentType: 'application/json',
            dataType: 'json', // tambahkan ini untuk memastikan response diparse sebagai JSON
            data: JSON.stringify(formData),
            success: function(response) {
                console.log('Response sukses:', response);
                showAlert('success', response.message);
                $('#editMaintenanceModal').modal('hide');
                setTimeout(() => location.reload(), 1500);
            },
            error: function(xhr, textStatus, errorThrown) {
                console.error('AJAX Error:', {
                    status: xhr.status,
                    statusText: xhr.statusText,
                    textStatus: textStatus,
                    errorThrown: errorThrown,
                    responseText: xhr.responseText
                });
                
                let errorMsg = 'Gagal memperbarui data pemeliharaan';
                
                try {
                    // Coba parse responseText jika responseJSON tidak ada
                    const errorResponse = xhr.responseJSON || JSON.parse(xhr.responseText);
                    errorMsg = errorResponse.message || errorResponse.error || errorMsg;
                } catch (e) {
                    errorMsg = xhr.responseText || errorMsg;
                }
                
                showAlert('danger', errorMsg);
            }
        });
    });

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
            $.ajax({
                url: `/api/maintenance/${deleteId}`,
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
    });

    // Reset deleteId ketika modal ditutup
    $('#deleteConfirmationModal').on('hidden.bs.modal', function() {
        deleteId = null;
    });

    //Fungsi untuk menampilkan notifikasi
    function showAlert(type, message) {
        const alertHtml = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        
        // Tempatkan notifikasi di atas konten utama
        $('.container.mt-3').html(alertHtml);
        
        // Auto close setelah 5 detik
        setTimeout(() => {
            $('.alert').alert('close');
        }, 5000);
    }
    
    // Format datetime untuk input datetime-local
    function formatDateTimeForInput(dateTimeStr) {
        const date = new Date(dateTimeStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    }
});
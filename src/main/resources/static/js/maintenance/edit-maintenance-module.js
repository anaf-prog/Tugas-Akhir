function loadEditMaintenanceModule() {
    // Handle edit button click
    $('#editMaintenanceModal').on('show.bs.modal', function(event) {
        const button = $(event.relatedTarget);
        const id = button.data('id');
        loadMaintenanceLogForEdit(id);
    });

    // Handle edit form submission
    $('#editMaintenanceForm').on('submit', function(e) {
        e.preventDefault();
        submitEditMaintenanceForm();
    });
}

function loadMaintenanceLogForEdit(id) {
    $.get(`/api/maintenance/${id}`, function(log) {
        $('#editId').val(log.id);
        $('#editMaintenanceDate').val(formatDateTimeForInput(log.maintenanceDate));
        $('#editTechnician').val(log.technician);
        $('#editDescription').val(log.description);
        
        loadDevicesForEditForm(log.device.id);
    }).fail(function(xhr) {
        showAlert('danger', 'Gagal memuat data pemeliharaan');
        $('#editMaintenanceModal').modal('hide');
    });
}

function loadDevicesForEditForm(selectedDeviceId) {
    $.get('/api/devices', function(devices) {
        $('#editDeviceId').empty().append('<option value="">Pilih Perangkat</option>');
        devices.forEach(function(device) {
            const deviceName = device.name || device.deviceName;
            $('#editDeviceId').append(
                `<option value="${device.id}" ${device.id === selectedDeviceId ? 'selected' : ''}>
                    ${deviceName}
                </option>`
            );
        });
    });
}

function submitEditMaintenanceForm() {
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
        dataType: 'json',
        data: JSON.stringify(formData),
        success: function(response) {
            console.log('Response sukses:', response);
            showAlert('success', response.message);
            $('#editMaintenanceModal').modal('hide');
            setTimeout(() => location.reload(), 1500);
        },
        error: function(xhr, textStatus, errorThrown) {
            handleEditFormError(xhr, textStatus, errorThrown);
        }
    });
}

function handleEditFormError(xhr, textStatus, errorThrown) {
    console.error('AJAX Error:', {
        status: xhr.status,
        statusText: xhr.statusText,
        textStatus: textStatus,
        errorThrown: errorThrown,
        responseText: xhr.responseText
    });
    
    let errorMsg = 'Gagal memperbarui data pemeliharaan';
    
    try {
        const errorResponse = xhr.responseJSON || JSON.parse(xhr.responseText);
        errorMsg = errorResponse.message || errorResponse.error || errorMsg;
    } catch (e) {
        errorMsg = xhr.responseText || errorMsg;
    }
    
    showAlert('danger', errorMsg);
}
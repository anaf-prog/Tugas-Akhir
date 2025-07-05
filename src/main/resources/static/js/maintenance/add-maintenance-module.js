function loadAddMaintenanceModule() {
    // Handle form submission untuk tambah data
    $('#addMaintenanceForm').on('submit', function(e) {
        e.preventDefault();
        submitAddMaintenanceForm();
    });
    
    // Load devices untuk modal tambah
    loadDevicesForAddForm();
    
    // Set default datetime untuk maintenanceDate dan scheduledTime
    setDefaultDateTimeForAddForm();
}

function submitAddMaintenanceForm() {
    const formData = getAddFormData();
    const validationErrors = validateAddForm(formData);
    
    if (validationErrors.length > 0) {
        showAlert('danger', validationErrors[0]);
        return;
    }
    
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
            handleAddFormError(xhr);
        }
    });
}

function getAddFormData() {
    return {
        deviceId: $('#deviceId').val(),
        maintenanceDate: $('#maintenanceDate').val(),
        scheduledTime: $('#scheduledTime').val(),
        autoDisable: $('#autoDisable').is(':checked'),
        technician: $('#technician').val(),
        description: $('#description').val(),
        repairCompletionTime: $('#repairCompletionTime').val()
    };
}

function validateAddForm(formData) {
    const errors = [];
    
    if (!formData.deviceId) errors.push('Perangkat harus dipilih');
    if (!formData.maintenanceDate) errors.push('Tanggal maintenance harus diisi');
    if (!formData.technician) errors.push('Teknisi harus diisi');
    if (!formData.description) errors.push('Deskripsi harus diisi');
    if (formData.autoDisable && !formData.scheduledTime) {
        errors.push('Harap isi jadwal maintenance jika ingin mengaktifkan auto-disable');
    }
    if (formData.autoDisable && formData.repairCompletionTime && 
        new Date(formData.repairCompletionTime) <= new Date(formData.scheduledTime)) {
        errors.push('Waktu selesai perbaikan harus setelah waktu mulai maintenance');
    }
    
    return errors;
}

function loadDevicesForAddForm() {
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
}

function setDefaultDateTimeForAddForm() {
    const now = new Date();
    const nowStr = now.toISOString().slice(0, 16);
    $('#maintenanceDate').val(nowStr);
    $('#scheduledTime').val(nowStr);
}

function handleAddFormError(xhr) {
    let errorMsg = 'Gagal menyimpan data';
    try {
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
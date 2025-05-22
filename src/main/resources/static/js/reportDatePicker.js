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
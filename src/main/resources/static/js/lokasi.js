document.addEventListener('DOMContentLoaded', function() {
    // Sidebar toggle
    $('#sidebarCollapse').on('click', function() {
        $('#sidebar').toggleClass('active');
        $('#content').toggleClass('active');
        
        // Force redraw untuk mencegah bug visual
        document.body.style.overflow = 'hidden';
        setTimeout(function() {
            document.body.style.overflow = '';
        }, 10);
    });

    // Data lokasi dan perangkat (ini nanti bisa diganti dengan fetch API ke backend)
    const locations = [
        {id: 1, building: 'Gedung A', floor: 'Lantai 1', room: 'Server Room Utama', location_name: 'Gedung UNSIA'},
        {id: 2, building: 'Gedung A', floor: 'Lantai 1', room: 'Server Room Backup', location_name: 'Gedung UNSIA'},
        {id: 3, building: 'Gedung B', floor: 'Lantai 2', room: 'Ruang Switch', location_name: 'Gedung UNSIA'},
        {id: 4, building: 'Gedung B', floor: 'lantai 3', room: 'Ruang monitoring', location_name: 'Gedung UNSIA'},
        {id: 5, building: 'Gedung B', floor: 'lantai 2', room: 'Ruang router', location_name: 'Gedung UNSIA'},
        {id: 6, building: 'Gedung B', floor: 'lantai 3', room: 'Ruang router', location_name: 'Gedung UNSIA'},
        {id: 7, building: 'Gedung B', floor: 'lantai 3', room: 'Ruang router', location_name: 'Gedung UNSIA'},
        {id: 8, building: 'Gedung B', floor: 'lantai 3', room: 'Ruang router', location_name: 'Gedung UNSIA'},
        {id: 9, building: 'Gedung B', floor: 'lantai 3', room: 'Ruang router', location_name: 'Gedung UNSIA'},
        {id: 10, building: 'Gedung B', floor: 'lantai 3', room: 'Ruang monitoring', location_name: 'Gedung UNSIA'},
        {id: 11, building: 'Gedung B', floor: 'lantai 4', room: 'Ruang router', location_name: 'Gedung UNSIA C'},
        {id: 12, building: 'Gedung B', floor: 'lantai 2', room: 'Ruang router', location_name: 'Gedung UNSIA'}
    ];

    const devices = [
        {id: 4, device_name: 'Switch Utama', ip_address: '192.168.1.4', device_type: 'SWITCH', location_id: 3},
        {id: 7, device_name: 'Server B', ip_address: '192.168.1.7', device_type: 'SERVER', location_id: 2},
        {id: 5, device_name: 'Switch Backup', ip_address: '192.168.1.5', device_type: 'SWITCH', location_id: 3},
        {id: 6, device_name: 'Server A', ip_address: '192.168.1.6', device_type: 'SERVER', location_id: 1},
        {id: 15, device_name: 'Router B', ip_address: '192.168.1.9', device_type: 'ROUTER', location_id: 11},
        {id: 8, device_name: 'Router Utama', ip_address: '192.168.1.8', device_type: 'ROUTER', location_id: 4},
        {id: 16, device_name: 'Router A', ip_address: '192.168.1.1', device_type: 'ROUTER', location_id: 12},
        {id: 3, device_name: 'Router Back up', ip_address: '192.168.1.3', device_type: 'ROUTER', location_id: 1}
    ];

    // Fungsi untuk menampilkan daftar lokasi dan perangkat
    function renderLocations() {
        const container = document.getElementById('locations-container');
        
        locations.forEach(location => {
            // Filter perangkat berdasarkan location_id
            const locationDevices = devices.filter(device => device.location_id === location.id);
            
            // Buat card untuk setiap lokasi
            const card = document.createElement('div');
            card.className = 'card mb-4 location-card';
            card.innerHTML = `
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0">${location.location_name}</h5>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Gedung:</strong> ${location.building}</p>
                            <p><strong>Lantai:</strong> ${location.floor}</p>
                            <p><strong>Ruangan:</strong> ${location.room}</p>
                        </div>
                        <div class="col-md-6">
                            <h6>Perangkat di Lokasi Ini:</h6>
                            ${locationDevices.length > 0 ? 
                                `<ul class="list-group">${locationDevices.map(device => 
                                    `<li class="list-group-item d-flex justify-content-between align-items-center">
                                        ${device.device_name}
                                        <span class="badge bg-secondary">${device.device_type}</span>
                                    </li>`
                                ).join('')}</ul>` : 
                                '<p class="text-muted">Tidak ada perangkat di lokasi ini</p>'}
                        </div>
                    </div>
                </div>
                <div class="card-footer">
                    <div id="map-${location.id}" class="location-map" style="height: 300px;"></div>
                </div>
            `;
            
            container.appendChild(card);
            
            // Inisialisasi peta untuk lokasi ini
            initMap(`map-${location.id}`, location.location_name);
        });
    }

    // Fungsi untuk inisialisasi peta Leaflet
    function initMap(mapId, locationName) {
        const unsiaCoords = [-6.2984519, 106.8205877];
        const map = L.map(mapId).setView(unsiaCoords, 18);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
        
        const customIcon = L.icon({
            iconUrl: 'https://cdn-icons-png.flaticon.com/512/684/684908.png',
            iconSize: [32, 32],
            iconAnchor: [16, 32],
            popupAnchor: [0, -32]
        });
        
        L.marker(unsiaCoords, {icon: customIcon}).addTo(map)
            .bindPopup(`<b>${locationName}</b><br>Universitas Siber Asia`)
            .openPopup();
        
        L.circle(unsiaCoords, {
            color: '#3388ff',
            fillColor: '#3388ff',
            fillOpacity: 0.2,
            radius: 50
        }).addTo(map);
    }

    // Panggil fungsi render saat halaman dimuat
    renderLocations();
});
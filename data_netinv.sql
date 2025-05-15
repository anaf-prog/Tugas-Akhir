-- Active: 1746537516437@@127.0.0.1@5432@netinv
-- user --
INSERT INTO users (username, password, email, role) VALUES
('Anaf', 'anaf170996', 'izzatannafs89@gmail.com', 'ADMIN'),
('Izzat', 'izzat170996', 'izzatannafs6@gmail.com', 'VIEWER');

SELECT * FROM users;

-- Lokasi perangkat --
INSERT INTO locations (building, floor, room) VALUES
('Gedung A', 'Lantai 1', 'Server Room Utama'),
('Gedung A', 'Lantai 1', 'Server Room Backup'),
('Gedung B', 'Lantai 2', 'Ruang Switch');

SELECT * FROM locations;

-- Device --
-- Router --
INSERT INTO devices (device_name, ip_address, mac_address, device_type, status_device, location_id) VALUES
('Router Utama', '192.168.1.1', '00:1A:2B:3C:4D:5E', 'ROUTER', 'ONLINE', 1),
('Router Backup', '192.168.1.2', '00:1A:2B:3C:4D:5F', 'ROUTER', 'ONLINE', 2),
('Router Cadangan', '192.168.1.3', '00:1A:2B:3C:4D:6A', 'ROUTER', 'ONLINE', 1);

-- Switch --
INSERT INTO devices (device_name, ip_address, mac_address, device_type, status_device, location_id) VALUES
('Switch Utama', '192.168.1.4', '00:1B:2C:3D:4E:5F', 'SWITCH', 'ONLINE', 3),
('Switch Backup', '192.168.1.5', '00:1B:2C:3D:4E:6A', 'SWITCH', 'ONLINE', 3);

-- Server --
INSERT INTO devices (device_name, ip_address, mac_address, device_type, status_device, location_id) VALUES
('Server A', '192.168.1.6', '00:1C:2D:3E:4F:5A', 'SERVER', 'ONLINE', 1),
('Server B', '192.168.1.7', '00:1C:2D:3E:4F:5B', 'SERVER', 'ONLINE', 2);

UPDATE devices 
SET status_device = 'ONLINE', 
ping_status = 'ONLINE';

SELECT * FROM devices;

-- Jika Router Utama down, gunakan Router Backup
INSERT INTO backup_routes (main_device_id, backup_device_id, is_active) VALUES
(1, 2, true),  -- Router Utama -> Router Backup
(4, 5, true);  -- Switch Utama -> Switch Backup

SELECT * FROM backup_routes;

-- Data monitoring untuk Router Utama (contoh 3 log terakhir)
INSERT INTO monitoring_logs (device_id, ping_status, response_time) VALUES
(1, true, 5),
(1, true, 8),
(1, false, NULL); -- Ping terakhir gagal (simulasi gangguan)

SELECT * FROM monitoring_logs;

-- History maintenance --
INSERT INTO maintenance_logs (maintenance_date, descriptions, technician, device_id) VALUES
('2025-04-01 10:00:00', 'Ganti kabel fiber optik', 'Izzat', 1),
('2025-04-02 14:30:00', 'Update firmware', 'Izzat', 4);

SELECT * FROM maintenance_logs;

-- Contoh log failover sebelumnya (bisa di-generate otomatis nanti oleh sistem) --
INSERT INTO failover_logs (main_device_id, backup_device_id, response_time_ms, status) VALUES
(1, 2, 120, 'SUCCESS'),
(4, 5, 250, 'FAILED');

SELECT * FROM failover_logs;


-- Active: 1746537516437@@127.0.0.1@5432@netinv
-- Untuk PostgreSQL (gunakan BIGSERIAL dan text/boolean)
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    building VARCHAR(255),
    floor VARCHAR(255),
    room VARCHAR(255),
    location_name(255)
);

SELECT * FROM locations;

CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    device_name VARCHAR(255) NOT NULL,
    ip_address VARCHAR(15) UNIQUE NOT NULL,
    mac_address VARCHAR(17),        
    device_type VARCHAR(20) CHECK (device_type IN ('ROUTER', 'SWITCH', 'SERVER')) NOT NULL,
    status_device VARCHAR(20) CHECK (status_device IN ('ONLINE', 'OFFLINE')) DEFAULT 'ONLINE',
    last_checked TIMESTAMP,
    location_id BIGINT,
    ping_status VARCHAR(255) CHECK (ping_status IN ('ONLINE', 'OFFLINE', 'PINGING')),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE maintenance_logs (
    id BIGSERIAL PRIMARY KEY,
    maintenance_date TIMESTAMP NOT NULL,
    descriptions TEXT,
    technician VARCHAR(255),
    device_id BIGINT NOT NULL,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

CREATE TABLE report (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    issue_date TIMESTAMP NOT NULL,
    issue_description TEXT NOT NULL,
    repair_date TIMESTAMP NOT NULL,
    repair_description TEXT NOT NULL,
    technician VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

SELECT * FROM devices;

CREATE TABLE monitoring_logs (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    ping_status BOOLEAN NOT NULL, 
    response_time INTEGER,         
    monitoring TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    log_reason VARCHAR(50),
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- hapus data dengan rentang waktu --
-- DELETE FROM monitoring_logs WHERE monitoring >= '2025-05-10 16:00:00' AND monitoring <  '2025-05-10 17:00:00';
-- DELETE FROM monitoring_logs WHERE monitoring::date = '2025-05-10';

SELECT * FROM monitoring_logs;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(20) CHECK (role IN ('ADMIN', 'VIEWER')) NOT NULL
);

-- TABEL BACKUP ROUTES (Tambahan)
CREATE TABLE backup_routes (
    id BIGSERIAL PRIMARY KEY,
    main_device_id BIGINT NOT NULL,
    backup_device_id BIGINT NOT NULL,
    is_active BOOLEAN,
    FOREIGN KEY (main_device_id) REFERENCES devices(id),
    FOREIGN KEY (backup_device_id) REFERENCES devices(id)
);

SELECT * FROM backup_routes;

-- TABEL FAILOVER LOGS (Tambahan)
CREATE TABLE failover_logs (
    id BIGSERIAL PRIMARY KEY,
    waktu TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    main_device_id BIGINT NOT NULL,
    backup_device_id BIGINT NOT NULL,
    response_time_ms INTEGER,
    status VARCHAR(20) CHECK (status IN ('SUCCESS', 'FAILED')),
    FOREIGN KEY (main_device_id) REFERENCES devices(id),
    FOREIGN KEY (backup_device_id) REFERENCES devices(id)
);

-- Tambahan: Index untuk mempercepat query monitoring
CREATE INDEX idx_monitoring_device_id ON monitoring_logs (device_id);
CREATE INDEX idx_devices_status ON devices (status_device);




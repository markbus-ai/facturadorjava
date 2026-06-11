CREATE DATABASE IF NOT EXISTS sfi_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sfi_db;

CREATE USER IF NOT EXISTS 'sfi_user'@'localhost' IDENTIFIED BY 'sfi_pass';
GRANT ALL PRIVILEGES ON sfi_db.* TO 'sfi_user'@'localhost';
FLUSH PRIVILEGES;

-- ============================================================
-- TABLA DE USUARIOS (RBAC)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('ADMIN', 'USER', 'REPOSITOR') NOT NULL DEFAULT 'USER',
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- TABLA DE CLIENTES
-- ============================================================
CREATE TABLE IF NOT EXISTS clients (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(255) DEFAULT '',
    phone       VARCHAR(50)  DEFAULT '',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- TABLA DE PRODUCTOS (Catálogo)
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    price       DECIMAL(12,2) NOT NULL CHECK (price > 0),
    stock       INT          NOT NULL CHECK (stock >= 0),
    min_stock   INT          NOT NULL DEFAULT 0,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- TABLA DE FACTURAS (Cabecera)
-- ============================================================
CREATE TABLE IF NOT EXISTS invoices (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number  VARCHAR(2000) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    client_id       BIGINT,
    subtotal        DECIMAL(12,2) NOT NULL,
    discount_type   ENUM('NONE', 'PERCENTAGE', 'NOMINAL') NOT NULL DEFAULT 'NONE',
    discount_value  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    taxable_amount  DECIMAL(12,2) NOT NULL,
    tax_rate        DECIMAL(5,2) NOT NULL DEFAULT 21.00,
    tax_amount      DECIMAL(12,2) NOT NULL,
    total           DECIMAL(12,2) NOT NULL,
    issued_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA DE DETALLE DE FACTURA (Items)
-- ============================================================
CREATE TABLE IF NOT EXISTS invoice_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id      BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    quantity        INT NOT NULL CHECK (quantity > 0),
    unit_price      DECIMAL(12,2) NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL,
    discount_type   ENUM('NONE', 'PERCENTAGE', 'NOMINAL') NOT NULL DEFAULT 'NONE',
    discount_value  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

-- ============================================================
-- USUARIOS POR DEFECTO
-- ============================================================
INSERT INTO users (username, password, role) VALUES
('admin', 'admin123', 'ADMIN'),
('user',  'user123',  'USER'),
('repositor', 'repo123', 'REPOSITOR');

-- ============================================================
-- CLIENTE POR DEFECTO
-- ============================================================
INSERT INTO clients (name, address, phone) VALUES
('Consumidor Final', '', '');

-- ============================================================
-- PRODUCTOS DE EJEMPLO
-- ============================================================
INSERT INTO products (code, name, description, price, stock, min_stock) VALUES
('PROD-001', 'Laptop Gamer X1',  'Laptop 15.6" i7 32GB 1TB SSD', 850000.00, 10, 3),
('PROD-002', 'Monitor 27" 4K',   'Monitor IPS 27 pulgadas 4K UHD', 320000.00, 15, 5),
('PROD-003', 'Teclado Mecánico', 'Teclado RGB switches Cherry MX', 45000.00, 30, 10),
('PROD-004', 'Mouse Inalámbrico','Mouse ergonómico recargable', 25000.00, 50, 15),
('PROD-005', 'Webcam HD 1080p',  'Cámara web con micrófono integrado', 18000.00, 20, 5),
('PROD-006', 'Auriculares Bluetooth','Auriculares over-ear cancelación ruido', 35000.00, 25, 8),
('PROD-007', 'Pendrive 64GB',   'USB 3.0 tipo C y A', 8500.00, 100, 20),
('PROD-008', 'Disco SSD 1TB',   'SSD NVMe M.2 3500MB/s', 95000.00, 40, 10),
('PROD-009', 'Hub USB-C 7 puertos','Hub con HDMI SD y USB 3.0', 22000.00, 35, 10),
('PROD-010', 'Silla Ergonómica','Silla oficina con soporte lumbar', 210000.00, 8, 3),
('PROD-011', 'Cable HDMI 2m',   'Cable HDMI 2.1 48Gbps', 4500.00, 200, 50),
('PROD-012', 'Router WiFi 6',   'Router AX5400 doble banda', 65000.00, 12, 4);

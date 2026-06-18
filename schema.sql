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
-- TABLA DE PROVEEDORES
-- ============================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    contact     VARCHAR(150) DEFAULT '',
    phone       VARCHAR(50)  DEFAULT '',
    email       VARCHAR(100) DEFAULT '',
    address     VARCHAR(255) DEFAULT '',
    cuit        VARCHAR(11)  DEFAULT '',
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
    supplier_id BIGINT       DEFAULT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
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
('admin', 'ae6819454d0b069fdadafc733b751bc22fb3fe941892c79f143f1d23a9642c91', 'ADMIN'),
('user',  '8f342c057682295211d19f2e337924e136779f9861a23afd8ce0406053610974',  'USER'),
('repositor', 'f21cfa6e2f955baf2ebec163d6353c56819b4c6b479f4e4e827f0b876542cd2d', 'REPOSITOR');

-- ============================================================
-- CLIENTE POR DEFECTO
-- ============================================================
INSERT INTO clients (name, address, phone) VALUES
('Consumidor Final', '', '');

-- ============================================================
-- PROVEEDORES DE EJEMPLO
-- ============================================================
INSERT INTO suppliers (name, contact, phone, email, address, cuit) VALUES
('Tech Imports S.A.', 'Juan Pérez', '1122334455', 'contacto@techimports.com', 'Av. Cabildo 2000, CABA', '20304567899'),
('Distribuidora Global', 'María Gómez', '1155667788', 'ventas@distglobal.com', 'Calle Falsa 123, CABA', '27401234568');

-- ============================================================
-- PRODUCTOS DE EJEMPLO
-- ============================================================
INSERT INTO products (code, name, description, price, stock, min_stock, supplier_id) VALUES
('PROD-001', 'Laptop Gamer X1',  'Laptop 15.6" i7 32GB 1TB SSD', 850000.00, 10, 3, 1),
('PROD-002', 'Monitor 27" 4K',   'Monitor IPS 27 pulgadas 4K UHD', 320000.00, 15, 5, 1),
('PROD-003', 'Teclado Mecánico', 'Teclado RGB switches Cherry MX', 45000.00, 30, 10, 2),
('PROD-004', 'Mouse Inalámbrico','Mouse ergonómico recargable', 25000.00, 50, 15, 2),
('PROD-005', 'Webcam HD 1080p',  'Cámara web con micrófono integrado', 18000.00, 20, 5, 1),
('PROD-006', 'Auriculares Bluetooth','Auriculares over-ear cancelación ruido', 35000.00, 25, 8, 2),
('PROD-007', 'Pendrive 64GB',   'USB 3.0 tipo C y A', 8500.00, 100, 20, 2),
('PROD-008', 'Disco SSD 1TB',   'SSD NVMe M.2 3500MB/s', 95000.00, 40, 10, 1),
('PROD-009', 'Hub USB-C 7 puertos','Hub con HDMI SD y USB 3.0', 22000.00, 35, 10, 2),
('PROD-010', 'Silla Ergonómica','Silla oficina con soporte lumbar', 210000.00, 8, 3, NULL),
('PROD-011', 'Cable HDMI 2m',   'Cable HDMI 2.1 48Gbps', 4500.00, 200, 50, NULL),
('PROD-012', 'Router WiFi 6',   'Router AX5400 doble banda', 65000.00, 12, 4, 1);

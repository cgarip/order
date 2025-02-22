-- Create database if not exists
CREATE DATABASE IF NOT EXISTS fruit_order;
USE fruit_order;

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
  Id INT NOT NULL AUTO_INCREMENT,
  Name VARCHAR(100) NOT NULL,
  Phone VARCHAR(20) DEFAULT NULL,
  Email VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (Id)
);

-- Employees table
CREATE TABLE IF NOT EXISTS employees (
  Id INT NOT NULL AUTO_INCREMENT,
  UserName VARCHAR(50) NOT NULL,
  Phone VARCHAR(20) DEFAULT NULL,
  Email VARCHAR(100) DEFAULT NULL,
  Password VARCHAR(255) NOT NULL,
  PRIMARY KEY (Id)
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
  Id INT NOT NULL AUTO_INCREMENT,
  ProductName VARCHAR(100) NOT NULL,
  PRIMARY KEY (Id)
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
  Id INT NOT NULL AUTO_INCREMENT,
  CustomerId INT DEFAULT NULL,
  EmployeeId INT DEFAULT NULL,
  ProductId INT DEFAULT NULL,
  Quantity INT DEFAULT NULL,
  PRIMARY KEY (Id),
  FOREIGN KEY (CustomerId) REFERENCES customers(Id) ON DELETE CASCADE,
  FOREIGN KEY (EmployeeId) REFERENCES employees(Id) ON DELETE CASCADE,
  FOREIGN KEY (ProductId) REFERENCES products(Id) ON DELETE CASCADE
);

-- Create View
CREATE OR REPLACE VIEW invoiceview AS
SELECT 
    o.Id AS OrderID,
    c.Name AS CustomerName,
    c.Phone AS CustomerPhone,
    c.Email AS CustomerEmail,
    p.ProductName AS ProductName,
    o.Quantity AS Quantity,
    e.UserName AS EmployeeName,
    e.Phone AS EmployeePhone,
    e.Email AS EmployeeEmail
FROM orders o
JOIN customers c ON o.CustomerId = c.Id
JOIN products p ON o.ProductId = p.Id
JOIN employees e ON o.EmployeeId = e.Id;

-- Insert Sample Data
INSERT INTO customers (Name, Phone, Email) VALUES
('Alice Johnson', '1234567890', 'alice@example.com'),
('Bob Smith', '0987654321', 'bob@example.com'),
('Charlie Brown', '1112223333', 'charlie@example.com'),
('David White', '2223334444', 'david@example.com'),
('Emma Watson', '3334445555', 'emma@example.com'),
('Frank Miller', '4445556666', 'frank@example.com'),
('Grace Kelly', '5556667777', 'grace@example.com'),
('Hannah Lee', '6667778888', 'hannah@example.com'),
('Ian Clark', '7778889999', 'ian@example.com'),
('Jack Black', '8889990000', 'jack@example.com');

INSERT INTO employees (UserName, Phone, Email, Password) VALUES
('admin', '1112223333', 'admin@example.com', '111111'),
('john_doe', '2223334444', 'john@example.com', '111111'),
('jane_smith', '3334445555', 'jane@example.com', '111111'),
('robert_brown', '4445556666', 'robert@example.com', '111111'),
('linda_jones', '5556667777', 'linda@example.com', '111111'),
('michael_scott', '6667778888', 'michael@example.com', '111111'),
('susan_clark', '7778889999', 'susan@example.com', '111111'),
('paul_walker', '8889990000', 'paul@example.com', '111111'),
('natalie_p', '9990001111', 'natalie@example.com', '111111'),
('chris_evans', '0001112222', 'chris@example.com', '111111');

INSERT INTO products (ProductName) VALUES
('Apple'),
('Banana'),
('Cherry'),
('Date'),
('Elderberry'),
('Fig'),
('Grapes'),
('Honeydew'),
('Indian Fig'),
('Jackfruit');

-- Additional Orders for Customers
INSERT INTO orders (CustomerId, EmployeeId, ProductId, Quantity) VALUES
(1, 1, 2, 3), -- Alice Johnson
(1, 2, 3, 2), 
(1, 3, 4, 6),
(1, 4, 5, 1),
(1, 5, 6, 4),

(2, 2, 1, 5), -- Bob Smith
(2, 3, 3, 7),
(2, 4, 4, 2),
(2, 5, 5, 3),
(2, 6, 6, 6),

(3, 3, 2, 8), -- Charlie Brown
(3, 4, 4, 3),
(3, 5, 5, 5),
(3, 6, 6, 2),
(3, 7, 7, 9),

(4, 4, 1, 10), -- David White
(4, 5, 3, 5),
(4, 6, 4, 7),
(4, 7, 5, 6),
(4, 8, 6, 4),

(5, 5, 2, 8), -- Emma Watson
(5, 6, 3, 4),
(5, 7, 5, 3),
(5, 8, 6, 2),
(5, 9, 7, 5),

(6, 6, 4, 9), -- Frank Miller
(6, 7, 5, 2),
(6, 8, 6, 6),
(6, 9, 7, 3),
(6, 10, 8, 7),

(7, 7, 1, 4), -- Grace Kelly
(7, 8, 2, 5),
(7, 9, 3, 6),
(7, 10, 4, 8),
(7, 1, 5, 3),

(8, 8, 6, 7), -- Hannah Lee
(8, 9, 7, 5),
(8, 10, 8, 2),
(8, 1, 9, 4),
(8, 2, 10, 3),

(9, 9, 4, 3), -- Ian Clark
(9, 10, 5, 2),
(9, 1, 6, 5),
(9, 2, 7, 6),
(9, 3, 8, 4),

(10, 10, 2, 7); -- Jack Black

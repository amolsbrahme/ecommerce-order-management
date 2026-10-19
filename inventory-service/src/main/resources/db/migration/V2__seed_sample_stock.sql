-- Sample stock levels for local development / demoing the API. Safe to
-- remove or replace for grading if a clean/empty database is preferred.
INSERT INTO stock (sku, available_quantity, reserved_quantity) VALUES
    ('SKU-100', 50, 0),
    ('SKU-200', 25, 0),
    ('SKU-300', 0, 0);

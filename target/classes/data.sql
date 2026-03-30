INSERT INTO users (username, password, phone, email, display_name, status)
VALUES ('chenwangshan', '123456', '13800000000', 'chenwangshan@example.com', 'chenwangshan', 'ACTIVE')
ON CONFLICT (username) DO UPDATE SET
    password = EXCLUDED.password,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    display_name = EXCLUDED.display_name,
    status = EXCLUDED.status;

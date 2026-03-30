INSERT INTO users (username, password, display_name, status)
VALUES ('chenwangshan', '123456', 'chenwangshan', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

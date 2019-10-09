INSERT INTO users(id, name, username, password, admin, active)
SELECT *
FROM (values (1, 'root', 'root', 'pass', true, true))
WHERE NOT EXISTS(SELECT * FROM users WHERE id = 1);
insert into user_profile(id, name, surname, identity_no, city, country, email, phone_number, address, zip_code, registration_date)
select *
from (values(1, 'root', 'root', '11111111111', 'Istanbul', 'TR', 'admin@iyzico-challenge.com', '555555555', 'address', '34545', current_timestamp))
WHERE NOT EXISTS(SELECT * FROM user_profile WHERE id = 1);

INSERT INTO users(id, user_profile_id, username, password, admin, active)
SELECT *
FROM (values (1, 1, 'root', 'pass', true, true))
WHERE NOT EXISTS(SELECT * FROM users WHERE id = 1);
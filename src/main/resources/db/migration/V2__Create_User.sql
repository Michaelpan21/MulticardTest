create extension if not exists pgcrypto;

INSERT INTO usr(id, password, username)
	VALUES (1, crypt('admin', gen_salt('bf', 8)), 'admin');

INSERT INTO user_role(user_id, roles)
    VALUES (1, 'USER');
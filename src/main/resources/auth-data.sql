-- password is "pw" (bcrypt hashed, {bcrypt} prefix for DelegatingPasswordEncoder)
insert into users(username, password, enabled, tenant_id)
values ('user11', '{bcrypt}$2y$10$QRQm20yMT8p7u/KD0jyNzObNxifhHT/GApHZAGYHm3q6sXRZ1Hnhi', true, 1),
       ('user21', '{bcrypt}$2y$10$xuJtW2ksjH/NtjSVWrVtHeEDIfwxKQjST/.yc9vboehcl2ez6UuWO', true, 2)
on conflict (username) do nothing;

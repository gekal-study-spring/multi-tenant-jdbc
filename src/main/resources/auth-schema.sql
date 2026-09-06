create table if not exists users
(
    username  varchar(255) not null primary key,
    password  varchar(255) not null,
    enabled   boolean      not null default true,
    tenant_id integer      not null
);

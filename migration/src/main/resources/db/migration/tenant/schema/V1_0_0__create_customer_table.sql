-- テナントDB共通のスキーマ（全テナントで同一）。
create table customer
(
    id   serial       primary key,
    name varchar(255) not null
);

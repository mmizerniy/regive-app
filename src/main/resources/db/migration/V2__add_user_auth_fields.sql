alter table users add column password varchar(255) not null default '';
alter table users alter column password drop default;

alter table users add column role varchar(20) not null default 'USER';
alter table users alter column role drop default;
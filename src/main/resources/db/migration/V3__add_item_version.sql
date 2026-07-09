alter table items add column version bigint not null default 0;
alter table items alter column version drop default;

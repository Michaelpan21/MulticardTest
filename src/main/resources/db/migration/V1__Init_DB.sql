create sequence hibernate_sequence start 1 increment 1;

create table repair_details (
    id int8 not null,
    atm_id varchar(255),
    atm_serial_number varchar(255),
    bank_name varchar(255),
    begin_date timestamp,
    channel varchar(255),
    end_date timestamp,
    reason varchar(255),
    primary key (id)
);

create table user_role (
    user_id int8 not null,
    roles varchar(255)
);

create table usr (
    id int8 not null,
    password varchar(255),
    username varchar(255),
    primary key (id)
);

alter table if exists user_role add constraint user_role_user_fk foreign key (user_id) references usr;
create sequence emails_id_increment;

create table emails
(
    id number(20) not null,
    name varchar2(20) not null,
    email varchar2(50) not null,
    date_value date,
    constraint con_emails_pk primary key (id)
);
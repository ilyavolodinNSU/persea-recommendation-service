create table if not exists brands (
    id bigserial primary key,
    name varchar(255) not null unique,
    description text
);

create table if not exists categories (
    id bigserial primary key,
    name varchar(255) not null unique,
    code varchar(255) not null unique
);

create table if not exists products (
    id bigserial primary key,
    name varchar(255) not null,
    brand_id bigint not null,
    category_id bigint not null,
    rating int check (rating between 0 and 100),
    image_uri text not null,
    updated_at timestamp not null default now()
);
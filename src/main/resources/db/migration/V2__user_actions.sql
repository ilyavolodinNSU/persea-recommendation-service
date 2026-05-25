create table if not exists user_action_types (
    id bigserial primary key,
    name varchar(20) not null
);

insert into user_action_types (name)
values ('like'), ('view'), ('scan');

create table if not exists user_actions (
    id bigserial primary key,
    keycloak_id uuid not null,
    product_id bigint not null references products(id) on delete cascade,
    type_id bigint not null references user_action_types(id) on delete cascade,
    created_at timestamptz not null default now()
);
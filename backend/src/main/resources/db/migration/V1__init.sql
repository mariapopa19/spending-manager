create table person
(
    id   bigserial primary key,
    name varchar(255) not null
);

create table category
(
    id             bigserial primary key,
    name           varchar(255) not null unique,
    color          varchar(7), -- hex: #RRGGBB
    monthly_budget numeric(12, 2)
);

create table category_rule
(
    id          bigserial primary key,
    pattern     varchar(255) not null,
    category_id bigint       not null references category (id)
);

create table transaction
(
    id          bigserial primary key,
    date        date           not null,
    amount      numeric(12, 2) not null,         -- negative = expense, positive = income
    currency    varchar(3)     not null,         -- ISO code: RON, EUR
    description text           not null,         -- raw bank text, never modified
    merchant    varchar(255),                    -- cleaned display name
    category_id bigint references category (id), -- nullable = needs review
    person_id   bigint         not null references person (id),
    source      varchar(50)    not null,         -- REVOLUT / BT_PAY / BCR_GEORGE / MANUAL
    import_hash varchar(64) unique,              -- sha-256(date|amount|currency|description|source)
    created_at  timestamptz    not null default now()
);

create index idx_transaction_date on transaction (date);
create index idx_transaction_category_id on transaction (category_id);
create index idx_transaction_person_id on transaction (person_id);
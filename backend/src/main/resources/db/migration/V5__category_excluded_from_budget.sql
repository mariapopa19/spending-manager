alter table category
    add column excluded_from_budget boolean not null default false;

update category
set excluded_from_budget = true
where name = 'Economii';
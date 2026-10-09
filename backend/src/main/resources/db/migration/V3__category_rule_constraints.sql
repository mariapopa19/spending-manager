create unique index uq_category_rule_pattern on category_rule (lower(pattern));
create index idx_category_rule_category_id on category_rule (category_id);

-- seed non-spending rules (the Economii category must exist first)
insert into category_rule (pattern, category_id)
select p.pattern, c.id
from (values ('Round Up'), ('Transfer intern'), ('Achizitie unitati de fond')) as p(pattern)
         cross join category c
where c.name = 'Economii';
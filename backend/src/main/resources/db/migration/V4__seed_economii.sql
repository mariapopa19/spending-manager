insert into category (name, color)
values ('Economii', '#90A4AE');

insert into category_rule (pattern, category_id)
select p.pattern, c.id
from (values ('Round Up'), ('Transfer intern'), ('Achizitie unitati de fond')) as p(pattern)
         cross join category c
where c.name = 'Economii';
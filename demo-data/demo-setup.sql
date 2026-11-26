-- Spending Manager — demo setup.
-- NOT a Flyway migration. Run it by hand, once, on a FRESH demo database,
-- BEFORE importing the demo statements (rules are applied at import time).
-- It refuses to run on a database that already has transactions, so it can't touch real data.

do $$
begin
    if exists (select 1 from transaction) then
        raise exception 'demo-setup.sql: this database already has transactions — refusing to run. Use a fresh demo database.';
    end if;
end $$;

begin;

-- Fake people instead of the seeded ones
update person set name = 'Ana'    where name = 'Maria';
update person set name = 'Bogdan' where name = 'Cristi';

-- Budgets for categories the seed leaves empty, so Estimat vs Real has more to compare
update category set monthly_budget = 2200.00 where name = 'Mâncare';
update category set monthly_budget = 150.00  where name = 'Medicale';
update category set monthly_budget = 300.00  where name = 'Mașina 2 (Logan)';

-- Rules matching the demo merchants.
-- Bolt, Dacia Service and Florăria are left WITHOUT rules on purpose, so the review screen has rows.
insert into category_rule (pattern, category_id)
select r.pattern, c.id
from (values
          ('lidl', 'Mâncare'),
          ('kaufland', 'Mâncare'),
          ('mega image', 'Mâncare'),
          ('carrefour', 'Mâncare'),
          ('profi', 'Mâncare'),
          ('auchan', 'Mâncare'),
          ('omv petrom', 'Carburant'),
          ('mol', 'Carburant'),
          ('enel', 'Facturi'),
          ('digi rcs', 'Facturi'),
          ('apa nova', 'Facturi'),
          ('engie', 'Facturi'),
          ('rata credit', 'Rate'),
          ('netflix', 'Abonamente'),
          ('spotify', 'Abonamente'),
          ('youtube', 'Abonamente'),
          ('starbucks', 'Ieșit în oraș'),
          ('trattoria', 'Ieșit în oraș'),
          ('cinema city', 'Ieșit în oraș'),
          ('beraria', 'Ieșit în oraș'),
          ('glovo', 'Ieșit în oraș'),
          ('kfc', 'Ieșit în oraș'),
          ('salad box', 'Ieșit în oraș'),
          ('emag', 'Lifestyle'),
          ('zara', 'Lifestyle'),
          ('catena', 'Medicale'),
          ('dr max', 'Medicale'),
          ('booking.com', 'Vacanță'),
          ('wizz air', 'Vacanță')
     ) as r(pattern, category)
         join category c on c.name = r.category
on conflict do nothing;

commit;

select c.name as category, count(cr.id) as rules, c.monthly_budget
from category c
         left join category_rule cr on cr.category_id = c.id
group by c.name, c.monthly_budget
order by c.name;

# Demo data

Fake bank statements and a setup script for trying the app, or taking screenshots, without real data.
Everything here is invented: people, IBANs, card numbers, merchants' amounts, salaries.

| File | Bank | Import as person | Notes |
|---|---|---|---|
| `bt-pay-demo.csv` | BT Pay | Ana | Salary, bills, groceries, Round Up + savings transfers (→ Economii) |
| `revolut-demo.xlsx` | Revolut | Ana | Subscriptions, Glovo, Bolt, eMAG, a trip paid in **EUR**, one PENDING row (skipped) |
| `bcr-george-demo.csv` | BCR George | Bogdan | Salary, fuel, groceries, car service, trailing summary row (skipped) |
| `demo-setup.sql` | — | — | Renames the seeded people to Ana/Bogdan, sets a few budgets, adds rules for the demo merchants |
| `generate.py` | — | — | Regenerates the three statements for the **last 3 months up to today** |

Bolt, the car service and the florist have **no rules on purpose**, so the review screen has something to show.

## Run the demo (separate from your own data)

`-p sm-demo` gives Docker Compose a separate project, so the demo gets its **own empty database volume**.

```bash
docker compose down                                  # stop your normal stack (frees ports 5432/8080)
docker compose -p sm-demo up --build -d              # fresh DB; wait until the app has started (Flyway creates the schema)

docker compose -p sm-demo cp demo-data/demo-setup.sql postgres:/tmp/demo-setup.sql
docker compose -p sm-demo exec postgres sh -c 'psql -U "$POSTGRES_USER" -d spending_manager -f /tmp/demo-setup.sql'
```

`demo-setup.sql` refuses to run if the database already has transactions, so it can't touch real data.

Then open http://localhost:8080, log in, and import the three files with the person from the table above.
Import one file a second time to see `DUPLICATE` rows in the preview.

Remove the demo (only the demo volume is deleted):

```bash
docker compose -p sm-demo down -v
```

## Regenerate with current dates

The dashboard opens on the current month, so regenerate the files when they get old:

```bash
pip install openpyxl
python demo-data/generate.py
```

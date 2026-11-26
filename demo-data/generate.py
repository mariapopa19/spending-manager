#!/usr/bin/env python3
"""
Generates FAKE bank statements for the Spending Manager demo, in the exact
formats the parsers expect:

    bt-pay-demo.csv        BT Pay CSV      (person: Ana)
    revolut-demo.xlsx      Revolut Excel   (person: Ana)
    bcr-george-demo.csv    BCR George CSV  (person: Bogdan)

Covers the last 3 months up to today. The output is deterministic for a given
day (fixed random seed), so re-running it on the same day gives the same files.

    pip install openpyxl
    python demo-data/generate.py

Every name, IBAN, card number, merchant amount and salary here is invented.
"""

import csv
import datetime as dt
import math
import random
from decimal import ROUND_HALF_UP, Decimal
from pathlib import Path

from openpyxl import Workbook

OUT = Path(__file__).resolve().parent
TODAY = dt.date.today()
rng = random.Random(42)


# ---------------------------------------------------------------- helpers

def months_back(n):
    """The last n months as (year, month), oldest first, ending with the current one."""
    y, m = TODAY.year, TODAY.month
    result = []
    for _ in range(n):
        result.append((y, m))
        m -= 1
        if m == 0:
            y, m = y - 1, 12
    return list(reversed(result))


def last_day(y, m):
    nxt = dt.date(y + (m == 12), m % 12 + 1, 1)
    return (nxt - dt.timedelta(days=1)).day


def day(y, m, d):
    """A date in the month, clamped to the month end; None if it is in the future."""
    date = dt.date(y, m, min(d, last_day(y, m)))
    return date if date <= TODAY else None


def money(lo, hi):
    return Decimal(str(rng.uniform(lo, hi))).quantize(Decimal("0.01"), ROUND_HALF_UP)


def random_days(y, m, count):
    return sorted(rng.sample(range(1, last_day(y, m) + 1), count))


class Ledger:
    """Collects (date, description, signed amount) and keeps rows unique,
    because identical rows in one file would collapse into one at import."""

    def __init__(self):
        self.rows = []
        self._seen = set()

    def add(self, date, description, amount, **extra):
        if date is None:
            return
        key = (date, description, amount)
        while key in self._seen:  # nudge by one ban until unique
            amount += Decimal("-0.01") if amount < 0 else Decimal("0.01")
            key = (date, description, amount)
        self._seen.add(key)
        self.rows.append({"date": date, "description": description, "amount": amount, **extra})

    def sorted(self):
        return sorted(self.rows, key=lambda r: r["date"])


MONTHS = months_back(3)


# ---------------------------------------------------------------- BT Pay (Ana)

def bt_pay():
    ledger = Ledger()
    groceries = ["LIDL 0423 BUCURESTI", "KAUFLAND 1102 BUCURESTI", "MEGA IMAGE 0876 BUCURESTI",
                 "CARREFOUR MARKET 0219", "PROFI 1543 BUCURESTI"]
    going_out = [("STARBUCKS AFI COTROCENI", 18, 38), ("TRATTORIA IL CALCIO", 120, 260),
                 ("CINEMA CITY AFI", 60, 110), ("BERARIA H", 90, 210)]

    def pos(date, merchant, amount):
        if date is None:
            return
        desc = f"Plata la POS;EPOS {date:%d/%m/%Y} {merchant}"
        ledger.add(date, desc, -amount, ref="064POSI")
        # BT Round Up: rounds the payment up to the next 5 lei and moves the change to savings
        if rng.random() < 0.6:
            change = Decimal(math.ceil(amount / 5) * 5) - amount
            if change > 0:
                ledger.add(date, f"Round Up;EPOS {date:%d/%m/%Y} {merchant}", -change, ref="064POSI")

    for y, m in MONTHS:
        ledger.add(day(y, m, 10), f"Incasare OP;ACME SOFTWARE SRL SALARIU {m:02d}/{y}",
                   Decimal("7450.00"), ref="011OPCR")
        ledger.add(day(y, m, 11), "Transfer intern;Catre cont economii", Decimal("-1000.00"), ref="043P2P")
        ledger.add(day(y, m, 12), "Achizitie unitati de fond;BT Maxim", Decimal("-300.00"), ref="077FOND")
        ledger.add(day(y, m, 15), "Plata OP;Rata credit nr 4581273", Decimal("-277.50"), ref="011OPDB")
        ledger.add(day(y, m, 18), "Plata factura;ENEL ENERGIE MUNTENIA", -money(180, 320), ref="091FACT")
        ledger.add(day(y, m, 20), "Plata factura;DIGI RCS RDS", Decimal("-64.50"), ref="091FACT")
        ledger.add(day(y, m, 22), "Plata factura;APA NOVA BUCURESTI", -money(60, 110), ref="091FACT")
        ledger.add(day(y, m, 25), "Plata factura;ENGIE ROMANIA", -money(90, 260), ref="091FACT")

        for d in random_days(y, m, 7):
            pos(day(y, m, d), rng.choice(groceries), money(45, 420))
        for d in random_days(y, m, 4):
            name, lo, hi = rng.choice(going_out)
            pos(day(y, m, d), name, money(lo, hi))
        pos(day(y, m, rng.randint(3, 27)), "CATENA FARMACIA 0341", money(30, 140))

    # Running balance ("Suma"), computed oldest → newest, then written newest first like the bank does
    rows = ledger.sorted()
    balance = Decimal("2350.00")
    for r in rows:
        balance += r["amount"]
        r["balance"] = balance

    def fmt(x):
        return f"{x:,.2f}"  # 1,128.00 — BT uses comma thousands

    start, end = dt.date(*MONTHS[0], 1), TODAY
    preamble = [
        ["Tranzactii"], ["Criterii de cautare"],
        ["Numar cont:", "RO00BTRLRONCRT0000DEMO01 RON"],
        ["Perioada:", f"{start:%d.%m.%Y}-{end:%d.%m.%Y}"],
        [], [], ["Cont"],
        ["Utilizator:", "DEMO ANA"], ["Client:", "DEMO ANA"],
        ["Numar cont:", "RO00BTRLRONCRT0000DEMO01 RON"],
        ["Gasite:", f"{len(rows)} Tranzactii"], ["Rezultat cautare"],
    ]
    header = ["Data procesarii", "Data tranzacției", "Descriere", "Referința tranzacției",
              "Debit", "Credit", "Suma"]

    with open(OUT / "bt-pay-demo.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        for line in preamble:
            w.writerow(line + [""] * (7 - len(line)))
        w.writerow(header)
        for r in reversed(rows):
            processed = min(r["date"] + dt.timedelta(days=1), TODAY)
            debit = fmt(-r["amount"]) if r["amount"] < 0 else ""
            credit = fmt(r["amount"]) if r["amount"] > 0 else ""
            w.writerow([f"{processed:%d.%m.%Y}", f"{r['date']:%d.%m.%Y}", r["description"],
                        r["ref"], debit, credit, fmt(r["balance"])])
    return len(rows)


# ---------------------------------------------------------------- Revolut (Ana)

def revolut():
    ledger = Ledger()
    for y, m in MONTHS:
        ledger.add(day(y, m, 2), "Netflix", Decimal("-55.99"), currency="RON", type="CARD_PAYMENT")
        ledger.add(day(y, m, 5), "Spotify", Decimal("-26.99"), currency="RON", type="CARD_PAYMENT")
        for d in random_days(y, m, 3):
            ledger.add(day(y, m, d), "Glovo", -money(45, 120), currency="RON", type="CARD_PAYMENT")
        for d in random_days(y, m, 4):  # no rule on purpose → shows up on the review screen
            ledger.add(day(y, m, d), "Bolt", -money(15, 48), currency="RON", type="CARD_PAYMENT")
        ledger.add(day(y, m, rng.randint(6, 26)), "eMAG", -money(80, 650), currency="RON", type="CARD_PAYMENT")
        if rng.random() < 0.7:
            ledger.add(day(y, m, rng.randint(6, 26)), "Zara", -money(150, 400), currency="RON",
                       type="CARD_PAYMENT")

    # A trip in the middle month, paid in EUR (shows multi-currency)
    y, m = MONTHS[1]
    ledger.add(day(y, m, 8), "Booking.com", Decimal("-312.40"), currency="EUR", type="CARD_PAYMENT")
    ledger.add(day(y, m, 8), "Wizz Air", Decimal("-89.98"), currency="EUR", type="CARD_PAYMENT")

    wb = Workbook()
    ws = wb.active
    ws.title = "Account statement"
    ws.append(["Type", "Product", "Started Date", "Completed Date", "Description",
               "Amount", "Fee", "Currency", "State", "Balance"])

    balances = {"RON": Decimal("1200.00"), "EUR": Decimal("450.00")}
    rows = ledger.sorted()
    for r in rows:
        balances[r["currency"]] += r["amount"]
        when = dt.datetime.combine(r["date"], dt.time(rng.randint(8, 22), rng.randint(0, 59)))
        ws.append([r["type"], "Current", when, when, r["description"], float(r["amount"]),
                   0.0, r["currency"], "COMPLETED", float(balances[r["currency"]])])

    # A pending payment: the parser must skip it
    now = dt.datetime.combine(TODAY, dt.time(12, 30))
    ws.append(["CARD_PAYMENT", "Current", now, None, "Glovo", -64.20, 0.0, "RON", "PENDING", None])

    for row in ws.iter_rows(min_row=2):
        for cell in (row[2], row[3]):
            cell.number_format = "yyyy-mm-dd hh:mm:ss"
    wb.save(OUT / "revolut-demo.xlsx")
    return len(rows)


# ---------------------------------------------------------------- BCR George (Bogdan)

def bcr_george():
    ledger = Ledger()

    def card(date, merchant, amount, country="RO", tid="40871234"):
        if date is None:
            return
        time = f"{rng.randint(8, 21):02d}:{rng.randint(0, 59):02d}:{rng.randint(0, 59):02d}"
        desc = (f"Tranzactie comerciant - Tranz: Nr card 478900XXXXXX2231, Ref {rng.randint(10**11, 10**12 - 1)}, "
                f"Suma platita {amount} RON. Comision: 0 RON. Locatie: {tid} {country} {merchant}. "
                f"Data_Ora: {date:%d-%m-%Y} {time}")
        ledger.add(date, desc, -amount, hour=time[:5])

    for y, m in MONTHS:
        ledger.add(day(y, m, 9), "Incasare salariu - BETA CONSULTING SRL", Decimal("6200.00"), hour="09:12")
        card(day(y, m, 3), "GOOGLE *YouTubePremium g.co/helpp", Decimal("26.00"), country="GB", tid="99999999")
        for d in random_days(y, m, 2):
            card(day(y, m, d), rng.choice(["OMV PETROM 2041 BUCURESTI", "MOL 1123 BUCURESTI"]), money(100, 165))
        for d in random_days(y, m, 4):
            card(day(y, m, d), rng.choice(["KAUFLAND 1021 BUCURESTI", "AUCHAN MILITARI"]), money(60, 380))
        for d in random_days(y, m, 3):
            card(day(y, m, d), rng.choice(["KFC AFI COTROCENI", "SALAD BOX VICTORIEI"]), money(35, 95))
        card(day(y, m, rng.randint(4, 26)), "DR MAX FARMACIE 112", money(25, 90))

    # No rules for these on purpose → review screen
    card(day(*MONTHS[1], 14), "DACIA SERVICE AUTO MILITARI", Decimal("850.00"))
    card(day(*MONTHS[2], min(TODAY.day, 6)), "FLORARIA MAGNOLIA", Decimal("180.00"))

    rows = ledger.sorted()
    issued, start = TODAY, dt.date(*MONTHS[0], 1)
    header = ["Issuing date of the statement", "Issuing time of the statement", "Starting date", "End date",
              "Currency", "BNR exchange rate", "Statement issued for account", "Product type", "Account owner",
              "First opening accounting balance", "Transaction completion date",
              "Transaction completion hour", "Transaction's details", "Operation's reference",
              "Debit (amount)", "Credit (amount)", "Total debit (amount)", "Total credit (amount)",
              "Final accounting balance", "Blocked amounts", "Available balance",
              "Credit lines available limit"]

    def plain(x):
        return f"{x:.2f}".rstrip("0").rstrip(".") if x else "0"

    balance = Decimal("1840.00")
    with open(OUT / "bcr-george-demo.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(header)
        for i, r in enumerate(rows):
            balance += r["amount"]
            debit = -r["amount"] if r["amount"] < 0 else Decimal(0)
            credit = r["amount"] if r["amount"] > 0 else Decimal(0)
            w.writerow([f"{issued:%d.%m.%Y}", "22:00", f"{start:%d.%m.%Y}", f"{issued:%d.%m.%Y}", "RON", "",
                        "RO00RNCB0000DEMO00000001", "Cont curent", "Demo Bogdan",
                        "1840" if i == 0 else "0", f"{r['date']:%d.%m.%Y}", r["hour"], r["description"],
                        f"{r['date']:%Y%m%d}{rng.randint(10**7, 10**8 - 1)}",
                        plain(debit), plain(credit), plain(debit), plain(credit), "0", "0", "0", "0"])
        # Trailing summary row with a blank description: the parser must skip it
        summary = [""] * 22
        summary[10], summary[20] = f"{issued:%d.%m.%Y}", plain(balance)
        w.writerow(summary)
    return len(rows)


if __name__ == "__main__":
    print(f"Months: {', '.join(f'{m:02d}/{y}' for y, m in MONTHS)} (up to {TODAY})")
    print(f"bt-pay-demo.csv      {bt_pay():>4} transactions")
    print(f"revolut-demo.xlsx    {revolut():>4} transactions (+1 pending, skipped)")
    print(f"bcr-george-demo.csv  {bcr_george():>4} transactions (+1 summary row, skipped)")

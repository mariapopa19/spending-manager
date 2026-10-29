import { useState } from "react";
import type { Source, StatsFilters } from "../../types/domain";
import { usePersons } from "../persons/api";
import { IMPORT_SOURCES } from "../import/types";
import { OverviewCards } from "./OverviewCards";
import { CategoryDonut } from "./CategoryDonut";
import { MonthlyBars } from "./MonthlyBars";
import { BudgetTable } from "./BudgetTable";
import { TopMerchants } from "./TopMerchants";
import { currentMonth } from "../../lib/month";
import { MonthPicker } from "./MonthPicker";

export const DashboardPage = () => {
  const [month, setMonth] = useState(currentMonth);
  const [personId, setPersonId] = useState<number | undefined>();
  const [source, setSource] = useState<Source | undefined>();
  const filters: StatsFilters = { month, personId, source };

  const { data: persons } = usePersons();

  return (
    <main className="mx-auto max-w-6xl space-y-6 p-6">
      <div className="flex flex-wrap items-end gap-3">
        <MonthPicker month={month} onChange={setMonth} />
        <select
          value={personId ?? ""}
          onChange={(event) =>
            setPersonId(event.target.value ? Number(event.target.value) : undefined)
          }
          className="rounded-lg border border-gray-200 px-2 py-1"
        >
          <option value="">All persons</option>
          {persons?.map((person) => (
            <option key={person.id} value={person.id}>
              {person.name}
            </option>
          ))}
        </select>
        <select
          value={source ?? ""}
          onChange={(event) =>
            setSource(event.target.value ? (event.target.value as Source) : undefined)
          }
          className="rounded-lg border border-gray-200 px-2 py-1"
        >
          <option value="">All banks</option>
          {IMPORT_SOURCES.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <OverviewCards filters={filters} />
      <div className="grid gap-6 lg:grid-cols-2">
        <CategoryDonut filters={filters} />
        <MonthlyBars filters={filters} />
      </div>
      <BudgetTable filters={filters} />
      <TopMerchants filters={filters} />
    </main>
  );
};

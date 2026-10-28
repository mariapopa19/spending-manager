import { useState } from "react";
import type { Source, StatsFilters } from "../../types/domain";
import { usePersons } from "../persons/api";
import { IMPORT_SOURCES } from "../import/types";
import { OverviewCards } from "./OverviewCards";
import { CategoryDonut } from "./CategoryDonut";

export const DashboardPage = () => {
  const [month, setMonth] = useState(() =>
    new Date().toISOString().slice(0, 7),
  );
  const [personId, setPersonId] = useState<number | undefined>();
  const [source, setSource] = useState<Source | undefined>();
  const filters: StatsFilters = { month, personId, source };

  const { data: persons } = usePersons();

  return (
    <main className="mx-auto max-w-6xl space-y-6 p-6">
      <div className="flex flex-wrap items-end gap-3">
        <input
          type="month"
          value={month}
          onChange={(e) => setMonth(e.target.value)}
          className="rouded border px-2 py-1"
        />
        <select
          value={personId ?? ""}
          onChange={(e) =>
            setPersonId(e.target.value ? Number(e.target.value) : undefined)
          }
          className="rounded border px-2 py-1"
        >
          <option value="">All persons</option>
          {persons?.map((person: { id: number; name: string }) => (
            <option key={person.id} value={person.id}>
              {person.name}
            </option>
          ))}
        </select>
        <select
          value={source ?? ""}
          onChange={(e) =>
            setSource(e.target.value ? (e.target.value as Source) : undefined)
          }
          className="rounded border px-2 py-1"
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

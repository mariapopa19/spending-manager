import type { StatsFilters } from "../../types/domain";
import { useCategoryTotals } from "./api";
import { BudgetRow } from "./BudgetRow";

export const BudgetTable = ({ filters }: { filters: StatsFilters }) => {
  const { data: categoryTotals = [] } = useCategoryTotals(filters);
  const budgeted = categoryTotals.filter(
    (category) => category.estimated !== null,
  );
  const unbudgeted = categoryTotals.filter(
    (category) => category.estimated === null,
  );

  return (
    <section className="rounded-lg border border-gray-200 p-4">
      <h2 className="mb-2 font-semibold">Estimat vs Real</h2>
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-gray-200 text-left text-xs text-gray-500">
            <th className="py-2 font-medium">Categorie</th>
            <th className="py-2 text-right font-medium">Estimat</th>
            <th className="py-2 text-right font-medium">Real</th>
            <th className="py-2 text-right font-medium">Diferență</th>
            <th className="w-40"></th>
          </tr>
        </thead>
        <tbody>
          {budgeted.map((category) => (
            <BudgetRow key={category.name} category={category} />
          ))}
          {unbudgeted.length > 0 && (
            <tr>
              <td
                colSpan={5}
                className="pt-5 pb-1 text-xs font-semibold uppercase text-gray-400"
              >
                Fără buget
              </td>
            </tr>
          )}
          {unbudgeted.map((category) => (
            <BudgetRow key={category.name} category={category} />
          ))}
        </tbody>
      </table>
    </section>
  );
};

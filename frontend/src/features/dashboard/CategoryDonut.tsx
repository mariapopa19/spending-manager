import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import type { StatsFilters } from "../../types/domain";
import { useCategoryTotals } from "./api";
import { formatMoney } from "../../lib/money";

export const CategoryDonut = ({ filters }: { filters: StatsFilters }) => {
  const { data = [] } = useCategoryTotals(filters);
  const slices = data?.filter((category) => category.actual > 0);

  return (
    <section className="rounded-lg border p-4">
      <h2 className="mb-2 font-semibold">Spending by category</h2>
      {slices?.length === 0 ? (
        <p className="text-gray-500">No spending this month.</p>
      ) : (
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={slices}
                dataKey="actual"
                nameKey="name"
                innerRadius="55%"
                outerRadius="85%"
              >
                {slices?.map((c) => (
                  <Cell key={c.name} fill={c.color ?? "#9E9E9E"} />
                ))}
              </Pie>
              <Tooltip formatter={(v) => formatMoney(Number(v), "RON")} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      )}
    </section>
  );
};

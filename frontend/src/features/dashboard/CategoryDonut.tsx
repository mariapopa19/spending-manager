import {
  Pie,
  PieChart,
  ResponsiveContainer,
  Sector,
  Tooltip,
  type PieSectorShapeProps,
} from "recharts";
import type { StatsFilters } from "../../types/domain";
import { useCategoryTotals } from "./api";
import { formatMoney } from "../../lib/money";

export const CategoryDonut = ({ filters }: { filters: StatsFilters }) => {
  const { data: categoryTotals = [], isLoading } = useCategoryTotals(filters);
  const slices = categoryTotals.filter((category) => category.actual > 0);

  const CategorySlice = (sliceProps: PieSectorShapeProps) => (
    <Sector
      {...sliceProps}
      fill={slices[sliceProps.index]?.color ?? "#9E9E9E"}
    />
  );

  const renderContent = () => {
    if (isLoading) {
      return <p className="text-gray-500">Loading...</p>;
    }

    if (slices.length === 0) {
      return <p className="text-gray-500">No spending this month.</p>;
    }

    return (
      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={slices}
              dataKey="actual"
              nameKey="name"
              innerRadius="55%"
              outerRadius="85%"
              shape={CategorySlice}
            />
            <Tooltip formatter={(value) => formatMoney(Number(value), "RON")} />
          </PieChart>
        </ResponsiveContainer>
      </div>
    );
  };

  return (
    <section className="rounded-lg border border-gray-200 p-4">
      <h2 className="mb-2 font-semibold">Spending by category</h2>
      {renderContent()}
    </section>
  );
};

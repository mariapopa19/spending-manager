import {
  Bar,
  BarChart,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { StatsFilters } from "../../types/domain";
import { useMonthly } from "./api";
import { formatMoney } from "../../lib/money";
import { formatMonth, monthRange, shiftMonth } from "../../lib/month";

export const MonthlyBars = ({ filters }: { filters: StatsFilters }) => {
  const { month, ...otherFilters } = filters;
  const fromMonth = shiftMonth(month, -5);
  const { data: monthlyTotals = [] } = useMonthly(
    fromMonth,
    month,
    otherFilters,
  );
  const chartData = monthRange(fromMonth, month).map((chartMonth) => {
    const total = monthlyTotals.find(
      (monthlyTotal) => monthlyTotal.month === chartMonth,
    );
    return {
      label: formatMonth(chartMonth),
      income: total?.income ?? 0,
      expenses: total?.expenses ?? 0,
    };
  });

  return (
    <section className="rounded-lg border border-gray-200 p-4">
      <h2 className="mb-2 font-semibold">Last 6 months</h2>
      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData}>
            <XAxis dataKey="label" />
            <YAxis />
            <Tooltip formatter={(value) => formatMoney(Number(value), "RON")} />
            <Legend />
            <Bar dataKey="income" name="Income" fill="#43A047" />
            <Bar dataKey="expenses" name="Expenses" fill="#E53935" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </section>
  );
};

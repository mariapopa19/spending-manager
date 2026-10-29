import { formatMoney } from "../../lib/money";
import type { CategoryTotal } from "../../types/domain";

export const BudgetRow = ({ category }: { category: CategoryTotal }) => {
  const color = category.color ?? "#9E9E9E";
  const difference =
    category.estimated === null ? null : category.estimated - category.actual;
  const percentSpent = category.estimated
    ? Math.min(100, (category.actual / category.estimated) * 100)
    : 0;
  const isOverBudget = difference !== null && difference < 0;

  return (
    <tr className="border-b border-gray-100">
      <td className="py-2">
        <span className="flex items-center gap-2">
          <span
            className="h-2.5 w-2.5 shrink-0 rounded-full"
            style={{ backgroundColor: color }}
          />
          {category.name}
        </span>
      </td>
      <td className="py-2 text-right text-gray-500">
        {category.estimated === null
          ? "-"
          : formatMoney(category.estimated, "RON")}
      </td>
      <td className="py-2 text-right">{formatMoney(category.actual, "RON")}</td>
      <td
        className={`py-2 text-right ${isOverBudget ? "text-red-600" : "text-gray-500"}`}
      >
        {difference === null ? "-" : formatMoney(difference, "RON")}
      </td>
      <td className="py-2 pl-4">
        {difference !== null && (
          <div className="h-1.5 rounded-full bg-gray-100">
            <div
              className="h-1.5 rounded-full"
              style={{
                width: `${percentSpent}%`,
                backgroundColor: isOverBudget ? "#DC2626" : color,
              }}
            />
          </div>
        )}
      </td>
    </tr>
  );
};

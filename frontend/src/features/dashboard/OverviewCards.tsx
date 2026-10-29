import { Link } from "react-router-dom";
import { formatMoney } from "../../lib/money";
import { useOverview } from "./api";
import type { StatsFilters } from "../../types/domain";

export const OverviewCards = ({ filters }: { filters: StatsFilters }) => {
  const { data, isLoading, isError } = useOverview(filters);
  if (isLoading) return <p>Loading...</p>;
  if (isError || !data)
    return <p className="text-red-600">Could not load overview.</p>;

  const cards = [
    { label: "Venituri", value: data.income },
    { label: "Cheltuieli", value: data.expenses },
    { label: "Estimat", value: data.estimated },
    { label: "Bani Rămași", value: data.remaining, emphasize: true },
  ];

  return (
    <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
      {cards.map((card) => (
        <div key={card.label} className="rounded-lg border border-gray-200 p-4">
          <div className="text-sm text-gray-500">{card.label}</div>
          <div
            className={`text-2xl font-semibold ${card.emphasize && card.value < 0 ? "text-red-600" : ""}`}
          >
            {formatMoney(card.value, data.currency)}
          </div>
        </div>
      ))}
      {data.uncategorizedCount > 0 && (
        <Link
          to="/review"
          className="col-span-full text-sm text-amber-700 underline"
        >
          {data.uncategorizedCount} transactions still need a category
        </Link>
      )}
    </div>
  );
};

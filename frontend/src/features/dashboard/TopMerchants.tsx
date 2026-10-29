import { formatMoney } from "../../lib/money";
import type { StatsFilters } from "../../types/domain";
import { useTopMerchants } from "./api";

export const TopMerchants = ({ filters }: { filters: StatsFilters }) => {
  const { data: merchantTotals = [], isLoading } = useTopMerchants(filters);
  return (
    <section className="rounded-lg border border-gray-200 p-4">
      <h2 className="mb-2 font-semibold">Top 10 comercianți</h2>
      {!isLoading && merchantTotals.length === 0 ? (
        <p className="text-gray-500">Nicio tranzacție luna aceasta.</p>
      ) : (
        <ol className="space-y-1 text-sm">
          {merchantTotals.map((merchantTotal) => (
            <li
              key={merchantTotal.merchant}
              className="flex justify-between gap-4"
            >
              <span className="truncate">{merchantTotal.merchant}</span>
              <span className="shrink-0">
                {formatMoney(merchantTotal.total, "RON")} ·{" "}
                {merchantTotal.count}×
              </span>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
};

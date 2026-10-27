import { useQuery } from "@tanstack/react-query";
import type {
  CategoryTotal,
  MerchantTotal,
  MonthlyTotal,
  Overview,
  StatsFilters,
} from "../../types/domain";
import { api } from "../../lib/apiClient";

const params = (filter: StatsFilters) => ({
  month: filter.month,
  personId: filter.personId,
  source: filter.source,
});

export const useOverview = (filter: StatsFilters) =>
  useQuery({
    queryKey: ["stats", "overview", filter],
    queryFn: async () =>
      (await api.get<Overview>("/stats/overview", { params: params(filter) }))
        .data,
  });

export const useCategoryTotals = (filter: StatsFilters) =>
  useQuery({
    queryKey: ["stats", "categories", filter],
    queryFn: async () =>
      (
        await api.get<CategoryTotal[]>("/stats/categories", {
          params: params(filter),
        })
      ).data,
  });

export const useTopMerchants = (filter: StatsFilters) =>
  useQuery({
    queryKey: ["stats", "merchants", filter],
    queryFn: async () =>
      (
        await api.get<MerchantTotal[]>("/stats/merchants", {
          params: params(filter),
        })
      ).data,
  });

export const useMonthly = (
  from: string,
  to: string,
  filter: Omit<StatsFilters, "month">,
) =>
  useQuery({
    queryKey: ["stats", "monthly", from, to, filter],
    queryFn: async () =>
      (
        await api.get<MonthlyTotal[]>("/stats/monthly", {
          params: {
            from,
            to,
            personId: filter.personId,
            source: filter.source,
          },
        })
      ).data,
  });

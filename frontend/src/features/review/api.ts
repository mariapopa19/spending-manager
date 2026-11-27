import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { api } from "../../lib/apiClient";
import type { UpdateArgs } from "../../lib/api.types";
import type { Page, Transaction } from "../../types/domain";

export type SetCategoryInput = {
  categoryId: number;
  createRule?: { pattern: string };
};

export const useUncategorized = (
  page: number,
  personId?: number,
  size: number = 20,
) => {
  return useQuery({
    queryKey: ["transactions", "uncategorized", { page, size, personId }],
    queryFn: async () =>
      (
        await api.get<Page<Transaction>>("/transactions", {
          params: {
            uncategorized: true,
            personId,
            page,
            size,
            sort: "date,desc",
          },
        })
      ).data,
    placeholderData: keepPreviousData,
  });
};

export const useSetCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, input }: UpdateArgs<SetCategoryInput>) => {
      const response = await api.patch<Transaction>(
        `/transactions/${id}/category`,
        input,
      );
      return response.data;
    },
    onSuccess: async () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ["transactions"] }),
        queryClient.invalidateQueries({ queryKey: ["rules"] }),
        queryClient.invalidateQueries({ queryKey: ["stats"] }),
      ]),
  });
};

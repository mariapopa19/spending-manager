import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../lib/apiClient";
import type { UpdateArgs } from "../../lib/api.types";
import type { Page, Transaction } from "../../types/domain";

export type SetcategoryInput = {
  categoryId: number;
  createRule?: { pattern: string };
};

export const useUncategorized = (page: number, size: number = 20) => {
  return useQuery({
    queryKey: ["transactions", "uncategorized", page, size],
    queryFn: async () => {
      const response = await api.get<Page<Transaction>>(
        `/transactions?uncategorized=true&page=${page}&size=${size}&sort=date,desc`,
      );
      return response.data;
    },
  });
};

export const useSetCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, input }: UpdateArgs<SetcategoryInput>) => {
      const response = await api.patch<Transaction>(
        `/transactions/${id}/category`,
        input,
      );
      return response.data;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["transactions"] });
      await queryClient.invalidateQueries({ queryKey: ["rules"] }); // a rule may have been created
    },
  });
};

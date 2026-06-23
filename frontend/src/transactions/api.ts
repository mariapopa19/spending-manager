import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Page, Source, Transaction } from "../types/domain";
import { api } from "../lib/apiClient";

export type TransactionInput = {
  date: string;
  amount: number;
  currency: string;
  description: string;
  merchant: string | null;
  categoryId: number | null;
  personId: number;
  source: Source;
};

export const useTransactions = (page: number, size: number = 20) => {
  return useQuery({
    queryKey: ["transactions", page, size],
    queryFn: async () => {
      const response = await api.get<Page<Transaction>>(
        `/transactions?page=${page}&size=${size}&sort=date,desc`,
      );
      return response.data;
    },
  });
};

export const useCreateTransaction = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: TransactionInput) => {
      const response = await api.post<Transaction>("/transactions", input);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
};

export const useDeleteTransaction = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/transactions/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
};

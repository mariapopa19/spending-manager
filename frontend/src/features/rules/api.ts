import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../lib/apiClient";
import type { CategoryRule } from "../../types/domain";
import type { UpdateArgs } from "../../lib/api.types";

export type RuleInput = {
  pattern: string;
  categoryId: number;
};

export const useRules = () => {
  return useQuery({
    queryKey: ["rules"],
    queryFn: async () => {
      const response = await api.get<CategoryRule[]>("/category-rules");
      return response.data;
    },
  });
};

export const useCreateRule = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: RuleInput) => {
      const response = await api.post<CategoryRule>("/category-rules", input);
      return response.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["rules"] }),
  });
};

export const useDeleteRule = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/category-rules/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["rules"] }),
  });
};

export const useUpdateRule = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, input }: UpdateArgs<RuleInput>) => {
      const response = await api.put<CategoryRule>(
        `/category-rules/${id}`,
        input,
      );
      return response.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["rules"] }),
  });
};

export const useApplyRules = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const response = await api.post<{ updated: number }>(
        "/transactions/apply-rules",
      );
      return response.data;
    },
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["transactions"] }),
  });
};

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/apiClient";
import type { Category } from "../types/domain";
import type { UpdateArgs } from "../lib/api.types";

export type CategoryInput = {
  name: string;
  color: string | null;
  monthlyBudget: number | null;
};

// read: all categories
export const useCategories = () => {
  return useQuery({
    queryKey: ["categories"], // identity key in cache
    queryFn: async () => {
      const response = await api.get<Category[]>("/categories");
      return response.data;
    },
  });
};

// create
export const useCreateCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: CategoryInput) => {
      const response = await api.post<Category>("/categories", input);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["categories"] });
    },
  });
};

// update
export const useUpdateCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, input }: UpdateArgs<CategoryInput>) => {
      const response = await api.put<Category>(`/categories/${id}`, input);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["categories"] });
    },
  });
};

// delete
export const useDeleteCategory = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/categories/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["categories"] });
    },
  });
};

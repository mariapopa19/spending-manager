import { useQuery } from "@tanstack/react-query";
import type { Person } from "../types/domain";
import { api } from "../lib/apiClient";

export const usePersons = () => {
  return useQuery({
    queryKey: ["persons"],
    queryFn: async () => {
      const response = await api.get<Person[]>("/persons");
      return response.data;
    },
  });
};

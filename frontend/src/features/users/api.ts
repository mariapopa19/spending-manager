import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { User, UserRole } from "../../types/domain";
import { api } from "../../lib/apiClient";

export type UserInput = {
  username: string;
  password: string;
  role: UserRole;
};

export type PasswordChange = {
  oldPassword: string;
  newPassword: string;
};

export const useUsers = () => {
  return useQuery({
    queryKey: ["users"],
    queryFn: async () => {
      const response = await api.get<User[]>("/users");
      return response.data;
    },
  });
};

export const useCreateUser = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: UserInput) => {
      const response = await api.post<User>("/users", input);
      return response.data;
    },
    onSuccess: () => {
      return queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });
};

export const useChangePassword = () => {
  return useMutation({
    mutationFn: async (input: PasswordChange) => {
      const response = await api.put("/users/me/password", input);
      return response.data;
    },
  });
};

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../lib/apiClient";
import { ApiError } from "../../lib/ApiError";
import { useNavigate } from "react-router-dom";
import type { UserRole } from "../../types/domain";

export type LoginInput = {
  username: string;
  password: string;
};

type MeResponse = { username: string; role: UserRole };

export const useMe = () => {
  return useQuery({
    queryKey: ["me"],
    queryFn: async () => {
      try {
        const response = await api.get<MeResponse>("/auth/me");
        return response.data;
      } catch (e) {
        if (e instanceof ApiError && e.status === 401) return null;
        throw e;
      }
    },
    retry: false,
  });
};

export const useLogin = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (input: LoginInput) => {
      const response = await api.post(
        "/auth/login",
        new URLSearchParams(input),
      );
      return response.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["me"] }),
  });
};

export const useLogout = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  return useMutation({
    mutationFn: async () => {
      const response = await api.post("/auth/logout");
      return response.data;
    },
    onSettled: async () => {
      queryClient.clear();
      await navigate("/login");
    },
  });
};

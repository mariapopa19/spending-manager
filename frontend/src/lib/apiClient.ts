import axios from "axios";
import { ApiError } from "./ApiError";

export const api = axios.create({
  baseURL: "/api",
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ?? error.message ?? "Cererea a eșuat";
     return Promise.reject(new ApiError(message, error.response?.status));
  },
);

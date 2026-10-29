import axios from "axios";

export const api = axios.create({
  baseURL: "/api",
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ?? error.message ?? "Cererea a eșuat";
    return Promise.reject(new Error(message));
  },
);

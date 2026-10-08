import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { Source } from "../../types/domain";
import type {
  ImportConfirmRequest,
  ImportPreviewResponse,
  ImportResultResponse,
} from "./types";
import { api } from "../../lib/apiClient";

export const usePreviewImport = () =>
  useMutation({
    mutationFn: async ({ source, file }: { source: Source; file: File }) => {
      const formData = new FormData();
      formData.append("source", source);
      formData.append("file", file);

      const response = await api.post<ImportPreviewResponse>(
        "/imports/preview",
        formData,
      );
      return response.data;
    },
  });

export const useConfirmImport = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (request: ImportConfirmRequest) => {
      const response = await api.post<ImportResultResponse>(
        "/imports/confirm",
        request,
      );
      return response.data;
    },
    onSuccess: () => {
      return queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
};

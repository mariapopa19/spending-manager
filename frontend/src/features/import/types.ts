import type { Source } from "../../types/domain";

export type ImportStatus = "NEW" | "DUPLICATE";

export type ImportPreviewRow = {
  date: string;   // LocalDate serialises as "2026-08-14"
  amount: number;  // BigDecimal — display only, never summed here
  currency: string;
  description: string;
  status: ImportStatus;
};

export type ImportPreviewResponse = {
  total: number;
  newCount: number;
  duplicateCount: number;
  rows: ImportPreviewRow[];
};

export type ImportRowRequest = {
  date: string;
  amount: number;
  currency: string;
  description: string;
};

export type ImportConfirmRequest = {
  source: Source;
  personId: number;
  rows: ImportRowRequest[];
};

export type ImportResultResponse = {
    imported: number;
    skipped: number;
}

export const IMPORT_SOURCES = [
  { value: "REVOLUT",    label: "Revolut",    accept: ".xlsx" },
  { value: "BT_PAY",     label: "BT Pay",     accept: ".csv"  },
  { value: "BCR_GEORGE", label: "BCR George", accept: ".csv"  },
] as const satisfies ReadonlyArray<{ value: Source; label: string; accept: string }>;

export type Wizard =
  | { step: "upload" }
  | { step: "review"; preview: ImportPreviewResponse }
  | { step: "done"; result: ImportResultResponse };

export type WizardStep = Wizard["step"];


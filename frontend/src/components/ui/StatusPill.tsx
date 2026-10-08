import type { ImportStatus } from "../../features/import/types";

const styles: Record<ImportStatus, string> = {
  NEW: "border-green-700 bg-green-50 text-green-800",
  DUPLICATE: "border-amber-700 bg-amber-50 text-amber-800",
};

const labels: Record<ImportStatus, string> = {
  NEW: "new",
  DUPLICATE: "aleady imported",
};

export const StatusPill = ({ status }: { status: ImportStatus }) => (
  <span
    className={`inline-block rounded border px-2 py-0.5 text-xs font-medium uppercase tracking-wide ${styles[status]}`}
  >
    {labels[status]}
  </span>
);

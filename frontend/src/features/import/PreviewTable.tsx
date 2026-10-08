import { Button } from "../../components/ui/Button";
import { StatusPill } from "../../components/ui/StatusPill";
import { formatMoney } from "../../lib/money";
import type { ImportPreviewResponse } from "./types";

type Props = {
  preview: ImportPreviewResponse;
  selected: Set<number>;
  onToggle: (index: number) => void;
  onConfirm: () => void;
  onBack: () => void;
  isPending: boolean;
  error: Error | null;
};

export const PreviewTable = ({
  preview,
  selected,
  onToggle,
  onConfirm,
  onBack,
  isPending,
  error,
}: Props) => (
  <div className="flex flex-col gap-4">
    <p className="text-sm text-gray-600">
      {preview.total} rows · <strong>{preview.newCount}</strong> new ·{" "}
      <strong>{preview.duplicateCount}</strong> already imported ·{" "}
      <strong>{selected.size}</strong> selected
    </p>

    <div className="overflow-x-auto rounded-lg border">
      <table className="w-full text-left text-sm">
        <thead className="bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
          <tr>
            <th className="w-10 px-3 py-2" />
            <th className="px-3 py-2">Date</th>
            <th className="px-3 py-2">Description</th>
            <th className="px-3 py-2 text-right">Amount</th>
            <th className="px-3 py-2">Status</th>
          </tr>
        </thead>
        <tbody className="divide-y">
          {preview.rows.map((row, index) => (
            <tr
              key={index}
              className={
                row.status === "DUPLICATE" ? "bg-gray-50 text-gray-500" : ""
              }
            >
              <td className="px-3 py-2">
                <input
                  type="checkbox"
                  className="h-4 w-4"
                  checked={selected.has(index)}
                  onChange={() => onToggle(index)}
                />
              </td>
              <td className="whitespace-nowrap px-3 py-2">{row.date}</td>
              <td className="px-3 py-2">{row.description}</td>
              <td className="whitespace-nowrap px-3 py-2 text-right font-mono">
                {formatMoney(row.amount, row.currency)}
              </td>
              <td className="px-3 py-2">
                <StatusPill status={row.status} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>

    {error && (
      <p
        role="alert"
        className="rounded border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700"
      >
        {error.message}
      </p>
    )}
    <div className="flex gap-2">
      <Button variant="ghost" onClick={onBack}>
        Back
      </Button>
      <Button onClick={onConfirm} disabled={isPending || selected.size === 0}>
        Import {selected.size} row
        {selected.size === 1 ? "" : "s"}
      </Button>
    </div>
  </div>
);

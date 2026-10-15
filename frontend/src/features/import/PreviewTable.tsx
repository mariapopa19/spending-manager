import { Button } from "../../components/ui/Button";
import { StatusPill } from "../../components/ui/StatusPill";
import { textColorFor } from "../../lib/contrast";
import { formatMoney } from "../../lib/money";
import type { Category } from "../../types/domain";
import type { ImportPreviewResponse, ImportPreviewRow } from "./types";

type Props = {
  preview: ImportPreviewResponse;
  selected: Set<number>;
  categories: Category[];
  categoryIdFor: (index: number, row: ImportPreviewRow) => number | null;
  onCategoryChange: (index: number, categoryId: number | null) => void;
  onToggle: (index: number) => void;
  onConfirm: () => void;
  onBack: () => void;
  isPending: boolean;
  error: Error | null;
};

export const PreviewTable = ({
  preview,
  selected,
  categories,
  categoryIdFor,
  onCategoryChange,
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
            <th className="px-3 py-2">Category</th>
          </tr>
        </thead>
        <tbody className="divide-y">
          {preview.rows.map((row, index) => {
            const current = categories.find(
              (c) => c.id === categoryIdFor(index, row),
            );

            return (
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
                <td>
                  <span
                    className="mr-2 inline-block h-3 w-3 rounded-full align-middle"
                    style={{ backgroundColor: current?.color ?? "transparent" }}
                  />
                  <select
                    className="border rounded px-3 py-2"
                    value={categoryIdFor(index, row) ?? ""}
                    onChange={(e) =>
                      onCategoryChange(
                        index,
                        e.target.value === "" ? null : Number(e.target.value),
                      )
                    }
                    disabled={row.status === "DUPLICATE"}
                  >
                    <option value="">No category</option>
                    {categories.map((category) => (
                      <option
                        key={category.id}
                        value={category.id}
                        style={{
                          backgroundColor: category.color ?? "#999",
                          color: textColorFor(category.color ?? "#999"),  
                        }}
                      >
                        {category.name}
                      </option>
                    ))}
                  </select>
                </td>
              </tr>
            );
          })}
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

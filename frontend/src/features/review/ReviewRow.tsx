import { useState } from "react";
import type { Category, Transaction } from "../../types/domain";
import { useSetCategory } from "./api";
import { formatMoney } from "../../lib/money";
import { Button } from "../../components/ui/Button";
import { textColorFor, tintStyle } from "../../lib/contrast";

type Props = {
  transaction: Transaction;
  categories: Category[];
};

export const ReviewRow = ({ transaction, categories }: Props) => {
  const setCategory = useSetCategory();
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [always, setAlways] = useState(false);
  const [pattern, setPattern] = useState(
    transaction.merchant ?? transaction.description,
  );
  const [expanded, setExpanded] = useState(false);

  const patternValid = pattern.trim().length >= 3;
  const canSave = categoryId !== "" && (!always || patternValid);

  const handleSave = () => {
    if (categoryId === "") return;
    setCategory.mutate({
      id: transaction.id,
      input: {
        categoryId,
        createRule: always ? { pattern: pattern.trim() } : undefined,
      },
    });
  };

  const busy = setCategory.isPending;
  const current = categories.find((c) => c.id === categoryId);

  return (
    <li className="rounded-lg border bg-white p-4">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <p className="text-xs text-gray-500">{transaction.date}</p>
          <button
            type="button"
            className={`w-full appearance-none bg-transparent p-0 text-left text-sm wrap-break-word ${expanded ? "" : "line-clamp-2"}`}
            onClick={() => setExpanded((e) => !e)}
            title={expanded ? "Click to collapse" : "Click to expand"}
          >
            {transaction.description}
          </button>
        </div>
        <p
          className={`whitespace-nowrap font-mono text-sm font-semibold ${
            Number(transaction.amount) < 0 ? "text-red-600" : "text-green-700"
          }`}
        >
          {formatMoney(transaction.amount, transaction.currency)}
        </p>
      </div>

      <div className="mt-3 flex flex-wrap items-center gap-3 border-t pt-3">
        <select
          className="rounded-full border py-1.5 pl-4 pr-8 text-sm font-medium"
          style={tintStyle(current?.color)}
          value={categoryId}
          disabled={busy}
          onChange={(e) =>
            setCategoryId(e.target.value === "" ? "" : Number(e.target.value))
          }
        >
          <option value="">Choose...</option>
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

        <label className="flex items-center gap-2 whitespace-nowrap text-sm text-gray-700">
          <input
            type="checkbox"
            className="h-4 w-4"
            checked={always}
            disabled={busy}
            onChange={(e) => setAlways(e.target.checked)}
          />
          Always do this
        </label>

        {always && (
          <input
            className="w-56 rounded border px-3 py-1.5 text-sm"
            placeholder="Text to match"
            value={pattern}
            disabled={busy}
            onChange={(e) => setPattern(e.target.value)}
          />
        )}

        <Button
          className="ml-auto"
          onClick={handleSave}
          disabled={!canSave || busy}
        >
          Save
        </Button>
      </div>

      {always && !patternValid && (
        <p className="mt-2 text-xs text-red-600">
          Pattern needs at least 3 characters
        </p>
      )}
      {setCategory.error && (
        <p role="alert" className="mt-2 text-xs text-red-600">
          {setCategory.error.message}
        </p>
      )}
    </li>
  );
};

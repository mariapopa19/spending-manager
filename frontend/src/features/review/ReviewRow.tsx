import { useState } from "react";
import type { Category, Transaction } from "../../types/domain";
import { useSetCategory } from "./api";
import { formatMoney } from "../../lib/money";
import { Button } from "../../components/ui/Button";
import { textColorFor } from "../../lib/contrast";

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
    <tr>
      <td className="whitespace-nowrap px-3 py-2">{transaction.date}</td>
      <td className="px-3 py-2">{transaction.description}</td>
      <td className="whitespace-nowrap px-3 py-2 text-right font-mono">
        {formatMoney(transaction.amount, transaction.currency)}
      </td>
      <td className="px-3 py-2">
        <span
          className="mr-2 inline-block h-3 w-3 rounded-full align-middle"
          style={{ backgroundColor: current?.color ?? "transparent" }}
        />
        <select
          className="rounded border px-3 py-2"
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
      </td>
      <td className="px-3 py-2">
        <label className="flex items-center gap-2 text-sm">
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
            className="mt-2 w-full rounded border px-3 py-2"
            value={pattern}
            disabled={busy}
            onChange={(e) => setPattern(e.target.value)}
          />
        )}
        {always && !patternValid && (
          <p className="mt-1 text-xs text-red-600">At least 3 characters</p>
        )}
        {setCategory.error && (
          <p role="alert" className="mt-1 text-xs text-red-600">
            {setCategory.error.message}
          </p>
        )}
      </td>
      <td className="px-3 py-2 text-right">
        <Button onClick={handleSave} disabled={!canSave || busy}>
          Save
        </Button>
      </td>
    </tr>
  );
};

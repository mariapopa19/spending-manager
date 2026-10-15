import { useState } from "react";
import type { Category, Transaction } from "../../types/domain";
import { useSetCategory } from "./api";
import { formatMoney } from "../../lib/money";
import { Button } from "../../components/ui/Button";

type Props = {
  transaction: Transaction;
  categories: Category[];
};

export const ReviewRow = ({ transaction, categories }: Props) => {
  const setCategory = useSetCategory();
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [always, setAlways] = useState(false);
  const [pattern, setPattern] = useState(transaction.merchant ?? "");

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

  return (
    <tr>
      <td>{transaction.date}</td>
      <td>{transaction.description}</td>
      <td>{formatMoney(transaction.amount, transaction.currency)}</td>
      <td>
        <select
          value={categoryId}
          onChange={(e) =>
            setCategoryId(e.target.value === "" ? "" : Number(e.target.value))
          }
        >
          <option value="">Choose...</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </td>
      <td>
        <label>
          <input
            type="checkbox"
            checked={always}
            onChange={(e) => setAlways(e.target.checked)}
          />
          {" "}Always do this
        </label>
        {always && (
          <input value={pattern} onChange={(e) => setPattern(e.target.value)} />
        )}
        {always && !patternValid && <p>At least 3 characters</p>}
      </td>
      <td>
        <Button
          onClick={handleSave}
          disabled={!canSave || setCategory.isPending}
        >
          Save
        </Button>
      </td>
    </tr>
  );
};

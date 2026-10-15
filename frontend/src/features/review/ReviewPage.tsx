import { useState } from "react";
import { ReviewRow } from "./ReviewRow";
import { useUncategorized } from "./api";
import { useCategories } from "../categories/api";

export const ReviewPage = () => {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError } = useUncategorized(page);
  const { data: categories } = useCategories();

  if (isLoading || !categories) return <p>Loading…</p>;
  if (isError || !data) return <p>Something went wrong.</p>;
  if (data.totalElements === 0) return <p>Nothing to review 🎉</p>;

  return (
    <table>
      <tbody>
        {data.content.map((t) => (
          <ReviewRow key={t.id} transaction={t} categories={categories} />
        ))}
      </tbody>
    </table>
    // + pagination, same approach as TransactionsPage
  );
};

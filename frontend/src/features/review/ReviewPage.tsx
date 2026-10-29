import { useState } from "react";
import { Button } from "../../components/ui/Button";
import { ReviewRow } from "./ReviewRow";
import { useUncategorized } from "./api";
import { useCategories } from "../categories/api";

export const ReviewPage = () => {
  const [page, setPage] = useState(0);
  const { data: pageData, isLoading, isError } = useUncategorized(page);
  const { data: categories } = useCategories();

  if (isLoading || !categories)
    return <p className="mx-auto max-w-6xl p-6">Se încarcă...</p>;
  if (isError || !pageData)
    return (
      <p className="mx-auto max-w-6xl p-6 text-red-600">
        A apărut o eroare.
      </p>
    );
  if (pageData.page.totalElements === 0)
    return (
      <p className="mx-auto max-w-6xl p-6 text-gray-500">Nimic de revizuit 🎉</p>
    );
  if (pageData.content.length === 0 && pageData.page.number > 0) {
    setPage(pageData.page.totalPages - 1); // setState during render, guarded
    return null;
  }

  const isFirstPage = pageData.page.number === 0;
  const isLastPage = pageData.page.number + 1 >= pageData.page.totalPages;

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">De revizuit</h1>
      <ul className="flex flex-col gap-3">
        {pageData.content.map((transaction) => (
          <ReviewRow
            key={transaction.id}
            transaction={transaction}
            categories={categories}
          />
        ))}
      </ul>
      {/* Pagination */}
      <div className="flex items-center justify-between">
        <span className="text-sm text-gray-500">
          Pagina {pageData.page.number + 1} din {pageData.page.totalPages} ·{" "}
          {pageData.page.totalElements} în total
        </span>
        <div className="flex gap-2">
          <Button
            variant="ghost"
            onClick={() => setPage((p) => p - 1)}
            disabled={isFirstPage}
          >
            Anterioară
          </Button>
          <Button
            variant="ghost"
            onClick={() => setPage((p) => p + 1)}
            disabled={isLastPage}
          >
            Următoare
          </Button>
        </div>
      </div>
    </div>
  );
};

import { useState } from "react";
import { Button } from "../../components/ui/Button";
import { ReviewRow } from "./ReviewRow";
import { useUncategorized } from "./api";
import { useCategories } from "../categories/api";

export const ReviewPage = () => {
  const [page, setPage] = useState(0);
  const { data: pageData, isLoading, isError } = useUncategorized(page);
  const { data: categories } = useCategories();

  if (isLoading || !categories) return <p className="p-4">Loading…</p>;
  if (isError || !pageData)
    return <p className="p-4 text-red-600">Something went wrong.</p>;
  if (pageData.totalElements === 0)
    return <p className="p-4 text-gray-500">Nothing to review 🎉</p>;
  if (pageData.content.length === 0 && pageData.number > 0) {
    setPage(pageData.totalPages - 1); // setState during render, guarded
    return null;
  }

  return (
    <div className="flex flex-col gap-4 p-4">
      <h1 className="text-xl font-semibold">Needs review</h1>
      <div className="overflow-x-auto rounded-lg border">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
            <tr>
              <th className="px-3 py-2">Date</th>
              <th className="px-3 py-2">Description</th>
              <th className="px-3 py-2 text-right">Amount</th>
              <th className="px-3 py-2">Category</th>
              <th className="px-3 py-2">Rule</th>
              <th className="px-3 py-2" />
            </tr>
          </thead>
          <tbody className="divide-y">
            {pageData.content.map((transaction) => (
              <ReviewRow
                key={transaction.id}
                transaction={transaction}
                categories={categories}
              />
            ))}
          </tbody>
        </table>
      </div>
      {/* Pagination */}
      <div className="flex items-center justify-between">
        <span className="text-sm text-gray-500">
          Page {pageData.number + 1} of {pageData.totalPages} ·{" "}
          {pageData.totalElements} total
        </span>
        <div className="flex gap-2">
          <Button
            variant="ghost"
            onClick={() => setPage((p) => p - 1)}
            disabled={pageData.first}
          >
            Previous
          </Button>
          <Button
            variant="ghost"
            onClick={() => setPage((p) => p + 1)}
            disabled={pageData.last}
          >
            Next
          </Button>
        </div>
      </div>
    </div>
  );
};

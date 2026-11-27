import { useState } from "react";
import { Button } from "../../components/ui/Button";
import { ReviewRow } from "./ReviewRow";
import { useUncategorized } from "./api";
import { useCategories } from "../categories/api";
import { PersonSelect } from "../persons/PersonSelect";

export const ReviewPage = () => {
  const [page, setPage] = useState(0);
  const [personId, setPersonId] = useState<number | undefined>();
  const {
    data: pageData,
    isLoading,
    isError,
  } = useUncategorized(page, personId);
  const { data: categories } = useCategories();

  const handlePersonChange = (selectedPersonId: number | undefined) => {
    setPersonId(selectedPersonId);
    setPage(0); // a new filter always starts from the first page
  };

  // Loading / error / empty only replace the list, so the person filter stays visible
  const renderList = () => {
    if (isLoading || !categories) return <p>Se încarcă...</p>;
    if (isError || !pageData)
      return <p className="text-red-600">A apărut o eroare.</p>;
    if (pageData.page.totalElements === 0)
      return <p className="text-gray-500">Nimic de revizuit 🎉</p>;
    // must stay after the empty check: with 0 results totalPages is 0 → setPage(-1)
    if (pageData.content.length === 0 && pageData.page.number > 0) {
      setPage(pageData.page.totalPages - 1); // setState during render, guarded
      return null;
    }

    const isFirstPage = pageData.page.number === 0;
    const isLastPage = pageData.page.number + 1 >= pageData.page.totalPages;

    return (
      <>
        <ul className="flex flex-col gap-3">
          {pageData.content.map((transaction) => (
            <ReviewRow
              key={transaction.id}
              transaction={transaction}
              categories={categories}
            />
          ))}
        </ul>
        <div className="flex items-center justify-between">
          <span className="text-sm text-gray-500">
            Pagina {pageData.page.number + 1} din {pageData.page.totalPages} ·{" "}
            {pageData.page.totalElements} în total
          </span>
          <div className="flex gap-2">
            <Button
              variant="ghost"
              onClick={() => setPage((currentPage) => currentPage - 1)}
              disabled={isFirstPage}
            >
              Anterioară
            </Button>
            <Button
              variant="ghost"
              onClick={() => setPage((currentPage) => currentPage + 1)}
              disabled={isLastPage}
            >
              Următoare
            </Button>
          </div>
        </div>
      </>
    );
  };

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">De revizuit</h1>
        <PersonSelect value={personId} onChange={handlePersonChange} />
      </div>
      {renderList()}
    </div>
  );
};

import { useRef, useState } from "react";
import { useCategories } from "../categories/api";
import { usePersons } from "../persons/api";
import type { Transaction } from "../../types/domain";
import {
  useCreateTransaction,
  useDeleteTransaction,
  useTransactions,
  useUpdateTransaction,
  type TransactionInput,
} from "./api";
import { textColorFor, tintStyle } from "../../lib/contrast";
import { Button } from "../../components/ui/Button";
import { formatMoney } from "../../lib/money";
import { CategoryBadge } from "../../components/ui/CategoryBadge";

export const TransactionsPage = () => {
  const [page, setPage] = useState(0);
  const { data: pageData, isLoading, isError, error } = useTransactions(page);
  const { data: categories } = useCategories();
  const { data: persons } = usePersons();
  const createTransaction = useCreateTransaction();
  const updateTransaction = useUpdateTransaction();
  const deleteTransaction = useDeleteTransaction();

  const isFirstPage = (pageData?.page.number ?? 0) === 0;
  const isLastPage = pageData
    ? pageData.page.number + 1 >= pageData.page.totalPages
    : true;

  // form state
  const [date, setDate] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [personId, setPersonId] = useState<string>("");
  const [editId, setEditId] = useState<number | null>(null);
  const formRef = useRef<HTMLFormElement>(null);

  const resetForm = () => {
    setEditId(null);
    setDate("");
    setAmount("");
    setDescription("");
    setCategoryId("");
    setPersonId("");
  };

  const handleAdd = () => {
    if (!date || !amount || !description || !personId) return;

    if (editId === null) {
      createTransaction.mutate(
        {
          date,
          amount: Number(amount),
          currency: "RON",
          description,
          merchant: null,
          categoryId: categoryId ? Number(categoryId) : null,
          personId: Number(personId),
          source: "MANUAL",
        },
        {
          onSuccess: resetForm,
        },
      );
    } else {
      const input: TransactionInput = {
        date,
        amount: Number(amount),
        currency: "RON",
        description,
        merchant: null,
        categoryId: categoryId ? Number(categoryId) : null,
        personId: Number(personId),
        source: "MANUAL",
      };

      updateTransaction.mutate(
        {
          id: editId,
          input,
        },
        {
          onSuccess: resetForm,
        },
      );
    }
  };

  const startEdit = (transaction: Transaction) => {
    setEditId(transaction.id);
    setDate(transaction.date);
    setAmount(transaction.amount?.toString() ?? "");
    setDescription(transaction.description);
    setCategoryId(transaction.categoryId?.toString() ?? "");
    setPersonId(transaction.personId?.toString() ?? "");
    formRef.current?.scrollIntoView({ behavior: "smooth", block: "center" });
  };

  const isEditing = editId !== null;
  const formError = isEditing
    ? updateTransaction.error
    : createTransaction.error;
  const isSubmitting = isEditing
    ? updateTransaction.isPending
    : createTransaction.isPending;
  const getSubmitLabel = () => {
    if (isSubmitting) return "...";
    if (isEditing) return "Salvează";
    return "Adaugă";
  };
  const current = categories?.find((c) => String(c.id) === categoryId);

  if (isLoading) return <p className="mx-auto max-w-6xl p-6">Se încarcă...</p>;
  if (isError)
    return (
      <p className="mx-auto max-w-6xl p-6 text-red-600">
        Eroare: {error.message}
      </p>
    );

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Tranzacții</h1>

      {/* Add / edit form */}
      <form
        ref={formRef}
        onSubmit={(e) => {
          e.preventDefault();
          handleAdd();
        }}
        className={`rounded-lg border p-4 ${
          isEditing ? "border-blue-300 bg-blue-50" : "border-gray-200"
        }`}
      >
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold text-gray-700">
            {isEditing ? "Editezi tranzacția" : "Adaugă o tranzacție"}
          </h2>
          {isEditing && (
            <span className="rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-700">
              Mod editare
            </span>
          )}
        </div>

        <div className="flex flex-wrap items-end gap-3">
          <input
            type="date"
            className="rounded-lg border px-3 py-2 text-sm"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
          <input
            type="number"
            step="0.01"
            placeholder="Sumă"
            className="w-28 rounded-lg border px-3 py-2 text-sm"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
          <input
            placeholder="Descriere"
            className="min-w-40 flex-1 rounded-lg border px-3 py-2 text-sm"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <select
            className="rounded-lg border py-2 pl-3 pr-8 text-sm font-medium"
            style={tintStyle(current?.color)}
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
          >
            <option value="">Fără categorie</option>
            {categories?.map((category) => (
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
          <select
            className="rounded-lg border px-3 py-2 text-sm"
            value={personId}
            onChange={(e) => setPersonId(e.target.value)}
          >
            <option value="">Persoană...</option>
            {persons?.map((person) => (
              <option key={person.id} value={person.id}>
                {person.name}
              </option>
            ))}
          </select>
          <Button type="submit" disabled={isSubmitting}>
            {getSubmitLabel()}
          </Button>
          {isEditing && (
            <Button variant="ghost" onClick={resetForm}>
              Renunță
            </Button>
          )}
        </div>
      </form>

      {formError && (
        <p
          role="alert"
          className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700"
        >
          {formError.message}
        </p>
      )}

      {deleteTransaction.error && (
        <p
          role="alert"
          className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700"
        >
          {deleteTransaction.error.message}
        </p>
      )}

      {/* Table */}
      <div className="overflow-x-auto rounded-lg border">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
            <tr>
              <th className="px-3 py-2">Data</th>
              <th className="px-3 py-2">Descriere</th>
              <th className="px-3 py-2">Categorie</th>
              <th className="px-3 py-2">Persoană</th>
              <th className="px-3 py-2 text-right">Sumă</th>
              <th className="px-3 py-2" />
            </tr>
          </thead>
          <tbody className="divide-y">
            {pageData?.content.map((transaction: Transaction) => (
              <tr
                key={transaction.id}
                className={
                  editId === transaction.id ? "bg-blue-50" : "hover:bg-gray-50"
                }
              >
                <td className="whitespace-nowrap px-3 py-2 text-gray-500">
                  {transaction.date}
                </td>
                <td className="px-3 py-2">{transaction.description}</td>
                <td className="px-3 py-2">
                  {transaction.categoryName ? (
                    <CategoryBadge
                      name={transaction.categoryName}
                      color={transaction.categoryColor}
                    />
                  ) : (
                    <span className="text-xs text-gray-400">De revizuit</span>
                  )}
                </td>
                <td className="whitespace-nowrap px-3 py-2">
                  {transaction.personName}
                </td>
                <td
                  className={`whitespace-nowrap px-3 py-2 text-right font-mono font-medium ${
                    transaction.amount < 0 ? "text-red-600" : "text-green-700"
                  }`}
                >
                  {formatMoney(transaction.amount, transaction.currency)}
                </td>
                <td className="whitespace-nowrap px-3 py-2">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="ghost"
                      onClick={() => startEdit(transaction)}
                    >
                      Editează
                    </Button>
                    <Button
                      variant="ghost"
                      onClick={() => {
                        if (window.confirm("Ștergi această tranzacție?")) {
                          if (editId === transaction.id) resetForm();
                          deleteTransaction.mutate(transaction.id);
                        }
                      }}
                      disabled={deleteTransaction.isPending}
                    >
                      Șterge
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between">
        <span className="text-sm text-gray-500">
          Pagina {pageData ? pageData.page.number + 1 : 0} din{" "}
          {pageData?.page.totalPages ?? 0} · {pageData?.page.totalElements ?? 0}{" "}
          în total
        </span>
        <div className="flex gap-2">
          <Button
            variant="ghost"
            onClick={() => setPage((p) => p - 1)}
            disabled={isFirstPage ?? true}
          >
            Anterioară
          </Button>
          <Button
            variant="ghost"
            onClick={() => setPage((p) => p + 1)}
            disabled={isLastPage ?? true}
          >
            Următoare
          </Button>
        </div>
      </div>
    </div>
  );
};

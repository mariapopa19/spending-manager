import { useState } from "react";
import { useCategories } from "../categories/api";
import { usePersons } from "../persons/api";
import type { Transaction } from "../types/domain";
import {
  useCreateTransaction,
  useDeleteTransaction,
  useTransactions,
  useUpdateTransaction,
  type TransactionInput,
} from "./api";
import { textColorFor } from "../lib/contrast";

export const TransactionsPage = () => {
  const [page, setPage] = useState(0);
  const { data: pageData, isLoading, isError, error } = useTransactions(page);
  const { data: categories } = useCategories();
  const { data: persons } = usePersons();
  const createTransaction = useCreateTransaction();
  const updateTransaction = useUpdateTransaction();
  const deleteTransaction = useDeleteTransaction();

  // form state
  const [date, setDate] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [personId, setPersonId] = useState<string>("");
  const [editId, setEditId] = useState<number | null>(null);

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

  const isEditing = editId !== null;
  const isSubmitting = isEditing
    ? updateTransaction.isPending
    : createTransaction.isPending;
  const submitLabel = isSubmitting ? "..." : isEditing ? "Save" : "Add";

  if (isLoading) return <p className="p-4">Loading...</p>;
  if (isError)
    return <p className="p-4 text-red-600">Error: {error.message}</p>;

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">Transactions</h1>

      {/* Add form */}
      <div className="flex flex-wrap gap-2 mb-6 items-end">
        <input
          type="date"
          className="border rounded px-3 py-2"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
        <input
          type="number"
          step="0.01"
          placeholder="Amount"
          className="border rounded px-3 py-2 w-28"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
        <input
          placeholder="Description"
          className="border rounded px-3 py-2 flex-1 min-w-40"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <select
          className="border rounded px-3 py-2"
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
        >
          <option value="">No category</option>
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
          className="border rounded px-3 py-2"
          value={personId}
          onChange={(e) => setPersonId(e.target.value)}
        >
          <option value="">Person...</option>
          {persons?.map((person) => (
            <option key={person.id} value={person.id}>
              {person.name}
            </option>
          ))}
        </select>
        <button
          className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700 disabled:opacity-50"
          onClick={handleAdd}
          disabled={isSubmitting}
        >
          {submitLabel}
        </button>
        {editId !== null && (
          <button
            className="bg-gray-200 text-black rounded px-4 py-2 disabled:opacity-50"
            onClick={resetForm}
          >
            Cancel
          </button>
        )}
      </div>

      {/* Table */}
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b text-left text-sm text-gray-500">
            <th className="py-2">Date</th>
            <th className="py-2">Description</th>
            <th className="py-2">Category</th>
            <th className="py-2">Person</th>
            <th className="py-2 text-right">Amount</th>
            <th className="py-2 w-16"></th>
            <th className="py-2 w-16"></th>
          </tr>
        </thead>
        <tbody>
          {pageData?.content.map((transaction: Transaction) => (
            <tr key={transaction.id} className="boarder-b">
              <td className="py-2">{transaction.date}</td>
              <td className="py-2">{transaction.description}</td>
              <td className="py-2">
                {transaction.categoryName ? (
                  <span
                    className="inline-block rounded-full px-3 py-1 text-sm font-medium text-white"
                    style={{
                      backgroundColor: transaction.categoryColor ?? "#999",
                    }}
                  >
                    {transaction.categoryName}
                  </span>
                ) : (
                  <span className="text-gray-400 text-sm">Needs review</span>
                )}
              </td>
              <td className="py-2">{transaction.personName}</td>
              <td
                className={`py-2 text-right font-medium ${transaction.amount < 0 ? "text-red-600" : "text-green-600"}`}
              >
                {transaction.amount.toFixed(2)} {transaction.currency}
              </td>
              <td className="py-2 text-right">
                <button
                  className="text-red-600 hover:underline disabled:opacity-50"
                  onClick={() => deleteTransaction.mutate(transaction.id)}
                  disabled={deleteTransaction.isPending}
                >
                  Delete
                </button>
              </td>
              <td className="py-2 text-right">
                {" "}
                <button
                  className="text-blue-600 hover:underline disabled:opacity-50"
                  onClick={() => {
                    setEditId(transaction.id);
                    setDate(transaction.date);
                    setAmount(transaction.amount?.toString() ?? "");
                    setDescription(transaction.description);
                    setCategoryId(transaction.categoryId?.toString() ?? "");
                    setPersonId(transaction.personId?.toString());
                  }}
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Pagination */}
      <div className="flex items-center justify-between mt-4">
        <span className="text-sm text-gray-500">
          Page {pageData ? pageData.number + 1 : 0} of{" "}
          {pageData?.totalPages ?? 0} · {pageData?.totalElements ?? 0} total
        </span>
        <div className="flex gap-2">
          <button
            className="border rounded px-3 py-1 disabled:opacity-50"
            onClick={() => setPage((p) => p - 1)}
            disabled={pageData?.first ?? true}
          >
            Previous
          </button>
          <button
            className="border rounded px-3 py-1 disabled:opacity-50"
            onClick={() => setPage((p) => p + 1)}
            disabled={pageData?.last ?? true}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

import { useState } from "react";
import { useCategories } from "../categories/api";
import { usePersons } from "../persons/api";
import {
  useCreateTransaction,
  useDeleteTransaction,
  useTransactions,
} from "./api";
import type { Category, Person, Transaction } from "../types/domain";

export const TransactionsPage = () => {
  const [page, setPage] = useState(0);
  const { data: pageData, isLoading, isError, error } = useTransactions(page);
  const { data: categories } = useCategories();
  const { data: persons } = usePersons();
  const createTransaction = useCreateTransaction();
  const deleteTransaction = useDeleteTransaction();

  // form state
  const [date, setDate] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [personId, setPersonId] = useState<string>("");

  const handleAdd = () => {
    if (!date || !amount || !description || !personId) return;

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
        onSuccess: () => {
          setDate("");
          setAmount("");
          setDescription("");
          setCategoryId("");
          setPersonId("");
        },
      },
    );
  };

  if (isLoading) return <p className="p-4">Loading...</p>;
  if (isError)
    return <p className="p-4 text-red-600">Error: {error.message}</p>;

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">Transactions</h1>

      {/* Add form */}
      {/* Table */}
      {/* Pagination */}
    </div>
  );
};

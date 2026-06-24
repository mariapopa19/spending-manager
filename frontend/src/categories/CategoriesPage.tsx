import { useState } from "react";
import {
  useCategories,
  useCreateCategory,
  useDeleteCategory,
  useUpdateCategory,
  type CategoryInput,
} from "./api";

export const CategoriesPage = () => {
  const { data: categories, isLoading, isError, error } = useCategories(); // data: categories - rename data variable
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory();
  const deleteCategory = useDeleteCategory();

  const [name, setName] = useState("");
  const [color, setColor] = useState("#64B5F6");
  const [budget, setBudget] = useState("");
  const [editId, setEditId] = useState<number | null>(null);

  const resetForm = () => {
    setEditId(null);
    setName("");
    setColor("#64B5F6");
    setBudget("");
  };

  const handleAdd = () => {
    if (name.trim() === "") return;
    if (editId === null) {
      createCategory.mutate(
        {
          name: name.trim(),
          color,
          monthlyBudget: budget ? Number(budget) : null,
        },
        {
          onSuccess: resetForm,
        },
      );
    } else {
      const input: CategoryInput = {
        name: name.trim(),
        color,
        monthlyBudget: budget ? Number(budget) : null,
      };
      updateCategory.mutate(
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
    ? updateCategory.isPending
    : createCategory.isPending;
  const submitLabel = isSubmitting ? "..." : isEditing ? "Save" : "Add";

  if (isLoading) return <p className="p-4">Loading...</p>;
  if (isError)
    return <p className="p-4 text-red-600">Error: {error.message}</p>;

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">Categories</h1>

      {/* Add form */}
      <div className="flex gap-2 mb-6">
        <input
          className="border rounded px-3 py-2 flex-1"
          placeholder="Category name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          type="color"
          className="border rounded  w-12 h-10"
          value={color}
          onChange={(e) => setColor(e.target.value)}
        />
        <input
          className="border rounded px-3 py-2 w-32"
          placeholder="Budget"
          type="number"
          value={budget}
          onChange={(e) => setBudget(e.target.value)}
        />
        <button
          className="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50"
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
          <tr className="border-b text-left">
            <th className="py-2">Category</th>
            <th className="py-2 text-right">Monthly budget</th>
            <th className="py-2 w-20"></th>
            <th className="py-2 w-20"></th>
          </tr>
        </thead>
        <tbody>
          {categories?.map((category) => (
            <tr key={category.id} className="border-b">
              <td className="py-2">
                <span className="inline-flex items-center gap-2">
                  <span
                    className="w-3 h-3 rounded-full inline-block"
                    style={{ backgroundColor: category.color ?? "#ccc" }}
                  />
                  {category.name}
                </span>
              </td>
              <td className="py-2 text-right">
                {category.monthlyBudget != null
                  ? `${category.monthlyBudget.toFixed(2)} RON`
                  : "-"}
              </td>
              <td className="py-2 text-right">
                <button
                  className="text-red-600 hover:underline disabled:opacity-50"
                  onClick={() => deleteCategory.mutate(category.id)}
                  disabled={deleteCategory.isPending}
                >
                  Delete
                </button>
              </td>
              <td className="py-2 text-right">
                {" "}
                <button
                  className="text-blue-600 hover:underline disabled:opacity-50"
                  onClick={() => {
                    setEditId(category.id);
                    setName(category.name);
                    setColor(category.color ?? "#64B5F6");
                    setBudget(category.monthlyBudget?.toString() ?? "");
                  }}
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

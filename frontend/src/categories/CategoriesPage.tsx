import { useState } from "react";
import { useCategories, useCreateCategory, useDeleteCategory } from "./api";

export const CategoriesPage = () => {
  const { data: categories, isLoading, isError, error } = useCategories(); // data: categories - rename data variable
  const createCategory = useCreateCategory();
  const deleteCategory = useDeleteCategory();

  const [name, setName] = useState("");
  const [color, setColor] = useState("#64B5F6");
  const [budget, setBudget] = useState("");

  const handleAdd = () => {
    if (name.trim() === "") return;
    createCategory.mutate(
      {
        name: name.trim(),
        color,
        monthlyBudget: budget ? Number(budget) : null,
      },
      {
        onSuccess: () => {
          setName("");
          setBudget("");
        },
      },
    );
  };

  if (isLoading) return <p className="p-4">Loading...</p>;
  if (isError)
    return <p className="p-4 text-red-600">Error: {error.message}</p>;

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <h1 className="text-2x1 font-bold mb-4">Categories</h1>

      {/* add form */}
      <div className="flex gap-2 mb-6">
        <input
          className="border rounded px-3 py-2 flex-1"
          placeholder="Category name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          type="color"
          className="border rouded w-12 h-10"
          value={color}
          onChange={(e) => setColor(e.target.value)}
        />
        <input
          className="border rounded px-3 py-2 w-32"
          placeholder="Budget"
          type="number"
          value={budget}
          onChange={(e) => setName(e.target.value)}
        />
        <button
          className="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50"
          onClick={handleAdd}
          disabled={createCategory.isPending}
        >
          {createCategory.isPending ? "..." : "Add"}
        </button>
      </div>

      {/* table */}
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b text-left">
            <th className="py-2">Category</th>
            <th className="py-2 text-right">Monthly budget</th>
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
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

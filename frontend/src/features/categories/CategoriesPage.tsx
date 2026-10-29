import { useState } from "react";
import {
  useCategories,
  useCreateCategory,
  useDeleteCategory,
  useUpdateCategory,
  type CategoryInput,
} from "./api";
import { ColorPicker } from "../../components/ui/ColorPicker";
import { Button } from "../../components/ui/Button";

export const CategoriesPage = () => {
  const { data: categories, isLoading, isError, error } = useCategories(); // data: categories - rename data variable
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory();
  const deleteCategory = useDeleteCategory();

  const [name, setName] = useState("");
  const [color, setColor] = useState("#64B5F6");
  const [budget, setBudget] = useState("");
  const [editId, setEditId] = useState<number | null>(null);
  const [excludedFromBudget, setExcludedFromBudget] = useState(false);

  const resetForm = () => {
    setEditId(null);
    setName("");
    setColor("#64B5F6");
    setBudget("");
    setExcludedFromBudget(false);
  };

  const handleAdd = () => {
    if (name.trim() === "") return;
    const input: CategoryInput = {
      name: name.trim(),
      color,
      monthlyBudget: budget ? Number(budget) : null,
      excludedFromBudget,
    };
    if (editId === null) {
      createCategory.mutate(input, { onSuccess: resetForm });
    } else {
      updateCategory.mutate({ id: editId, input }, { onSuccess: resetForm });
    }
  };

  const isEditing = editId !== null;
  const isSubmitting = isEditing
    ? updateCategory.isPending
    : createCategory.isPending;
  const getSubmitLabel = () => {
    if (isSubmitting) return "...";
    if (isEditing) return "Salvează";
    return "Adaugă";
  };

  if (isLoading) return <p className="mx-auto max-w-6xl p-6">Se încarcă...</p>;
  if (isError)
    return (
      <p className="mx-auto max-w-6xl p-6 text-red-600">
        Eroare: {error.message}
      </p>
    );

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Categorii</h1>

      {/* Add form */}
      <div className="flex flex-wrap items-center gap-2 rounded-lg border border-gray-200 p-4">
        <input
          className="min-w-40 flex-1 rounded-lg border px-3 py-2 text-sm"
          placeholder="Nume categorie"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <ColorPicker value={color} onChange={setColor} />
        <input
          className="w-32 rounded-lg border px-3 py-2 text-sm"
          placeholder="Buget"
          type="number"
          value={budget}
          onChange={(e) => setBudget(e.target.value)}
        />
        <label className="flex items-center gap-2 whitespace-nowrap text-sm">
          <input
            type="checkbox"
            checked={excludedFromBudget}
            onChange={(event) => setExcludedFromBudget(event.target.checked)}
          />
          <span>Exclus din buget</span>
        </label>
        <Button onClick={handleAdd} disabled={isSubmitting}>
          {getSubmitLabel()}
        </Button>
        {editId !== null && (
          <Button variant="ghost" onClick={resetForm}>
            Renunță
          </Button>
        )}
      </div>

      {/* Table */}
      <div className="overflow-x-auto rounded-lg border">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
            <tr>
              <th className="px-3 py-2">Categorie</th>
              <th className="px-3 py-2 text-right">Buget lunar</th>
              <th className="w-20 px-3 py-2"></th>
              <th className="w-20 px-3 py-2"></th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {categories?.map((category) => (
              <tr key={category.id} className="hover:bg-gray-50">
                <td className="px-3 py-2">
                  <span className="inline-flex items-center gap-2">
                    <span
                      className="w-3 h-3 rounded-full inline-block"
                      style={{ backgroundColor: category.color ?? "#ccc" }}
                    />
                    {category.name}
                    {category.excludedFromBudget && (
                      <span className="text-xs text-gray-500">
                        exclus din buget
                      </span>
                    )}
                  </span>
                </td>
                <td className="px-3 py-2 text-right font-mono">
                  {category.monthlyBudget != null
                    ? `${category.monthlyBudget.toFixed(2)} RON`
                    : "-"}
                </td>
                <td className="px-3 py-2 text-right">
                  {!category.excludedFromBudget && (
                    <button
                      className="text-red-600 hover:underline disabled:opacity-50"
                      onClick={() => deleteCategory.mutate(category.id)}
                      disabled={deleteCategory.isPending}
                    >
                      Șterge
                    </button>
                  )}
                </td>
                <td className="px-3 py-2 text-right">
                  {" "}
                  <button
                    className="text-blue-600 hover:underline disabled:opacity-50"
                    onClick={() => {
                      setEditId(category.id);
                      setName(category.name);
                      setColor(category.color ?? "#64B5F6");
                      setBudget(category.monthlyBudget?.toString() ?? "");
                      setExcludedFromBudget(category.excludedFromBudget);
                    }}
                  >
                    Editează
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

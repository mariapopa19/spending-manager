import { useState } from "react";
import { Button } from "../../components/ui/Button";
import {
  useApplyRules,
  useCreateRule,
  useDeleteRule,
  useRules,
  useUpdateRule,
  type RuleInput,
} from "./api";
import { Field } from "../../components/ui/Field";
import { useCategories } from "../categories/api";
import { textColorFor, tintStyle } from "../../lib/contrast";
import { CategoryBadge } from "../../components/ui/CategoryBadge";

export const RulesPage = () => {
  const { data: rules, isLoading, isError, error } = useRules();
  const createRule = useCreateRule();
  const updateRule = useUpdateRule();
  const deleteRule = useDeleteRule();
  const apply = useApplyRules();
  const { data: categories } = useCategories();

  const [pattern, setPattern] = useState<string>("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [editId, setEditId] = useState<number | null>(null);

  const resetForm = () => {
    setEditId(null);
    setPattern("");
    setCategoryId("");
    createRule.reset();
    updateRule.reset();
  };

  const canSubmit = pattern.trim().length >= 3 && categoryId !== "";

  const handleAdd = () => {
    if (!canSubmit) return;
    if (editId === null) {
      createRule.mutate(
        {
          pattern: pattern.trim(),
          categoryId: Number(categoryId),
        },
        {
          onSuccess: resetForm,
        },
      );
    } else {
      const input: RuleInput = {
        pattern: pattern.trim(),
        categoryId: Number(categoryId),
      };
      updateRule.mutate(
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
  const isSubmitting = isEditing ? updateRule.isPending : createRule.isPending;
  const getSubmitLabel = () => {
    if (isSubmitting) return "...";
    if (isEditing) return "Save";
    return "Add";
  };
  const formError = isEditing ? updateRule.error : createRule.error;
  const categoryById = new Map(categories?.map((c) => [c.id, c]));
  const current = categoryById.get(Number(categoryId));

  if (isLoading) return <p className="p-4">Loading...</p>;
  if (isError)
    return <p className="p-4 text-red-600">Error: {error.message}</p>;

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">Rules</h1>
          <p className="text-sm text-gray-500">
            Transactions whose description contains a pattern get its category
            on import. Re-apply to categorize existing uncategorized ones.
          </p>
        </div>
        <div className="flex items-center gap-3">
          {apply.isSuccess && (
            <span className="text-sm text-green-700">
              {apply.data.updated} transactions categorized
            </span>
          )}
          <Button
            variant="ghost"
            onClick={() => apply.mutate()}
            disabled={apply.isPending}
          >
            Re-apply rules
          </Button>
        </div>
      </div>

      {/* Add/edit form */}
      <h2 className="text-sm font-semibold text-gray-700">
        {isEditing ? "Edit rule" : "Add a rule"}
      </h2>
      <form
        className={`flex flex-wrap items-end gap-3 rounded-lg border p-4 ${isEditing ? "bg-blue-50" : ""}`}
        onSubmit={(e) => {
          e.preventDefault();
          handleAdd();
        }}
      >
        <Field label="Pattern">
          <input
            className="w-64 rounded border px-3 py-2"
            placeholder="e.g. Netflix (min. 3 characters)"
            value={pattern}
            onChange={(e) => setPattern(e.target.value)}
          />
        </Field>
        <Field label="Category">
          <select
            className="w-48 rounded-full border py-2 pl-4 pr-8 text-sm font-medium"
            style={tintStyle(current?.color)}
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
          >
            <option value="">Choose...</option>
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
        </Field>
        <Button type="submit" disabled={isSubmitting || !canSubmit}>
          {getSubmitLabel()}
        </Button>
        {isEditing && (
          <Button variant="ghost" onClick={resetForm}>
            Cancel
          </Button>
        )}
      </form>

      {/* 409 error */}
      {formError && (
        <p
          role="alert"
          className="rounded border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700"
        >
          {formError.message}
        </p>
      )}

      {deleteRule.error && (
        <p
          role="alert"
          className="rounded border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700"
        >
          {deleteRule.error.message}
        </p>
      )}

      {/* List */}
      <div className="overflow-x-auto rounded-lg border">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
            <tr>
              <th className="w-1/2 px-3 py-2">Pattern</th>
              <th className="px-3 py-2">Category</th>
              <th className="px-3 py-2" />
            </tr>
          </thead>
          <tbody className="divide-y">
            {rules?.map((rule) => (
              <tr key={rule.id} className="hover:bg-gray-50">
                <td className="px-3 py-2 font-mono">{rule.pattern}</td>
                <td className="px-3 py-2">
                  <CategoryBadge
                    name={rule.categoryName}
                    color={categoryById.get(rule.categoryId)?.color}
                  />
                </td>
                <td className="px-3 py-2">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="ghost"
                      onClick={() => {
                        createRule.reset();
                        updateRule.reset();
                        setEditId(rule.id);
                        setPattern(rule.pattern);
                        setCategoryId(String(rule.categoryId));
                      }}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="danger"
                      onClick={() => {
                        if (window.confirm(`Delete rule "${rule.pattern}"?`)) {
                          if (editId === rule.id) resetForm();
                          deleteRule.mutate(rule.id);
                        }
                      }}
                      disabled={deleteRule.isPending}
                    >
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {rules?.length === 0 && (
          <p className="p-4 text-center text-sm text-gray-500">No rules yet.</p>
        )}
      </div>
    </div>
  );
};

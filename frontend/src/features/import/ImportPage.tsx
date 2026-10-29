import { useState } from "react";
import type { Source } from "../../types/domain";
import type { ImportPreviewRow, Wizard } from "./types";
import { usePersons } from "../persons/api";
import { useConfirmImport, usePreviewImport } from "./api";
import { StepRail } from "./StepRail";
import { UploadStep } from "./UploadStep";
import { PreviewTable } from "./PreviewTable";
import { DoneStep } from "./DoneStep";
import { useCategories } from "../categories/api";

export const ImportPage = () => {
  const [source, setSource] = useState<Source | "">("");
  const [personId, setPersonId] = useState("");
  const [file, setFile] = useState<File | null>(null);

  const [wizard, setWizard] = useState<Wizard>({ step: "upload" });
  const [selected, setSelected] = useState<Set<number>>(new Set());

  const [overrides, setOverrides] = useState<Map<number, number | null>>(
    new Map(),
  );

  const { data: persons } = usePersons();
  const preview = usePreviewImport();
  const confirm = useConfirmImport();

  const { data: categories } = useCategories();

  const handlePreview = () => {
    if (source === "" || personId === "" || file === null) return;

    preview.mutate(
      { source, file },
      {
        onSuccess: (data) => {
          setWizard({ step: "review", preview: data });
          setSelected(
            new Set(
              data.rows
                .map((row, index) => ({ row, index }))
                .filter(({ row }) => row.status === "NEW")
                .map(({ index }) => index),
            ),
          );
          setOverrides(new Map());
        },
      },
    );
  };

  const toggle = (key: number) =>
    setSelected((current) => {
      const next = new Set(current);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });

  const handleSource = (s: Source | "") => {
    setSource(s);
    setFile(null);
  };

  const handleConfirm = () => {
    if (wizard.step !== "review") return;
    if (source === "" || personId === "") return;

    const rows = wizard.preview.rows
      .map((row, index) => ({ row, index }))
      .filter(({ index }) => selected.has(index))
      .map(({ row, index }) => ({
        date: row.date,
        amount: row.amount,
        currency: row.currency,
        description: row.description,
        categoryId: categoryIdFor(index, row),
      }));

    confirm.mutate(
      { source, personId: Number(personId), rows },
      { onSuccess: (result) => setWizard({ step: "done", result }) },
    );
  };

  const reset = () => {
    setWizard({ step: "upload" });
    setSelected(new Set());
    setOverrides(new Map());
    setFile(null);
    preview.reset();
    confirm.reset();
  };

  const categoryIdFor = (
    index: number,
    row: ImportPreviewRow,
  ): number | null => {
    if (overrides.has(index)) {
      return overrides.get(index)!;
    }
    return row.categoryId;
  };

  const handleCategoryChange = (index: number, categoryId: number | null) => {
    setOverrides((current) => {
      const next = new Map(current);
      next.set(index, categoryId);
      return next;
    });
  };

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Importă un extras de cont</h1>
      <StepRail step={wizard.step} />
      {wizard.step === "upload" && (
        <UploadStep
          source={source}
          onSource={handleSource}
          personId={personId}
          onPersonId={setPersonId}
          persons={persons ?? []}
          file={file}
          onFile={setFile}
          onPreview={handlePreview}
          isPending={preview.isPending}
          error={preview.error}
        />
      )}
      {wizard.step === "review" && (
        <PreviewTable
          preview={wizard.preview}
          selected={selected}
          categories={categories ?? []}
          categoryIdFor={categoryIdFor}
          onCategoryChange={handleCategoryChange}
          onToggle={toggle}
          onConfirm={handleConfirm}
          onBack={reset}
          isPending={confirm.isPending}
          error={confirm.error}
        />
      )}
      {wizard.step === "done" && (
        <DoneStep result={wizard.result} onAgain={reset} />
      )}
    </div>
  );
};

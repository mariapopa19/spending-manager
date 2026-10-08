import { useState } from "react";
import type { Source } from "../../types/domain";
import type { ImportPreviewResponse, ImportResultResponse, Wizard } from "./types";
import { usePersons } from "../persons/api";
import { useConfirmImport, usePreviewImport } from "./api";

export const ImportPage = () => {
  const [source, setSource] = useState<Source | "">("");
  const [personId, setPersonId] = useState("");
  const [file, setFile] = useState<File | null>(null);

  const [wizard, setWizard] = useState<Wizard>({ step: "upload" });
  const [selected, setSelected] = useState<Set<number>>(new Set());

  const { data: persons } = usePersons();
  const preview = usePreviewImport();
  const confirm = useConfirmImport();

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

  const handleConfirm = () => {
    if (wizard.step !== "review") return;
    if (source === "" || personId === "") return;

    const rows = wizard.preview.rows
      .map((row, index) => ({ row, index }))
      .filter(({ index }) => selected.has(index))
      .map(({ row }) => ({
        date: row.date,
        amount: row.amount,
        currency: row.currency,
        description: row.description,
      }));

    confirm.mutate(
      { source, personId: Number(personId), rows },
      { onSuccess: (result) => setWizard({ step: "done", result }) },
    );
  };

  const reset = () => {
    setWizard({ step: "upload" });
    setSelected(new Set());
    setFile(null);
    preview.reset();
    confirm.reset();
  };

  return (
  <div className="mx-auto max-w-5xl p-6">
    <h1 className="mb-4 text-2xl font-bold">Import a statement</h1>
    <StepRail
  </div>);
};

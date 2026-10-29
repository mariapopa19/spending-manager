import { Button } from "../../components/ui/Button";
import { Field } from "../../components/ui/Field";
import type { Person, Source } from "../../types/domain";
import { FileDropzone } from "./FileDropzone";
import { IMPORT_SOURCES } from "./types";

type Props = {
  source: Source | "";
  onSource: (s: Source | "") => void;
  personId: string;
  onPersonId: (id: string) => void;
  persons: Person[];
  file: File | null;
  onFile: (f: File) => void;
  onPreview: () => void;
  isPending: boolean;
  error: Error | null;
};

export const UploadStep = ({
  source,
  onSource,
  personId,
  onPersonId,
  persons,
  file,
  onFile,
  onPreview,
  isPending,
  error,
}: Props) => {
  const accept = IMPORT_SOURCES.find((s) => s.value === source)?.accept ?? "";
  const canPreview = source !== "" && personId !== "" && file !== null;
  return (
    <div className="flex flex-col gap-4 rounded-lg border border-gray-200 p-4">
      <Field label="Bancă">
        <select
          className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
          value={source}
          onChange={(e) => onSource(e.target.value as Source | "")}
        >
          <option value="">Alege banca…</option>
          {IMPORT_SOURCES.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </Field>
      <Field label="Persoană">
        <select
          className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
          value={personId}
          onChange={(e) => onPersonId(e.target.value)}
        >
          <option value="">Alege persoana…</option>
          {persons.map((person) => (
            <option key={person.id} value={person.id}>
              {person.name}
            </option>
          ))}
        </select>
      </Field>
      <div className={source === "" ? "pointer-events-none opacity-50" : ""}>
        <FileDropzone accept={accept} file={file} onFile={onFile} />
      </div>
      {error && (
        <p
          role="alert"
          className="rounded border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700"
        >
          {error.message}
        </p>
      )}
      <div>
        <Button onClick={onPreview} disabled={!canPreview || isPending}>
          {isPending ? "Se citește…" : "Previzualizează"}
        </Button>
      </div>
    </div>
  );
};

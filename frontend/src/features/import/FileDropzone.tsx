import { useRef, useState } from "react";

type Props = {
  accept: string;
  file: File | null;
  onFile: (file: File) => void;
};

export const FileDropzone = ({ accept, file, onFile }: Props) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);

  return (
    <>
      <input
        hidden
        ref={inputRef}
        type="file"
        accept={accept}
        onChange={(e) => {
          const picked = e.target.files?.[0];
          if (picked) onFile(picked);
          e.target.value = "";
        }}
      />
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => {
          e.preventDefault();
          setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault();
          setDragging(false);
          const dropped = e.dataTransfer.files[0];
          if (dropped) onFile(dropped);
        }}
        className={`flex cursor-pointer flex-col items-center gap-2 rounded-lg border-2 border-dashed p-8 text-center w-full focus-visible:outline-none focus-visible:border-blue-500 ${dragging ? "border-blue-500 bg-blue-50" : "border-gray-300 bg-gray-50"}`}
      >
        <span className="font-semibold">
          Drop the file here, or click to browse
        </span>
        {file && (
          <span className="rounded border border-blue-600 bg-blue-50 px-2 py-0.5 font-mono text-blue-700 text-xs">
            {file.name} · {Math.round(file.size / 1024)} KB
          </span>
        )}
      </button>
    </>
  );
};

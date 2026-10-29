import type { WizardStep } from "./types";

const STEPS: { key: WizardStep; label: string }[] = [
  { key: "upload", label: "1 · Încărcare" },
  { key: "review", label: "2 · Verificare" },
  { key: "done", label: "3 · Gata" },
];

export const StepRail = ({ step }: { step: WizardStep }) => (
  <ol className="flex gap-2 text-sm">
    {STEPS.map(({ key, label }) => (
      <li
        key={key}
        className={`rounded-lg border px-3 py-1 ${key === step ? "border-blue-600 bg-blue-600 font-semibold text-white" : "border-gray-300 text-gray-500"}`}
      >
        {label}
      </li>
    ))}
  </ol>
);

import type { WizardStep } from "./types";

const STEPS: { key: WizardStep; label: string }[] = [
  { key: "upload", label: "1 · Upload" },
  { key: "review", label: "2 · Review" },
  { key: "done", label: "3 · Done" },
];

export const StepRail = ({ step }: { step: WizardStep }) => (
  <ol className="mb-6 flex gap-2 text-sm">
    {STEPS.map(({ key, label }) => (
      <li
        key={key}
        className={`rounded-full border px-3 py-1 ${key === step ? "border-blue-600 bg-blue-600 font-semibold text-white" : "border-gray-300 text-gray-500"}`}
      >
        {label}
      </li>
    ))}
  </ol>
);

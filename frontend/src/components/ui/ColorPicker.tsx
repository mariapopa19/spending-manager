import { useState } from "react";

const PRESET_COLORS = [
  // 300 – light
  "#E57373",
  "#F06292",
  "#BA68C8",
  "#7986CB",
  "#64B5F6",
  "#4FC3F7",
  "#4DB6AC",
  "#81C784",
  "#AED581",
  "#FFD54F",
  "#FFB74D",
  "#FF8A65",
  "#A1887F",
  "#90A4AE",
  // 500 – medium
  "#F44336",
  "#E91E63",
  "#9C27B0",
  "#3F51B5",
  "#2196F3",
  "#03A9F4",
  "#009688",
  "#4CAF50",
  "#8BC34A",
  "#FFC107",
  "#FF9800",
  "#FF5722",
  "#795548",
  "#607D8B",
  // 700 – dark
  "#D32F2F",
  "#C2185B",
  "#7B1FA2",
  "#303F9F",
  "#1976D2",
  "#0288D1",
  "#00796B",
  "#388E3C",
  "#689F38",
  "#FFA000",
  "#F57C00",
  "#E64A19",
  "#5D4037",
  "#455A64",
];

type Props = { value: string; onChange: (color: string) => void };

export const ColorPicker = ({ value, onChange }: Props) => {
  const [isOpen, setIsOpen] = useState(false);

  const choose = (color: string) => {
    onChange(color);
    setIsOpen(false);
  };

  return (
    <div className="relative shrink-0">
      <button
        type="button"
        aria-label="Choose color"
        onClick={() => setIsOpen((open) => !open)}
        className="h-10 w-10 rounded-full border-2 border-white shadow ring-1 ring-gray-300"
        style={{ backgroundColor: value }}
      />

      {isOpen && (
        <>
          {/* invisible full-screen layer: clicking outside the panel closes it */}
          <button
            type="button"
            aria-label="Close color picker"
            className="fixed inset-0 z-10"
            onClick={() => setIsOpen(false)}
          />

          <div className="absolute left-0 top-12 z-20 grid w-48 grid-cols-5 gap-2 rounded-lg border border-gray-200 bg-white p-3 shadow-lg">
            {PRESET_COLORS.map((presetColor) => (
              <button
                key={presetColor}
                type="button"
                aria-label={presetColor}
                onClick={() => choose(presetColor)}
                className={`h-7 w-7 rounded-full ${
                  presetColor.toLowerCase() === value.toLowerCase()
                    ? "ring-2 ring-gray-900 ring-offset-2"
                    : ""
                }`}
                style={{ backgroundColor: presetColor }}
              />
            ))}
            {/* custom color: the native picker, styled as a dashed circle */}
            <label
              className="flex h-7 w-7 cursor-pointer items-center justify-center rounded-full border-2 border-dashed border-gray-300 text-gray-400"
              title="Custom color"
            >
              +
              <input
                type="color"
                value={value}
                onChange={(event) => onChange(event.target.value)}
                className="sr-only"
              />
            </label>
          </div>
        </>
      )}
    </div>
  );
};

import { currentMonth, formatMonth, shiftMonth } from "../../lib/month";

type Props = { month: string; onChange: (month: string) => void };

export const MonthPicker = ({ month, onChange }: Props) => {
  const isCurrentMonth = month === currentMonth();

  return (
    <div className="flex items-center rounded-lg border border-gray-200">
      <button
        type="button"
        aria-label="Luna anterioară"
        onClick={() => onChange(shiftMonth(month, -1))}
        className="px-3 py-1 text-gray-500 hover:text-gray-900"
      >
        ‹
      </button>
      <span className="w-36 text-center font-medium capitalize">
        {formatMonth(month)}
      </span>
      <button
        type="button"
        aria-label="Luna următoare"
        onClick={() => onChange(shiftMonth(month, 1))}
        disabled={isCurrentMonth}
        className="px-3 py-1 text-gray-500 hover:text-gray-900 disabled:opacity-30"
      >
        ›
      </button>
    </div>
  );
};

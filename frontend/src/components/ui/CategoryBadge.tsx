import { textColorFor } from "../../lib/contrast";

type Props = {
  name: string;
  color?: string | null;
};

export const CategoryBadge = ({ name, color }: Props) => {
  const bg = color ?? "#9ca3af";
  return (
    <span
      className="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
      style={{ backgroundColor: bg, color: textColorFor(bg) }}
    >
      {name}
    </span>
  );
};

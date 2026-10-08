import type { ReactNode } from "react";

type Props = {
    label: string;
    children: ReactNode;
    hint?: string;
};

export const Field = ({label, children,  hint = ""}: Props) => (
    <label className="flex flex-col gap-1">
        <span className="text-sm font-semibold text-gray-700">{label}</span>
        {children}
        {hint && (<span className="text-xs text-gray-500">{hint}</span>)}
    </label>
);
import type { ButtonHTMLAttributes } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "ghost" | "link";
};

const variants = {
  primary: "border-blue-600 bg-blue-600 text-white hover:bg-blue-700",
  ghost: "border-gray-300 bg-white text-gray-700 hover:bg-gray-50",
  link: "border-transparent text-blue-600 underline hover:text-blue-700",
} as const;

export const Button = ({
  variant = "primary",
  className = "",
  type = "button",
  ...rest
}: Props) => (
  <button
    type={type}
    className={`rounded border px-4 py-2 text-sm font-semibold disabled:opacity-50 ${variants[variant]} ${className}`}
    {...rest}
  />
);

export const textColorFor = (hex: string): string => {
  const c = hex.replace("#", "");
  const r = Number.parseInt(c.substring(0, 2), 16);
  const g = Number.parseInt(c.substring(2, 4), 16);
  const b = Number.parseInt(c.substring(4, 6), 16);

  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  return luminance > 0.65 ? "#000000" : "#ffffff";
};

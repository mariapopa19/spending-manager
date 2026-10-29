export const toMonthString = (date: Date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
export const currentMonth = () => toMonthString(new Date());

export const shiftMonth = (month: string, offset: number) => {
  const [year, monthNumber] = month.split("-").map(Number);
  return toMonthString(new Date(year, monthNumber - 1 + offset, 1));
};

export const formatMonth = (month: string) => {
  const [year, monthNumber] = month.split("-").map(Number);
  return new Date(year, monthNumber - 1, 1).toLocaleDateString("ro-RO", {
    month: "long",
    year: "numeric",
  });
};

export const monthRange = (fromMonth: string, toMonth: string) => {
  const months: string[] = [];
  for (let month = fromMonth; month <= toMonth; month = shiftMonth(month, 1)) {
    months.push(month);
  }
  return months;
};

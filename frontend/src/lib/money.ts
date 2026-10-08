export const formatMoney = (amount: number, currency: string) =>
  new Intl.NumberFormat("ro-RO", { style: "currency", currency }).format(
    amount,
  );

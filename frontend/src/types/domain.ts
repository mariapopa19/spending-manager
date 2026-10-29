export type Person = {
  id: number;
  name: string;
};

export type Category = {
  id: number;
  name: string;
  color: string | null;
  monthlyBudget: number | null;
  excludedFromBudget: boolean;
};

export type Transaction = {
  id: number;
  date: string;
  amount: number;
  currency: string;
  description: string;
  merchant: string | null;
  categoryId: number | null;
  categoryName: string | null;
  categoryColor: string | null;
  personId: number;
  personName: string;
  source: Source;
  importHash: string | null;
  createdAt: string;
};

export type CategoryRule = {
  id: number;
  pattern: string;
  categoryId: number;
  categoryName: string;
};

export type CategoryTotal = {
  categoryId: number | null;
  name: string;
  color: string | null;
  estimated: number | null;
  actual: number;
};

export type MerchantTotal = {
  merchant: string;
  total: number;
  count: number;
};
export type MonthlyTotal = {
  month: string;
  expenses: number;
  income: number;
};

export type Overview = {
  month: string;
  currency: string;
  income: number;
  expenses: number;
  estimated: number;
  remaining: number;
  uncategorizedCount: number;
};

export type StatsFilters = {
  month: string;
  personId?: number;
  source?: Source;
};

export type Page<T> = {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

export type Source = "REVOLUT" | "BT_PAY" | "BCR_GEORGE" | "MANUAL";

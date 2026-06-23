export type Person = {
    id: number;
    name: string;
};

export type Category = {
    id: number;
    name: string;
    color: string;
    monthlyBudget: number;
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
    crearedAt: string;
}

export type Page<T> = {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}


export type Source = "REVOLUT" | "BT_PAY" | "BCR_GEORGE" | "MANUAL";
import { QueryClient } from "@tanstack/react-query";

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 30,        // 30s - data are still valid and fresh
            retry: 1,                    // one retry to failded fetch
            refetchOnWindowFocus: false, // do not refetch on every click on the screen (not rerender)
        }
    }
})
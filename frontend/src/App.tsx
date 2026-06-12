import { useQuery } from "@tanstack/react-query";

function App() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["health"],
    queryFn: () =>
      fetch("/actuator/health").then((res) => {
        if (!res.ok) throw new Error("Backend unreachable");
        return res.json() as Promise<{ status: string }>;
      }),
  });
  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold text-emerald-600">Spending Manager</h1>
      <p className="mt-4">
        Backend:{" "}
        {isLoading ? "checking…" : isError ? "down ❌" : `${data?.status} ✅`}
      </p>
    </div>
  );
}

export default App;

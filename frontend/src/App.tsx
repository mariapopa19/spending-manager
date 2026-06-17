import { useQuery } from "@tanstack/react-query";
import axios from "axios";
import {
  Area,
  AreaChart,
  CartesianGrid,
  Legend,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

type FrankfurterApi = {
  amount: number;
  base: string;
  start_date: string;
  end_date: string;
  rates: {
    [date: string]: {
      [currency: string]: number;
    };
  };
};

function App({ isAnimationActive = true }) {
  const getExchangeRatesForUSD = async (): Promise<FrankfurterApi> => {
    return (
      await axios.get(
        "https://api.frankfurter.dev/v1/2026-06-01..?base=EUR&symbols=USD",
      )
    ).data;
  };

  const { data, isLoading, isError } = useQuery({
    queryKey: ["exchangeRates"],
    queryFn: getExchangeRatesForUSD,
  });

  const chartData = data?.rates
    ? Object.entries(data.rates).map(([date, values]) => ({
        date,
        USD: values.USD,
      }))
    : [];

  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold text-emerald-600">Spending Manager</h1>
      {isLoading && <p>Loading...</p>}
      {isError && <p>Something went wrong.</p>}
      <p className="mt-4">
        <AreaChart
          style={{
            width: "100%",
            maxWidth: "700px",
            maxHeight: "70vh",
            aspectRatio: 1.618,
          }}
          responsive
          data={chartData}
          margin={{
            top: 20,
            right: 20,
            bottom: 5,
            left: 0,
          }}
        >
          <defs>
            <linearGradient id="colorUv" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8} />
              <stop offset="95%" stopColor="#8884d8" stopOpacity={0} />
            </linearGradient>
            <linearGradient id="colorPv" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#82ca9d" stopOpacity={0.8} />
              <stop offset="95%" stopColor="#82ca9d" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid stroke="#aaa" strokeDasharray="5 5" />
          <Area
            type="monotone"
            dataKey="USD"
            stroke="#8884d8"
            fillOpacity={1}
            fill="url(#colorUv)"
            isAnimationActive={isAnimationActive}
          />

          <Area
            type="monotone"
            dataKey="date"
            stroke="#82ca9d"
            fillOpacity={1}
            fill="url(#colorPv)"
            isAnimationActive={isAnimationActive}
          />
          {/* <Line
            type="monotone"
            dataKey="USD"
            stroke="purple"
            strokeWidth={2}
            name="USD rate"
          /> */}
          <XAxis dataKey="date" />
          <YAxis dataKey="USD" label={{ value: 'USD', position: 'insideLeft', angle: -90 }} />
          <Legend align="right" />
          <Tooltip />
          {/* <RechartsDevtools /> */}
        </AreaChart>
      </p>
    </div>
  );
}

export default App;

import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { CategoriesPage } from "./features/categories/CategoriesPage";
import { TransactionsPage } from "./features/transactions/TransactionsPage";
import { AppLayout } from "./components/AppLayout";
import { ImportPage } from "./features/import/ImportPage";
import { ReviewPage } from "./features/review/ReviewPage";
import { RulesPage } from "./features/rules/RulesPage";
import { DashboardPage } from "./features/dashboard/DashboardPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/transactions" element={<TransactionsPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/import" element={<ImportPage />} />
          <Route path="/review" element={<ReviewPage />} />
          <Route path="/rules" element={<RulesPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;

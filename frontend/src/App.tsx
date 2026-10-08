import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { CategoriesPage } from "./features/categories/CategoriesPage";
import { TransactionsPage } from "./features/transactions/TransactionsPage";
import { AppLayout } from "./components/AppLayout";
import { ImportPage } from "./features/import/ImportPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/transactions" replace />} />
          <Route path="/transactions" element={<TransactionsPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/import" element={<ImportPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;

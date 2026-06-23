import { BrowserRouter, NavLink, Route, Routes } from "react-router-dom";
import { CategoriesPage } from "./categories/CategoriesPage";
import { TransactionsPage } from "./transactions/TransactionsPage";

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen">
        {/* navigation bar */}
        <nav className="flex gap-4 border-b px-6 py-3 bg-gray-50">
          <NavLink
            to="/categories"
            className={({ isActive }) =>
              isActive
                ? "font-bold text-blue-600"
                : "text-gray-600 hover:text-blue-600"
            }
          >
            Categories
          </NavLink>
          <NavLink
            to="/transactions"
            className={({ isActive }) =>
              isActive
                ? "font-bold text-blue-600"
                : "text-gray-600 hover:text-blue-600"
            }
          >
            Transactions
          </NavLink>
        </nav>

        {/* page content */}
        <Routes>
          <Route path="/" element={<CategoriesPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/transactions" element={<TransactionsPage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;

import { NavLink, Outlet } from "react-router-dom";

const linkClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? "font-bold text-blue-600" : "text-gray-600 hover:text-blue-600";

export const AppLayout = () => (
  <div className="min-h-screen">
    <nav className="flex items-center gap-4 border-b bg-gray-50 px-6 py-3">
      <span className="mr-2 font-semibold">Spending Manager</span>
      <NavLink to="/transactions" className={linkClass}>
        Transactions
      </NavLink>
      <NavLink to="/categories" className={linkClass}>
        Categories
      </NavLink>
      <NavLink to="/import" className={linkClass}>
        Import
      </NavLink>
    </nav>
    <Outlet />
  </div>
);

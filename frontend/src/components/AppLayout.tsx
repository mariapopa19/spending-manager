import { NavLink, Outlet } from "react-router-dom";
import { useUncategorized } from "../features/review/api";

const linkClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? "font-bold text-blue-600" : "text-gray-600 hover:text-blue-600";

export const AppLayout = () => {
  const { data } = useUncategorized(0, 1);
  const count = data?.totalElements ?? 0;
  return (
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
        <NavLink to="/review" className={linkClass}>
          Review
          {count > 0 && (
            <span className="ml-1 rounded-full bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-700">
              {count}
            </span>
          )}
        </NavLink>
        <NavLink to="/rules" className={linkClass}>
          Rules
        </NavLink>
      </nav>
      <Outlet />
    </div>
  );
};

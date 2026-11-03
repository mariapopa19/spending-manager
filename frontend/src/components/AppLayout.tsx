import { NavLink, Outlet } from "react-router-dom";
import { useUncategorized } from "../features/review/api";
import { useLogout, useMe } from "../features/auth/api";
import { Button } from "./ui/Button";

const linkClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? "font-bold text-blue-600" : "text-gray-600 hover:text-blue-600";

export const AppLayout = () => {
  const { data } = useUncategorized(0, 1);
  const count = data?.page.totalElements ?? 0;
  const me = useMe(); // cached by RequireAuth already, so no extra request
  const logout = useLogout();
  return (
    <div className="min-h-screen">
      <nav className="border-b bg-gray-50">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-4 px-6 py-3">
          <span className="mr-2 font-semibold">Spending Manager</span>
          <NavLink to="/dashboard" className={linkClass}>
            Dashboard
          </NavLink>
          <NavLink to="/transactions" className={linkClass}>
            Tranzacții
          </NavLink>
          <NavLink to="/categories" className={linkClass}>
            Categorii
          </NavLink>
          <NavLink to="/import" className={linkClass}>
            Import
          </NavLink>
          <NavLink to="/review" className={linkClass}>
            Revizuire
            {count > 0 && (
              <span className="ml-1 rounded-full bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-700">
                {count}
              </span>
            )}
          </NavLink>
          <NavLink to="/rules" className={linkClass}>
            Reguli
          </NavLink>
          <div className="ml-auto flex items-center gap-3">
            <span className="text-sm text-gray-600">{me.data?.username}</span>
            <Button
              variant="ghost"
              className="py-1"
              onClick={() => logout.mutate()}
              disabled={logout.isPending}
            >
              Deconectare
            </Button>
          </div>
        </div>
      </nav>
      <Outlet />
    </div>
  );
};

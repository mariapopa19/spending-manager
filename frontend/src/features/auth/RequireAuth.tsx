import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useMe } from "./api";

export const RequireAuth = () => {
  const me = useMe();
  const location = useLocation();

  if (me.isPending)
    return <p className="p-6 text-sm text-gray-500">Se încarcă…</p>;
  if (me.isError)
    return <p className="p-6 text-sm text-red-700">{me.error.message}</p>;
  if (me.data === null)
    return <Navigate to="/login" replace state={{ from: location }} />;
  return <Outlet />;
};

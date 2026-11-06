import { Navigate, Outlet } from "react-router-dom";
import { useMe } from "./api"

export const RequireAdmin = () => {
    const me = useMe();
    if(me.data?.role !== "ADMIN") return <Navigate to="/dashboard" replace />
    return <Outlet />;
}
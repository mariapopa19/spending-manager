import { useState } from "react";
import { useLogin, useMe } from "./api";
import { Navigate, useLocation, type Location } from "react-router-dom";
import { Field } from "../../components/ui/Field";
import { Button } from "../../components/ui/Button";
import { ApiError } from "../../lib/ApiError";

const loginErrorMessage = (error: Error) => {
  if (error instanceof ApiError && error.status === 401)
    return "Utilizator sau parolă greșită";
  if (error instanceof ApiError && error.status === 403)
    return "Sesiune invalidă (CSRF). Reîncarcă pagina.";
  return error.message;
};

export const LoginPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const me = useMe();
  const login = useLogin();
  const location = useLocation();

  const from =
    (location.state as { from?: Location } | null)?.from?.pathname ??
    "/dashboard";

  if (me.isPending) return null; // avoids flashing the form for a logged-in user
  if (me.data) return <Navigate to={from} replace />;

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <form
        className="flex w-full max-w-sm flex-col gap-4 rounded-lg border border-gray-200 bg-white p-6"
        onSubmit={(e) => {
          e.preventDefault();
          login.mutate({ username, password });
        }}
      >
        <h1 className="text-lg font-semibold">Spending Manager</h1>
        <Field label="Utilizator">
          <input
            className="rounded-lg border px-3 py-2 text-sm"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            autoFocus
            required
          />
        </Field>
        <Field label="Parolă">
          <input
            type="password"
            className="rounded-lg border px-3 py-2 text-sm"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </Field>
        {login.isError && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {loginErrorMessage(login.error)}
          </p>
        )}
        <Button type="submit" className="w-full" disabled={login.isPending}>
          {login.isPending ? "Se autentifică…" : "Autentificare"}
        </Button>
      </form>
    </div>
  );
};

import { useState, type SubmitEvent } from "react";
import { useCreateUser, useUsers } from "./api";
import type { UserRole } from "../../types/domain";
import { Button } from "../../components/ui/Button";
import { Field } from "../../components/ui/Field";
import { ApiError } from "../../lib/ApiError";

export const UsersPage = () => {
  const { data: users, isLoading, isError, error } = useUsers();
  const createUser = useCreateUser();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<UserRole>("USER");

  const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    createUser.mutate(
      { username: username.trim(), password, role },
      {
        onSuccess: () => {
          setUsername("");
          setPassword("");
          setRole("USER");
        },
      },
    );
  };

  if (isLoading) return <p className="mx-auto max-w-6xl p-6">Se încarcă...</p>;
  if (isError)
    return (
      <p className="mx-auto max-w-6xl p-6 text-red-600">
        Eroare: {error.message}
      </p>
    );

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Utilizatori</h1>

      <form
        className="flex flex-wrap items-end gap-2 rounded-lg border border-gray-200 p-4"
        onSubmit={handleSubmit}
      >
        <Field label="Utilizator">
          <input
            className="min-w-40 rounded-lg border px-3 py-2 text-sm"
            value={username}
            onChange={(e) => setUsername(e.currentTarget.value)}
            autoComplete="off"
            required
            maxLength={100}
          />
        </Field>
        <Field label="Parolă">
          <input
            type="password"
            placeholder="Minim 8 caractere"
            className="min-w-40 rounded-lg border px-3 py-2 text-sm"
            value={password}
            onChange={(e) => setPassword(e.currentTarget.value)}
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            required
          />
        </Field>
        <Field label="Rol">
          <select
            className="rounded-lg border px-3 py-2 text-sm"
            value={role}
            onChange={(e) => setRole(e.target.value as UserRole)}
          >
            <option value="USER">Utilizator</option>
            <option value="ADMIN">Administrator</option>
          </select>
        </Field>
        <Button type="submit" disabled={createUser.isPending}>
          {createUser.isPending ? "..." : "Adaugă"}
        </Button>
      </form>

      {createUser.isError && (
        <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {createUser.error instanceof ApiError &&
          createUser.error.status === 409
            ? "Utilizatorul există deja"
            : createUser.error.message}
        </p>
      )}

      <div className="overflow-x-auto rounded-lg border">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase tracking-wide text-gray-500">
            <tr>
              <th className="px-3 py-2">Utilizator</th>
              <th className="px-3 py-2">Rol</th>
              <th className="px-3 py-2">Stare</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {users?.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50">
                <td className="px-3 py-2">{user.username}</td>
                <td className="px-3 py-2">
                  {user.role === "ADMIN" ? "Administrator" : "Utilizator"}
                </td>
                <td className="px-3 py-2">
                  {user.enabled ? "Activ" : "Dezactivat"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

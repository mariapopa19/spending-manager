import { useState, type SubmitEvent } from "react";
import { useChangePassword } from "../users/api";
import { Button } from "../../components/ui/Button";
import { Field } from "../../components/ui/Field";

export const AccountPage = () => {
  const changePassword = useChangePassword();
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const mismatch = confirm !== "" && confirm !== newPassword;

  const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (newPassword !== confirm) return;
    changePassword.mutate(
      { oldPassword, newPassword },
      {
        onSuccess: () => {
          setOldPassword("");
          setNewPassword("");
          setConfirm("");
        },
      },
    );
  };

  return (
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Cont</h1>

      <form
        className="flex w-full max-w-sm flex-col gap-4 rounded-lg border border-gray-200 p-4"
        onSubmit={handleSubmit}
      >
        <h2 className="text-lg font-semibold">Schimbă parola</h2>

        <Field label="Parola veche">
          <input
            type="password"
            className="rounded-lg border px-3 py-2 text-sm"
            value={oldPassword}
            onChange={(e) => setOldPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </Field>

        <Field label="Parola nouă" hint="Minim 8 caractere">
          <input
            type="password"
            className="rounded-lg border px-3 py-2 text-sm"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            required
          />
        </Field>

        <Field label="Confirmă parola nouă">
          <input
            type="password"
            className="rounded-lg border px-3 py-2 text-sm"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            autoComplete="new-password"
            required
          />
        </Field>

        {mismatch && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            Parolele nu coincid
          </p>
        )}
        {changePassword.isError && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {changePassword.error.message}
          </p>
        )}
        {changePassword.isSuccess && (
          <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
            Parola a fost schimbată
          </p>
        )}

        <Button type="submit" disabled={changePassword.isPending || mismatch}>
          {changePassword.isPending ? "Se salvează…" : "Schimbă parola"}
        </Button>
      </form>
    </div>
  );
};

import { Link } from "react-router-dom";
import { Button } from "../../components/ui/Button";
import type { ImportResultResponse } from "./types";

type Props = {
  result: ImportResultResponse;
  onAgain: () => void;
};

export const DoneStep = ({ result, onAgain }: Props) => (
  <div className="flex flex-col items-start gap-4 rounded-lg border bg-green-50 p-6">
    <p className="text-lg">
      Am importat <strong>{result.imported}</strong>{" "}
      {result.imported === 1 ? "tranzacție" : "tranzacții"}
      {result.skipped > 0 && (
        <>
          , am sărit <strong>{result.skipped}</strong> deja existente în baza de date
        </>
      )}
    </p>
    <div className="flex flex-wrap items-center gap-2">
      <Button onClick={onAgain}>Importă alt extras</Button>
      <Link
        to="/transactions"
        className="px-4 py-2 text-sm text-blue-600 underline"
      >
        Vezi tranzacțiile
      </Link>
      <Link to="/review" className="px-4 py-2 text-sm text-blue-600 underline">
        Revizuiește tranzacțiile fără categorie
      </Link>
    </div>
  </div>
);

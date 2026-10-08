import { Link } from "react-router-dom";
import { Button } from "../../components/ui/Button";
import type { ImportResultResponse } from "./types"

type Props = {
    result: ImportResultResponse;
    onAgain: () => void;
};

export const DoneStep = ({result, onAgain}: Props) => (
    <div className="flex flex-col items-start gap-4 rounded-lg border bg-green-50 p-6">
        <p className="text-lg">
            Imported <strong>{result.imported}</strong> transaction
            {result.imported === 1 ? "" : "s"}
            {result.skipped > 0 && <>, skipped <strong>{result.skipped}</strong> already in the database</>}
        </p>
        <div className="flex gap-2">
            <Button onClick={onAgain}>Import another statement</Button>
            <Link to="/transactions" className="px-4 py-2 text-sm text-blue-600 underline">
                View transactions
            </Link>
        </div>
    </div>
);
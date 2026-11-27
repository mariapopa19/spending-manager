import { usePersons } from "./api";

type Props = {
  value: number | undefined;
  onChange: (personId: number | undefined) => void;
};

export const PersonSelect = ({ value, onChange }: Props) => {
  const { data: persons } = usePersons();
  return (
    <select
      value={value ?? ""}
      onChange={(event) =>
        onChange(event.target.value ? Number(event.target.value) : undefined)
      }
      className="rounded-lg border border-gray-200 px-2 py-1"
    >
      <option value="">Toate persoanele</option>
      {persons?.map((person) => (
        <option key={person.id} value={person.id}>
          {person.name}
        </option>
      ))}
    </select>
  );
};

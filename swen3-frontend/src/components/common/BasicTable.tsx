import type { Key, ReactNode } from "react";
import "@/styling/common/BasicTable.css";

export type ColumnAlign = "left" | "center" | "right";

type ColumnBase = {
    label: string;
    align?: ColumnAlign;
};

type DataColumn<T> = {
    [K in keyof T & string]: ColumnBase & {
    key: K;
    render?: (row: T) => ReactNode;
};
}[keyof T & string];

type CustomColumn<T> = ColumnBase & {
    key: string;
    render: (row: T) => ReactNode;
};

export type Column<T> = DataColumn<T> | CustomColumn<T>;

export type BasicTableProps<T extends object> = {
    columns: ReadonlyArray<Column<T>>;
    data: ReadonlyArray<T>;
    getRowKey?: (row: T, index: number) => Key;
    onRowClick?: (row: T) => void;
    emptyText?: string;
    caption?: string;
    className?: string;
};

function formatValue(value: unknown): ReactNode {
    if (value === null || value === undefined) return "–";
    if (typeof value === "boolean") return value ? "Ja" : "Nein";
    if (typeof value === "string" || typeof value === "number" || typeof value === "bigint") {
        return String(value);
    }
    if (value instanceof Date) return value.toLocaleDateString("de-DE");
    return String(value);
}

export default function BasicTable<T extends object>({
                                                         columns,
                                                         data,
                                                         getRowKey,
                                                         onRowClick,
                                                         emptyText = "Keine Daten vorhanden.",
                                                         caption,
                                                         className,
                                                     }: BasicTableProps<T>) {
    const clickable = onRowClick !== undefined;
    const wrapperClass = className ? `basic-table-wrapper ${className}` : "basic-table-wrapper";

    return (
        <div className={wrapperClass}>
            <table className="basic-table">
                {caption && <caption className="basic-table__caption">{caption}</caption>}

                <thead>
                <tr>
                    {columns.map((col) => (
                        <th
                            key={col.key}
                            scope="col"
                            className={`basic-table__cell--${col.align ?? "left"}`}
                        >
                            {col.label}
                        </th>
                    ))}
                </tr>
                </thead>

                <tbody>
                {data.length === 0 ? (
                    <tr>
                        <td className="basic-table__empty" colSpan={Math.max(columns.length, 1)}>
                            {emptyText}
                        </td>
                    </tr>
                ) : (
                    data.map((row, index) => (
                        <tr
                            key={getRowKey ? getRowKey(row, index) : index}
                            className={clickable ? "basic-table__row--clickable" : undefined}
                            tabIndex={clickable ? 0 : undefined}
                            onClick={clickable ? () => onRowClick(row) : undefined}
                            onKeyDown={
                                clickable
                                    ? (e) => {
                                        if (e.key === "Enter" || e.key === " ") {
                                            e.preventDefault();
                                            onRowClick(row);
                                        }
                                    }
                                    : undefined
                            }
                        >
                            {columns.map((col) => (
                                <td
                                    key={col.key}
                                    className={`basic-table__cell--${col.align ?? "left"}`}
                                >
                                    {col.render
                                        ? col.render(row)
                                        : formatValue((row as Record<string, unknown>)[col.key])}
                                </td>
                            ))}
                        </tr>
                    ))
                )}
                </tbody>
            </table>
        </div>
    );
}
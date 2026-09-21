"use client";

import BasicTable, { type Column } from "@/components/common/BasicTable";
import BasicModal from "@/components/common/BasicModal";
import { useState } from "react";

interface User {
    id: number;
    name: string;
    email: string;
    active: boolean;
}

const columns: Column<User>[] = [
    { key: "id", label: "ID", align: "right" },
    { key: "name", label: "Name" },
    { key: "email", label: "E-Mail" },
    {
        key: "active",
        label: "Status",
        align: "center",
        render: (user) => (user.active ? "Aktiv" : "Inaktiv"),
    },
];

const users: User[] = [
    { id: 1, name: "Anna Muster", email: "anna@example.com", active: true },
    { id: 2, name: "Ben Beispiel", email: "ben@example.com", active: false },
    { id: 3, name: "Clara Test", email: "clara@example.com", active: true },
];

export default function DocumentsTable() {
    // null = Modal geschlossen, sonst der angeklickte User
    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    const closeModal = () => setSelectedUser(null);

    return (
        <>
            <BasicTable<User>
                caption="Alle Dokumente"
                columns={columns}
                data={users}
                getRowKey={(user) => user.id}
                onRowClick={(user) => setSelectedUser(user)}
            />

            <BasicModal
                isOpen={selectedUser !== null}
                onClose={closeModal}
                title={"Detailansicht"}
            >
                <div>TEST</div>
            </BasicModal>
        </>
    );
}
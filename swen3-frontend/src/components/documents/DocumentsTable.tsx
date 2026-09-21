"use client";

import BasicTable, { type Column } from "@/components/common/BasicTable";
import BasicModal from "@/components/common/BasicModal";
import { useState } from "react";

interface Document {
    id: number;
    name: string;
    fileSize: number;
    uploadedAt: string;
}

const columns: Column<Document>[] = [
    { key: "id", label: "ID", align: "left" },
    { key: "name", label: "Name" },
    { key: "fileSize", label: "Dateigröße" },
    {
        key: "uploadedAt",
        label: "Erstellt am",
        align: "right",
    },
];

const docs: Document[] = [
    { id: 1, name: "TestPDF1", fileSize: 2, uploadedAt: "09.09.2026" },
    { id: 2, name: "TestPDF2", fileSize: 12, uploadedAt: "09.09.2026" },
    { id: 3, name: "TestPDF3", fileSize: 30, uploadedAt: "09.09.2026" },
];

export default function DocumentsTable() {
    // null = Modal geschlossen, sonst der angeklickte User
    const [selectedDocument, setSelectedDocument] = useState<Document | null>(null);

    const closeModal = () => setSelectedDocument(null);

    return (
        <>
            <BasicTable<Document>
                caption="Alle Dokumente"
                columns={columns}
                data={docs}
                getRowKey={(user) => user.id}
                onRowClick={(docs) => setSelectedDocument(docs)}
            />

            <BasicModal
                isOpen={selectedDocument !== null}
                onClose={closeModal}
                title={"Detailansicht"}
            >
                <div>TEST</div>
            </BasicModal>
        </>
    );
}
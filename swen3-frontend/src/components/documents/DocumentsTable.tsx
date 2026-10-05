"use client";

import BasicTable, { type Column } from "@/components/common/BasicTable";
import BasicModal from "@/components/common/BasicModal";
import type { DocumentResponse } from "@/data/model/Document";
import { deleteDocumentById, fetchAllDocuments } from "@/data/repository/DocumentRepository";
import { useEffect, useState } from "react";

const columns: Column<DocumentResponse>[] = [
    { key: "id", label: "ID", align: "left" },
    { key: "name", label: "Name" },
    {
        key: "fileSize",
        label: "Dateigröße",
        render: (document) => `${(document.fileSize / 1024).toFixed(1)} KB`,
    },
    {
        key: "uploadedAt",
        label: "Erstellt am",
        align: "right",
        render: (document) => new Date(document.uploadedAt).toLocaleString("de-AT"),
    },
];

export default function DocumentsTable() {
    const [documents, setDocuments] = useState<DocumentResponse[]>([]);
    const [selectedDocument, setSelectedDocument] = useState<DocumentResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [deleting, setDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let active = true;

        fetchAllDocuments()
            .then((result) => {
                if (active) setDocuments(result);
            })
            .catch((loadError: unknown) => {
                if (active) {
                    setError(loadError instanceof Error ? loadError.message : "Dokumente konnten nicht geladen werden.");
                }
            })
            .finally(() => {
                if (active) setLoading(false);
            });

        return () => {
            active = false;
        };
    }, []);

    const closeModal = () => setSelectedDocument(null);

    const deleteSelectedDocument = async () => {
        if (!selectedDocument) return;

        setDeleting(true);
        setError(null);

        try {
            await deleteDocumentById(selectedDocument.id);
            setDocuments((previous) => previous.filter((document) => document.id !== selectedDocument.id));
            closeModal();
        } catch (deleteError) {
            setError(deleteError instanceof Error ? deleteError.message : "Dokument konnte nicht gelöscht werden.");
        } finally {
            setDeleting(false);
        }
    };

    if (loading) return <p>Dokumente werden geladen...</p>;

    return (
        <>
            {error && <p role="alert">{error}</p>}

            <BasicTable<DocumentResponse>
                caption="Alle Dokumente"
                columns={columns}
                data={documents}
                getRowKey={(document) => document.id}
                onRowClick={setSelectedDocument}
            />

            <BasicModal
                isOpen={selectedDocument !== null}
                onClose={closeModal}
                title="Dokumentdetails"
                footer={
                    <button type="button" onClick={deleteSelectedDocument} disabled={deleting}>
                        {deleting ? "Wird gelöscht..." : "Dokument löschen"}
                    </button>
                }
            >
                {selectedDocument && (
                    <dl>
                        <dt>Name</dt>
                        <dd>{selectedDocument.name}</dd>
                        <dt>Dateigröße</dt>
                        <dd>{(selectedDocument.fileSize / 1024).toFixed(1)} KB</dd>
                        <dt>Tags</dt>
                        <dd>{selectedDocument.tags.length > 0 ? selectedDocument.tags.join(", ") : "Keine"}</dd>
                    </dl>
                )}
            </BasicModal>
        </>
    );
}

"use client";


import "@/styling/documents/DocumentSearch.css";
import BasicTable, { type Column } from "@/components/common/BasicTable";
import type { DocumentResponse } from "@/data/model/Document";
import { deleteDocumentById, fetchAllDocuments, searchDocuments } from "@/data/repository/DocumentRepository";
import { useEffect, useState } from "react";
import DocumentDetailModal, {formatFileSize} from "@/components/documents/DocumentDetailModal";
import DownloadButton from "@/components/common/DownloadButton";

const columns: Column<DocumentResponse>[] = [
    { key: "id", label: "ID", align: "left" },
    { key: "name", label: "Name" },
    {
        key: "fileSize",
        label: "Dateigröße",
        render: (document) => formatFileSize(document.fileSize),
    },
    {
        key: "uploadedAt",
        label: "Erstellt am",
        align: "right",
        render: (document) => new Date(document.uploadedAt).toLocaleString("de-AT"),
    },

    {
        key: "action",
        label: "Download",
        align: "center",
        render: (document) => <DownloadButton document={document} />,
    },
];

export default function DocumentsTable() {
    const [documents, setDocuments] = useState<DocumentResponse[]>([]);
    const [selectedDocument, setSelectedDocument] = useState<DocumentResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [deleting, setDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [query, setQuery] = useState("");

    useEffect(() => {
        let active = true;
        const timer = setTimeout(() => {
            const load = query.trim() ? searchDocuments(query.trim()) : fetchAllDocuments();
            load
                .then((result) => {
                    if (active) {
                        setDocuments(result);
                        setError(null);
                    }
                })
                .catch((loadError: unknown) => {
                    if (active) {
                        setError(loadError instanceof Error ? loadError.message : "Dokumente konnten nicht geladen werden.");
                    }
                })
                .finally(() => {
                    if (active) setLoading(false);
                });
        }, 300);

        return () => {
            active = false;
            clearTimeout(timer);
        };
    }, [query]);

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

            <input
                type="search"
                className="documents-search"
                placeholder="Dokumente durchsuchen..."
                value={query}
                onChange={(event) => setQuery(event.target.value)}
            />

            <BasicTable<DocumentResponse>
                caption="Alle Dokumente"
                columns={columns}
                data={documents}
                getRowKey={(document) => document.id}
                onRowClick={setSelectedDocument}
            />

            <DocumentDetailModal
                document={selectedDocument}
                deleting={deleting}
                onClose={closeModal}
                onDelete={deleteSelectedDocument}
            />
        </>
    );
}

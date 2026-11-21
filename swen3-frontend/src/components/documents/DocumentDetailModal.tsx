"use client";

import BasicModal from "@/components/common/BasicModal";
import type { DocumentResponse } from "@/data/model/Document";
import "@/styling/documents/DocumentDetailModal.css";
import {fetchDocumentBlobById, fetchDocumentById} from "@/data/repository/DocumentRepository";
import {useEffect, useState} from "react";

type DocumentDetailModalProps = {
    document: DocumentResponse | null;
    deleting: boolean;
    onClose: () => void;
    onDelete: () => void;
};

export const formatFileSize = (bytes: number) => `${(bytes / 1024).toFixed(1)} KB`;

export default function DocumentDetailModal({ document, deleting, onClose, onDelete }: DocumentDetailModalProps) {

    const documentId = document?.id;
    const [pdfUrl, setPdfUrl] = useState<string | null>(null);
    const [previewFailed, setPreviewFailed] = useState(false);
    const [latest, setLatest] = useState<DocumentResponse | null>(null);
    const [summaryUnavailable, setSummaryUnavailable] = useState(false);
    const summaryDocument = latest && latest.id === documentId ? latest : document;

    useEffect(() => {
        if (documentId === undefined) return;

        let cancelled = false;
        let timer: ReturnType<typeof setTimeout>;
        async function refreshSummary(id: number) {
            try {
                const updated = await fetchDocumentById(id);
                if (cancelled) return;
                setLatest(updated);
                setSummaryUnavailable(false);
                if (updated.summaryStatus === "PENDING") {
                    timer = setTimeout(() => void refreshSummary(id), 3000);
                }
            } catch {
                if (!cancelled) setSummaryUnavailable(true);
            }
        }
        void refreshSummary(documentId);
        return () => {
            cancelled = true;
            clearTimeout(timer);
            setLatest(null);
            setSummaryUnavailable(false);
        };
    }, [documentId]);

    useEffect(() => {
        if (documentId === undefined) return;

        let cancelled = false;
        let url: string | null = null;

        fetchDocumentBlobById(documentId)
            .then((blob) => {
                if (cancelled) return;
                url = URL.createObjectURL(blob);
                setPdfUrl(url);
            })
            .catch(() => {
                if (!cancelled) setPreviewFailed(true);
            });

        return () => {
            cancelled = true;
            if (url) URL.revokeObjectURL(url);
            setPdfUrl(null);
            setPreviewFailed(false);
        };
    }, [documentId]);


    return (
        <BasicModal
            isOpen={document !== null}
            onClose={onClose}
            title="Dokumentdetails"
            footer={
                <div className="document-detail__footer">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={deleting}
                        className="document-detail__button"
                    >
                        Abbrechen
                    </button>
                    <button
                        type="button"
                        onClick={onDelete}
                        disabled={deleting}
                        className="document-detail__button document-detail__button--danger"
                    >
                        {deleting ? "Wird gelöscht..." : "Dokument löschen"}
                    </button>
                </div>
            }
        >
            {document && (
                <>
                <dl className="document-detail">
                    <dt className="document-detail__label">Name</dt>
                    <dd className="document-detail__value document-detail__value--strong">{document.name}</dd>

                    <dt className="document-detail__label">Dateigröße</dt>
                    <dd className="document-detail__value">{formatFileSize(document.fileSize)}</dd>

                    <dt className="document-detail__label">Erstellt am</dt>
                    <dd className="document-detail__value">
                        {new Date(document.uploadedAt).toLocaleString("de-AT")}
                    </dd>

                    <dt className="document-detail__label">Tags</dt>
                    <dd className="document-detail__value">
                        {document.tags.length > 0 ? (
                            <ul className="document-detail__tags">
                                {document.tags.map((tag) => (
                                    <li key={tag} className="document-detail__tag">
                                        {tag}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <span className="document-detail__empty">Keine</span>
                        )}
                    </dd>
                    <dt className="document-detail__label">KI-Zusammenfassung</dt>
                    <dd className="document-detail__value" style={{whiteSpace: "pre-wrap", overflowWrap: "anywhere"}}>
                        {summaryDocument?.summary ?? (summaryUnavailable
                            ? "Die Zusammenfassung konnte nicht abgerufen werden."
                            : summaryDocument?.summaryStatus === "PENDING"
                                ? "Zusammenfassung wird erstellt …"
                                : summaryDocument?.summaryStatus === "NO_TEXT"
                                    ? "Im PDF wurde kein Text erkannt."
                                    : summaryDocument?.summaryStatus === "FAILED"
                                        ? "Zusammenfassung konnte nicht erstellt werden."
                                        : "Keine Zusammenfassung vorhanden.")}
                    </dd>
                </dl>
                    {previewFailed ? (
                        <p className="document-detail__empty">Vorschau nicht verfügbar.</p>
                    ) : pdfUrl ? (
                        <iframe src={pdfUrl} width="100%" height="600" title={document.name} />
                        ) : (
                            <p>Lädt ...</p>
                    )
                    }
                </>
            )}
        </BasicModal>
    );
}
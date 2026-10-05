"use client";

import BasicModal from "@/components/common/BasicModal";
import type { DocumentResponse } from "@/data/model/Document";
import "@/styling/documents/DocumentDetailModal.css";

type DocumentDetailModalProps = {
    document: DocumentResponse | null;
    deleting: boolean;
    onClose: () => void;
    onDelete: () => void;
};

export const formatFileSize = (bytes: number) => `${(bytes / 1024).toFixed(1)} KB`;

export default function DocumentDetailModal({ document, deleting, onClose, onDelete }: DocumentDetailModalProps) {
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
                </dl>
            )}
        </BasicModal>
    );
}
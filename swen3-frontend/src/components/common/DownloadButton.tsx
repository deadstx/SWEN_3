"use client";

import { useState } from "react";
import type { DocumentResponse } from "@/data/model/Document";
import { downloadDocumentById } from "@/data/repository/DocumentRepository";
import "@/styling/common/DownloadButton.css";

export default function DownloadButton({ document }: { document: DocumentResponse }) {
    const [loading, setLoading] = useState(false);
    const [failed, setFailed] = useState(false);

    const handleClick = async (event: React.MouseEvent) => {
        event.stopPropagation(); // verhindert, dass die Zeile das Detail-Modal öffnet

        setLoading(true);
        setFailed(false);

        try {
            await downloadDocumentById(document.id, document.name);
        } catch {
            setFailed(true);
        } finally {
            setLoading(false);
        }
    };

    return (
        <button
            type="button"
            className="download-button"
            onClick={handleClick}
            disabled={loading}
            title={failed ? "Download fehlgeschlagen, erneut versuchen" : undefined}
        >
            {loading ? "Lädt..." : failed ? "Fehler" : "Download"}
        </button>
    );
}
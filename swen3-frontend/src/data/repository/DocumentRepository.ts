import type { DocumentResponse } from "@/data/model/Document";
import {request, requestBlob} from "@/data/api/client";

// CREATE
export function uploadDocument(file: File): Promise<DocumentResponse> {
    const formData = new FormData();
    formData.append("file", file);

    return request<DocumentResponse>("/api/documents", {
        method: "POST",
        body: formData,
    });
}

// READ
export function fetchAllDocuments(): Promise<DocumentResponse[]> {
    return request<DocumentResponse[]>("/api/documents", { cache: "no-store" });
}

// DELETE
export function deleteDocumentById(id: number): Promise<void> {
    return request<void>(`/api/documents/${id}`, { method: "DELETE" });
}


// DOWNLOAD
export async function downloadDocumentById(id: number, fileName: string): Promise<void> {
    const blob = await requestBlob(`/api/documents/${id}/download`);
    const url = URL.createObjectURL(blob);

    const link = window.document.createElement("a");
    link.href = url;
    link.download = fileName;
    link.click();

    URL.revokeObjectURL(url);
}
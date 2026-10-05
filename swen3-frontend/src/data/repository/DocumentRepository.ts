import type { DocumentResponse } from "@/data/model/Document";
import { request} from "@/data/api/client";

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
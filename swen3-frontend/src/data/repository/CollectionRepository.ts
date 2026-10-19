import { request } from "@/data/api/client";
import type { DocumentCollectionResponse } from "@/data/model/DocumentCollection";

export function fetchAllCollections(): Promise<DocumentCollectionResponse[]> {
    return request("/api/collections", { cache: "no-store" });
}

export function fetchCollectionById(id: number): Promise<DocumentCollectionResponse> {
    return request(`/api/collections/${id}`, { cache: "no-store" });
}

export function createCollection(name: string): Promise<DocumentCollectionResponse> {
    return request("/api/collections", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
    });
}

export function deleteCollectionById(id: number): Promise<void> {
    return request(`/api/collections/${id}`, { method: "DELETE" });
}

export function addDocumentToCollection(id: number, documentId: number): Promise<void> {
    return request(`/api/collections/${id}/documents/${documentId}`, { method: "PUT" });
}

export function removeDocumentFromCollection(id: number, documentId: number): Promise<void> {
    return request(`/api/collections/${id}/documents/${documentId}`, { method: "DELETE" });
}

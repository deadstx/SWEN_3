import type { DocumentResponse } from "@/data/model/Document";

export interface DocumentCollectionResponse {
    id: number;
    name: string;
    documents: DocumentResponse[];
}

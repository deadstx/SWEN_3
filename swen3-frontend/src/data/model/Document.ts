export interface DocumentResponse {
    id: number;
    name: string;
    fileSize: number;
    uploadedAt: string;
    tags: string[];
    summary: string | null;
    summaryStatus: "PENDING" | "COMPLETED" | "NO_TEXT" | "FAILED" | null;
}

import MainLayout from "@/components/layout/MainLayout";
import DocumentsTable from "@/components/documents/DocumentsTable";

export default function DocumentsPage() {
    return (
        <MainLayout title="Dokumente" button={true}>
            <DocumentsTable />
        </MainLayout>
    );
}
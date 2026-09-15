import MainLayout from "@/components/layout/MainLayout";
import UploadForm from "@/components/upload/UploadForm";

// STYLING
import "@/styling/upload/UploadForm.css"

export default function UploadPage() {
    return (
        <MainLayout>
            <UploadForm />
        </MainLayout>
    );
}
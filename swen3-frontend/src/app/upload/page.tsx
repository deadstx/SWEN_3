import MainLayout from "@/components/layout/MainLayout";
import UploadForm from "@/components/upload/UploadForm";


export default function UploadPage() {
    return (
        <MainLayout title={"Upload"}>
            <UploadForm />
        </MainLayout>
    );
}
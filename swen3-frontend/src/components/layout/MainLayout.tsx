import Sidebar from "@/components/layout/Sidebar";
import PageHeader from "@/components/layout/PageHeader";
import "@/styling/layout/MainLayout.css";

type MainLayoutProps = {
    children: React.ReactNode;
    title: string;
    subtitle?: string;
};

export default function MainLayout({ children, title, subtitle }: MainLayoutProps) {
    return (
        <div className="main-layout">
            <Sidebar />
            <div className="main-layout__body">
                <PageHeader title={title} subtitle={subtitle} />
                <main className="main-layout__content">{children}</main>
            </div>
        </div>
    );
}
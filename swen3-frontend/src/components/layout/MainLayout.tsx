import Sidebar from "@/components/layout/Sidebar";
import PageHeader from "@/components/layout/PageHeader";
import "@/styling/layout/MainLayout.css";

type MainLayoutProps = {
    children: React.ReactNode;
    title: string;
    subtitle?: string;
    button?: boolean;
};

export default function MainLayout({ children, title, button }: MainLayoutProps) {
    return (
        <div className="main-layout">
            <Sidebar />
            <div className="main-layout__body">
                <PageHeader title={title} button={button} />
                <main className="main-layout__content">{children}</main>
            </div>
        </div>
    );
}
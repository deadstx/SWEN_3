import Sidebar from "@/components/layout/Sidebar";
import "@/styling/layout/MainLayout.css";



type MainLayoutProps = {
    children: React.ReactNode;
};

export default function MainLayout({ children }: MainLayoutProps) {
    return (
        <div className="main-layout">
            <Sidebar />
            <main className="main-layout__content">{children}</main>
        </div>
    );
}
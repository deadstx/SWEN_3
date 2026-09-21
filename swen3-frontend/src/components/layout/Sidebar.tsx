import Link from "next/link";
import "@/styling/layout/Sidebar.css";
import ThemeToggle from "@/components/toggles/ThemeToggle";

export default function Sidebar() {
    return (
        <aside className="sidebar">
            <div className="sidebar__logo">
                <span>DocuManager</span>
            </div>

            <nav className="sidebar__nav">
                <Link href="/" className="sidebar__link">
                    Dashboard
                </Link>
                <Link href="/documents" className="sidebar__link">
                    Dokumente
                </Link>
                <Link href="/upload" className="sidebar__link">
                    Upload
                </Link>
            </nav>

            <div className="sidebar__toggle">
                <ThemeToggle></ThemeToggle>
            </div>
        </aside>
    );
}
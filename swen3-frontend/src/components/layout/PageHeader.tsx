import "@/styling/layout/PageHeader.css";
import {PlusIcon} from "lucide-react";

interface PageHeaderProps {
    title: string;
    subtitle?: string;
    button?: boolean;
}

export default function PageHeader({ title, button }: PageHeaderProps) {
    return (
        <header className="page-header">
            <h1 className="page-header__title">{title}</h1>

            {button && <a><PlusIcon></PlusIcon></a>}
        </header>
    );
}
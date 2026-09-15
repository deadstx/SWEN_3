import Link from "next/link";

import "@/styling/error/PageNotFound.css";

export default function PageNotFound() {
    return (
        <div className="nf">
            <div className="nf__grid" />

            <div className="nf__paper">
                <div className="nf__paper-corner" />

                <div className="nf__paper-lines">
                    <span />
                    <span />
                    <span />
                    <span />
                    <span />
                </div>

                <div className="nf__scanline" />

                <div className="nf__stamp">
                    <span className="nf__stamp-code">404</span>
                    <span className="nf__stamp-label">nicht gefunden</span>
                </div>
            </div>

            <h1 className="nf__title">Diese Seite gibt es nicht</h1>
            <p className="nf__text">
                Die angeforderte Seite wurde nicht gefunden, verschoben oder
                abgelegt, wo sie niemand mehr wiederfindet.
            </p>

            <Link href="/" className="nf__link">
                Zurück zur Übersicht
            </Link>
        </div>
    );
}
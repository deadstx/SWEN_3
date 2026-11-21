
import type { Metadata } from "next";

//STYLING
import "@/app/globals.css";
import Provider from "@/providers/ThemeProvider";


export const metadata: Metadata = {
    title: "Document Management System",
    description: "Semester Project SWEN3 | BIF5",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
    return (
        <html lang="de" suppressHydrationWarning>
        <body style={{ margin: 0 }}>
        <Provider>
            {children}
        </Provider>
        </body>
        </html>
    );
}
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";

//STYLING
import "@/app/globals.css";


const geistSans = Geist({
    variable: "--font-geist-sans",
    subsets: ["latin"],
});

const geistMono = Geist_Mono({
    variable: "--font-geist-mono",
    subsets: ["latin"],
});

export const metadata: Metadata = {
    title: "Document Management System",
    description: "Semester Project SWEN3 | BIF5",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
    return (
        <html lang="de" className={`${geistSans.variable} ${geistMono.variable}`}>
        <body style={{ margin: 0 }}>{children}</body>
        </html>
    );
}
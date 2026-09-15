"use client";

import { useTheme } from "next-themes";
import { useSyncExternalStore } from "react";

import "@/styling/toggles/ThemeToggle.css";

// Liefert erst nach der Hydration "true" zurück, ganz ohne setState im Effect.
function subscribe() {
    return () => {};
}

function useMounted() {
    return useSyncExternalStore(
        subscribe,
        () => true,   // Client-Snapshot
        () => false   // Server-Snapshot
    );
}

export default function ThemeToggle() {
    const { resolvedTheme, setTheme } = useTheme();
    const mounted = useMounted();

    const isDark = mounted && resolvedTheme === "dark";

    return (
        <button
            type="button"
            className="theme-toggle"
            data-state={mounted ? (isDark ? "dark" : "light") : undefined}
            onClick={() => setTheme(isDark ? "light" : "dark")}
            aria-label={isDark ? "Zu Light Mode wechseln" : "Zu Dark Mode wechseln"}
        >
            <span className="theme-toggle__track">
                <span className="theme-toggle__icon theme-toggle__icon--sun">
                    <SunIcon />
                </span>
                <span className="theme-toggle__icon theme-toggle__icon--moon">
                    <MoonIcon />
                </span>
                <span className="theme-toggle__thumb" />
            </span>
        </button>
    );
}

function SunIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="4.5" stroke="currentColor" strokeWidth="1.8" />
            <g stroke="currentColor" strokeWidth="1.8" strokeLinecap="round">
                <line x1="12" y1="2.5" x2="12" y2="4.5" />
                <line x1="12" y1="19.5" x2="12" y2="21.5" />
                <line x1="4.2" y1="4.2" x2="5.6" y2="5.6" />
                <line x1="18.4" y1="18.4" x2="19.8" y2="19.8" />
                <line x1="2.5" y1="12" x2="4.5" y2="12" />
                <line x1="19.5" y1="12" x2="21.5" y2="12" />
                <line x1="4.2" y1="19.8" x2="5.6" y2="18.4" />
                <line x1="18.4" y1="5.6" x2="19.8" y2="4.2" />
            </g>
        </svg>
    );
}

function MoonIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M20.5 14.2c-1.1.4-2.3.6-3.5.6-5.2 0-9.4-4.2-9.4-9.4 0-1.2.2-2.4.6-3.5C4.5 3.5 2 6.9 2 11c0 5.5 4.5 10 10 10 4.1 0 7.5-2.5 9-6.1-.1 0-.3.1-.5.3z"
                fill="currentColor"
            />
        </svg>
    );
}
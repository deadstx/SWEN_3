"use client";

import { useEffect, useId, useRef } from "react";
import type { ReactNode } from "react";
import { createPortal } from "react-dom";
import "@/styling/common/BasicModal.css";

export type ModalSize = "sm" | "md" | "lg";

export type BasicModalProps = {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    children: ReactNode;
    footer?: ReactNode;
    size?: ModalSize;
    closeOnBackdrop?: boolean;
    className?: string;
};

export default function BasicModal({
                                       isOpen,
                                       onClose,
                                       title,
                                       children,
                                       footer,
                                       size = "md",
                                       closeOnBackdrop = true,
                                       className,
                                   }: BasicModalProps) {
    const titleId = useId();
    const dialogRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!isOpen) return;

        const previouslyFocused = document.activeElement as HTMLElement | null;
        const previousOverflow = document.body.style.overflow;

        document.body.style.overflow = "hidden";
        dialogRef.current?.focus();

        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") onClose();
        };
        document.addEventListener("keydown", handleKeyDown);

        return () => {
            document.removeEventListener("keydown", handleKeyDown);
            document.body.style.overflow = previousOverflow;
            previouslyFocused?.focus();
        };
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    const dialogClass = [
        "basic-modal",
        `basic-modal--${size}`,
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return createPortal(
        <div
            className="basic-modal__backdrop"
            onMouseDown={(e) => {
                if (closeOnBackdrop && e.target === e.currentTarget) onClose();
            }}
        >
            <div
                ref={dialogRef}
                className={dialogClass}
                role="dialog"
                aria-modal="true"
                aria-labelledby={title ? titleId : undefined}
                tabIndex={-1}
            >
                <div className="basic-modal__header">
                    {title && (
                        <h2 id={titleId} className="basic-modal__title">
                            {title}
                        </h2>
                    )}
                    <button
                        type="button"
                        className="basic-modal__close"
                        onClick={onClose}
                        aria-label="Schließen"
                    >
                        ×
                    </button>
                </div>

                <div className="basic-modal__body">{children}</div>

                {footer && <div className="basic-modal__footer">{footer}</div>}
            </div>
        </div>,
        document.body,
    );
}
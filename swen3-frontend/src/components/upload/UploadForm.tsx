"use client";

import { useCallback, useRef, useState } from "react";

import "@/styling/upload/UploadForm.css";

type UploadStatus = "idle" | "uploading" | "success" | "error";

interface UploadFile {
    file: File;
    progress: number;
    status: UploadStatus;
}

export default function UploadForm() {
    const [isDragging, setIsDragging] = useState(false);
    const [uploads, setUploads] = useState<UploadFile[]>([]);
    const inputRef = useRef<HTMLInputElement>(null);

    const handleFiles = useCallback((fileList: FileList | null) => {
        if (!fileList) return;

        const pdfFiles = Array.from(fileList).filter(
            (file) => file.type === "application/pdf"
        );

        if (pdfFiles.length === 0) return;

        const newUploads: UploadFile[] = pdfFiles.map((file) => ({
            file,
            progress: 0,
            status: "uploading",
        }));

        setUploads((prev) => [...prev, ...newUploads]);

        // Mock-Upload mit Fortschritts-Animation.
        // TODO: durch echten API-Call ersetzen (z.B. fetch mit FormData + Progress via XHR).
        newUploads.forEach((upload) => {
            simulateUpload(upload.file);
        });
    }, []);

    const simulateUpload = (targetFile: File) => {
        let progress = 0;

        const interval = setInterval(() => {
            progress += Math.random() * 20 + 10;

            if (progress >= 100) {
                progress = 100;
                clearInterval(interval);

                setUploads((prev) =>
                    prev.map((u) =>
                        u.file === targetFile
                            ? { ...u, progress: 100, status: "success" }
                            : u
                    )
                );
                return;
            }

            setUploads((prev) =>
                prev.map((u) =>
                    u.file === targetFile ? { ...u, progress } : u
                )
            );
        }, 250);
    };

    const removeUpload = (targetFile: File) => {
        setUploads((prev) => prev.filter((u) => u.file !== targetFile));
    };

    const onDrop = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        setIsDragging(false);
        handleFiles(e.dataTransfer.files);
    };

    const onDragOver = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        setIsDragging(true);
    };

    const onDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        setIsDragging(false);
    };

    return (
        <div className="upload-form-outer-container">
            <div className="upload-form-container">
                <span className="upload-form-headline">PDF Dateien hochladen</span>
                <p className="upload-form-subtext">
                    Ziehe deine Dateien hierher oder wähle sie manuell aus
                </p>

                <div
                    className={`upload-dropzone ${isDragging ? "upload-dropzone--active" : ""}`}
                    onDrop={onDrop}
                    onDragOver={onDragOver}
                    onDragLeave={onDragLeave}
                    onClick={() => inputRef.current?.click()}
                >
                    <div className="upload-dropzone__icon">
                        <UploadIcon />
                    </div>
                    <span className="upload-dropzone__text">
                        {isDragging ? "Datei hier loslassen" : "Klicken oder Datei hierher ziehen"}
                    </span>
                    <span className="upload-dropzone__hint">Nur PDF-Dateien</span>

                    <input
                        ref={inputRef}
                        type="file"
                        accept="application/pdf"
                        multiple
                        className="upload-dropzone__input"
                        onChange={(e) => handleFiles(e.target.files)}
                    />
                </div>

                {uploads.length > 0 && (
                    <div className="upload-list">
                        {uploads.map((upload, index) => (
                            <div
                                className="upload-item"
                                key={`${upload.file.name}-${index}`}
                            >
                                <div className="upload-item__icon">
                                    <PdfIcon />
                                </div>

                                <div className="upload-item__info">
                                    <span className="upload-item__name">
                                        {upload.file.name}
                                    </span>

                                    <div className="upload-item__bar-track">
                                        <div
                                            className="upload-item__bar-fill"
                                            style={{ width: `${upload.progress}%` }}
                                        />
                                    </div>
                                </div>

                                <div className="upload-item__status">
                                    {upload.status === "success" ? (
                                        <span className="upload-item__check">
                                            <CheckIcon />
                                        </span>
                                    ) : (
                                        <span className="upload-item__percent">
                                            {Math.round(upload.progress)}%
                                        </span>
                                    )}
                                </div>

                                <button
                                    type="button"
                                    className="upload-item__remove"
                                    aria-label="Datei entfernen"
                                    onClick={() => removeUpload(upload.file)}
                                >
                                    <CloseIcon />
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

function UploadIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 15V4M12 4L7.5 8.5M12 4l4.5 4.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
            <path d="M4 15v3a2 2 0 002 2h12a2 2 0 002-2v-3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    );
}

function PdfIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 2h9l5 5v13a2 2 0 01-2 2H6a2 2 0 01-2-2V4a2 2 0 012-2z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
            <path d="M15 2v5h5" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
        </svg>
    );
}

function CheckIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 12.5l5 5L20 6" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    );
}

function CloseIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
        </svg>
    );
}
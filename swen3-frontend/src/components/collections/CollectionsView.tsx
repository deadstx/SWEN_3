"use client";

import { useEffect, useState, type FormEvent } from "react";
import BasicTable from "@/components/common/BasicTable";
import DownloadButton from "@/components/common/DownloadButton";
import type { DocumentResponse } from "@/data/model/Document";
import type { DocumentCollectionResponse } from "@/data/model/DocumentCollection";
import { fetchAllDocuments } from "@/data/repository/DocumentRepository";
import {
    fetchAllCollections, fetchCollectionById, createCollection,
    deleteCollectionById, addDocumentToCollection, removeDocumentFromCollection,
} from "@/data/repository/CollectionRepository";
import "@/styling/collections/CollectionsView.css";

export default function CollectionsView() {
    const [collections, setCollections] = useState<DocumentCollectionResponse[]>([]);
    const [documents, setDocuments] = useState<DocumentResponse[]>([]);
    const [selected, setSelected] = useState<DocumentCollectionResponse | null>(null);
    const [name, setName] = useState("");
    const [documentId, setDocumentId] = useState("");
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [collectionsLoaded, setCollectionsLoaded] = useState(false);

    useEffect(() => {
        let active = true;
        Promise.allSettled([fetchAllCollections(), fetchAllDocuments()])
            .then(([loadedCollections, loadedDocuments]) => {
                if (!active) return;
                const errors: string[] = [];
                if (loadedCollections.status === "fulfilled") {
                    setCollections(loadedCollections.value);
                    setCollectionsLoaded(true);
                } else {
                    errors.push("Collections konnten nicht geladen werden. Bitte lade die Seite erneut.");
                }
                if (loadedDocuments.status === "fulfilled") {
                    setDocuments(loadedDocuments.value);
                } else {
                    errors.push("Dokumente konnten nicht geladen werden. Bitte lade die Seite erneut, um Dokumente hinzuzufügen.");
                }
                setLoadError(errors.length ? errors.join(" ") : null);
            })
            .finally(() => { if (active) setLoading(false); });
        return () => { active = false; };
    }, []);

    async function run(action: () => Promise<void>) {
        setBusy(true);
        setError(null);
        try {
            await action();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Die Aktion ist fehlgeschlagen.");
        } finally {
            setBusy(false);
        }
    }

    function selectCollection(id: number) {
        setSelected(null);
        setDocumentId("");
        void run(async () => { setSelected(await fetchCollectionById(id)); });
    }

    async function refreshSelected(id: number) {
        const updated = await fetchCollectionById(id);
        setSelected(updated);
        setCollections(previous => previous.map(collection => collection.id === id ? updated : collection));
    }

    function handleCreate(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!name.trim()) return;
        void run(async () => {
            const created = await createCollection(name.trim());
            setCollections(previous => [...previous, created]);
            setSelected(created);
            setDocumentId("");
            setName("");
        });
    }

    function handleDelete() {
        if (!selected || !window.confirm(`Collection „${selected.name}“ löschen? Die Dokumente bleiben erhalten.`)) return;
        void run(async () => {
            await deleteCollectionById(selected.id);
            setCollections(previous => previous.filter(collection => collection.id !== selected.id));
            setSelected(null);
        });
    }

    const availableDocuments = documents.filter(document => !selected?.documents.some(member => member.id === document.id));

    return (
        <div className="collections">
            <p>Organisiere deine Dokumente in Collections.</p>
            {loadError && <p role="alert" className="collections__error">{loadError}</p>}
            {error && <p role="alert" className="collections__error">{error}</p>}
            <form className="collections__form" onSubmit={handleCreate}>
                <label htmlFor="collection-name">Neue Collection</label>
                <input id="collection-name" value={name} onChange={event => setName(event.target.value)} placeholder="Name der Collection" required disabled={busy || loading} />
                <button type="submit" disabled={busy || loading || !name.trim()}>Collection erstellen</button>
            </form>
            {loading ? <p role="status">Collections werden geladen...</p> : (
                <div className="collections__layout" aria-busy={busy}>
                    <section className="collections__panel" aria-label="Alle Collections">
                        <h2>Collections ({collections.length})</h2>
                        {collectionsLoaded && collections.length === 0 && <p>Noch keine Collections vorhanden. Erstelle deine erste Collection.</p>}
                        <ul className="collections__list">
                            {collections.map(collection => (
                                <li key={collection.id}>
                                    <button type="button" className="collections__item" aria-pressed={selected?.id === collection.id} disabled={busy} onClick={() => selectCollection(collection.id)}>
                                        <span>{collection.name}</span>
                                        <small>{collection.documents.length} Dokumente</small>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </section>
                    <section className="collections__panel" aria-label="Collection-Details">
                        {selected ? <>
                            <div className="collections__heading">
                                <h2>{selected.name}</h2>
                                <button type="button" className="collections__danger" disabled={busy} onClick={handleDelete}>Collection löschen</button>
                            </div>
                            <form className="collections__form" onSubmit={event => {
                                event.preventDefault();
                                if (!documentId) return;
                                void run(async () => {
                                    await addDocumentToCollection(selected.id, Number(documentId));
                                    setDocumentId("");
                                    await refreshSelected(selected.id);
                                });
                            }}>
                                <label htmlFor="collection-document">Dokument hinzufügen</label>
                                <select id="collection-document" value={documentId} onChange={event => setDocumentId(event.target.value)} disabled={busy || !availableDocuments.length}>
                                    <option value="">{availableDocuments.length ? "Dokument auswählen" : "Keine weiteren Dokumente verfügbar"}</option>
                                    {availableDocuments.map(document => <option key={document.id} value={document.id}>{document.name}</option>)}
                                </select>
                                <button type="submit" disabled={busy || !documentId}>Hinzufügen</button>
                            </form>
                            <BasicTable<DocumentResponse>
                                caption="Dokumente dieser Collection"
                                data={selected.documents}
                                getRowKey={document => document.id}
                                emptyText="Diese Collection enthält noch keine Dokumente."
                                columns={[
                                    { key: "name", label: "Name" },
                                    { key: "download", label: "Download", render: document => <DownloadButton document={document} /> },
                                    { key: "remove", label: "Aktion", render: document => (
                                        <button type="button" disabled={busy} aria-label={`${document.name} aus Collection entfernen`} onClick={() => void run(async () => {
                                            await removeDocumentFromCollection(selected.id, document.id);
                                            await refreshSelected(selected.id);
                                        })}>Entfernen</button>
                                    ) },
                                ]}
                            />
                        </> : <p role="status">{busy ? "Collection wird geladen..." : "Wähle eine Collection aus, um ihre Dokumente zu verwalten."}</p>}
                    </section>
                </div>
            )}
        </div>
    );
}

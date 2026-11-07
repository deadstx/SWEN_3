# Document Management System

Ein Dokumentenmanagementsystem zum **Archivieren, Verarbeiten, Durchsuchen und Verwalten von PDF-Dokumenten**.

Hochgeladene PDFs werden in einem Objektspeicher abgelegt, per **OCR** automatisch in Text umgewandelt und in **Elasticsearch** für die **Volltext- und Fuzzy-Suche** indexiert. Dokumente lassen sich über Metadaten, Tags und Sammlungen verwalten.

Projekt im Rahmen des **SWEN3 BIF5 Semesterprojekts**. Aktueller Stand: **Sprint 4** (Worker Services: OCR, Objektspeicher, Elasticsearch).

## Architektur

```
Frontend (Next.js) ──► Backend (Spring Boot) ──► RustFS (PDF-Dateien)
                            │   │
                            │   └──► PostgreSQL (Metadaten, objectKey)
                            ▼
                       ocr.queue (RabbitMQ)
                            ▼
                OCR-Worker (Python, Tesseract)
                  lädt PDF aus RustFS
                            ▼
                      index.queue (RabbitMQ)
                            ▼
               Indexing-Worker (Python) ──► Elasticsearch
                                                 ▲
Frontend-Suche ──► Backend /api/documents/search ┘
```

| Service | Technologie | Port | Aufgabe |
|---|---|---|---|
| `frontend` | Next.js | 3000 | Web-UI (Dashboard, Dokumente, Sammlungen, Upload, Suche) |
| `backend` | Spring Boot (Java 25) | 8081 | REST-API, Validierung, Speicherung, Suche |
| `postgres` | PostgreSQL 17 | 5432 | Metadaten (Name, Größe, Tags, `objectKey`, Sammlungen) |
| `rustfs` | RustFS (S3-kompatibel) | 9000 / 9001 | Ablage der PDFs (Bucket `documents`), Konsole auf 9001 |
| `rabbitmq` | RabbitMQ 4 | 5672 / 15672 | Queues zwischen Backend und Workern |
| `ocr-worker` | Python, Tesseract, Poppler | – | PDF laden, Text erkennen |
| `indexing-worker` | Python | – | Text in Elasticsearch schreiben |
| `elasticsearch` | Elasticsearch 8.15 | 9200 | Volltext- und Fuzzy-Suche |

## Ablauf eines Uploads

1. Das Frontend sendet das PDF an `POST /api/documents`.
2. Das Backend prüft die Datei (nur PDF, nicht leer) und legt sie in RustFS unter `documents/<uuid>.pdf` ab. Die Metadaten und der `objectKey` kommen in PostgreSQL. Schlägt das Speichern der Metadaten fehl, wird das PDF in RustFS wieder entfernt.
3. Das Backend sendet eine Nachricht an `ocr.queue`.
4. Der OCR-Worker lädt das PDF aus RustFS, wandelt die Seiten in Bilder um, erkennt den Text mit Tesseract (Deutsch + Englisch) und sendet ihn an `index.queue`.
5. Der Indexing-Worker schreibt den Text in den Elasticsearch-Index `documents` (Dokument-ID = ID aus PostgreSQL).
6. Die Suche im Frontend ruft `GET /api/documents/search?q=...` auf. Das Backend fragt Elasticsearch ab (Felder `text` und `fileName`, `fuzziness: AUTO`) und lädt die Treffer aus PostgreSQL.

## Queues und Nachrichtenformate

`ocr.queue` (Backend → OCR-Worker):

```json
{
  "documentId": 42,
  "fileName": "HelloWorld.pdf",
  "bucket": "documents",
  "objectKey": "documents/<uuid>.pdf"
}
```

`index.queue` (OCR-Worker → Indexing-Worker):

```json
{
  "documentId": 42,
  "fileName": "HelloWorld.pdf",
  "text": "Hello World ..."
}
```

Beide Queues sind `durable`. Fehlerhafte Nachrichten werden von den Workern verworfen (kein Requeue), damit die Queue nicht blockiert.

## REST-API

Dokumente (`/api/documents`):

| Methode | Pfad | Beschreibung |
|---|---|---|
| POST | `/api/documents` | PDF hochladen (`multipart/form-data`, Feld `file`) |
| GET | `/api/documents` | Alle Dokumente |
| GET | `/api/documents/{id}` | Ein Dokument |
| GET | `/api/documents/{id}/download` | PDF aus RustFS (inline, für Vorschau und Download) |
| GET | `/api/documents/search?q=...` | Volltext- und Fuzzy-Suche |
| PATCH | `/api/documents/{id}` | Name und/oder Tags ändern |
| DELETE | `/api/documents/{id}` | Löscht Metadaten, PDF in RustFS und den Suchindex-Eintrag |

Sammlungen (`/api/collections`): `POST`, `GET`, `GET /{id}`, `DELETE /{id}`, `PUT /{id}/documents/{documentId}`, `DELETE /{id}/documents/{documentId}`.

Fehler werden als HTTP-Problem-Responses geliefert. Probleme mit dem Objektspeicher ergeben HTTP 503.

## Starten

Voraussetzung: Docker Desktop.

```sh
docker compose up --build
```

Entwicklungsmodus (Frontend mit Hot Reload):

```sh
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

| Was | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8081 |
| RustFS-Konsole | http://localhost:9001 |
| RabbitMQ-Verwaltung | http://localhost:15672 (`guest` / `guest`) |
| Elasticsearch | http://localhost:9200 |

Die lokalen Zugangsdaten für RustFS sind `paperlessadmin` / `paperless-local-secret`. Sie lassen sich über die Umgebungsvariablen `STORAGE_ACCESS_KEY` und `STORAGE_SECRET_KEY` überschreiben (gilt für RustFS, Backend und OCR-Worker).

## Abnahmetest (Sprint 4)

1. `docker compose up --build` ausführen und warten, bis alle Container laufen.
2. `HelloWorld.pdf` im Frontend unter http://localhost:3000/upload hochladen.
3. Optional prüfen: `docker compose logs ocr-worker` zeigt `OCR done`, `docker compose logs indexing-worker` zeigt `Indexed documentId=...`.
4. Unter http://localhost:3000/documents im Suchfeld nach `Hello` suchen. `HelloWorld.pdf` erscheint in der Liste.

Direkt prüfen, was im Index liegt:

```sh
curl "http://localhost:9200/documents/_search?q=Hello"
```

## Hinweise

- PDFs, die vor Sprint 4 hochgeladen wurden, sind nicht im Suchindex, weil ihr OCR-Job nie lief. Sie müssen neu hochgeladen werden.
- Der Elasticsearch-Python-Client ist auf Version 8 festgelegt (`elasticsearch>=8,<9`), passend zum Server 8.15.
- Als Objektspeicher wird RustFS verwendet (S3-kompatibel) statt MinIO. Der Zugriff erfolgt über die S3-API mit Path-Style-Adressierung und der Region `us-east-1`.

## Tests

Unit-Tests des Backends laufen beim Docker-Build mit, lokal im Ordner `swen3-backend`:

```sh
./mvnw test
```

## Projektstruktur

```
swen3-backend/           Spring Boot REST-API
swen3-frontend/          Next.js Web-UI
swen3-ocr-worker-py/     Python OCR-Worker
swen3-indexing-worker/   Python Indexing-Worker
docker-compose.yml       Alle Services
docker-compose.dev.yml   Overrides für die Entwicklung
```

## Beteiligte

- Lars Hosnedl – deadstx
- Elena Dordevic – elenaDord
- Abdussalim Sen – salimsn
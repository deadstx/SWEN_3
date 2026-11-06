
## Sprint 4: RustFS storage

New PDF uploads are stored in RustFS. PostgreSQL stores metadata and an `objectKey`.
Existing PDFs in PostgreSQL are preserved, but are not migrated to RustFS automatically.
The `documents` bucket is created on the first upload. Object keys use UUIDs so identical
filenames do not overwrite each other. Storage errors return HTTP 503.

Start Docker Desktop, then run:

```sh
docker compose up --build
```

For this stage the frontend is at http://localhost:3000 and the RustFS console is at
http://localhost:9001. Local development credentials are `paperlessadmin` /
`paperless-local-secret`. Override them with `STORAGE_ACCESS_KEY` and `STORAGE_SECRET_KEY`
for both RustFS and the backend through Compose variable substitution.

Upload a PDF using the existing frontend. In the RustFS console, open the `documents`
bucket: the file should appear under `documents/<uuid>.pdf`. Download it from the console
and check that it matches the original. Deleting the document through the application
also deletes its RustFS object.

Worker contract: queue `ocr.queue`, JSON body:

```json
{
  "documentId": 42,
  "fileName": "HelloWorld.pdf",
  "bucket": "documents",
  "objectKey": "documents/<uuid>.pdf"
}
```

The future Python worker should use the S3 API at `http://rustfs:9000` inside Compose
(`http://localhost:9000` when running locally), region `us-east-1`, and path-style
addressing. It must fetch the PDF using `bucket` and `objectKey` from the message.
The existing Java OCR worker still only logs jobs; its message record was updated
to match the new fields. Replace that worker when implementing Python OCR.

Project Startup 

How to start project with Backend, Woker and Frontend:
docker compose build
docker compose up

Frist Start
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

Afterwards 

docker compose -f docker-compose.yml -f docker-compose.dev.yml up 



# Document Management System

A document management system for **archiving, processing, searching, and managing digital documents**.

Uploaded documents are automatically processed using **OCR**, indexed for **full-text and fuzzy search**, and summarized using **Generative AI**. Documents can be managed through their metadata and tags.

The project also includes asynchronous processing, batch processing, and additional use cases.

## Documentation

For detailed information, please refer to the following documentation:

- [SETUP.md](SETUP.md) – Setup and installation
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) – REST API documentation
- [ARCHITECTURE.md](ARCHITECTURE.md) – System architecture
- [USE_CASES.md](USE_CASES.md) – Use cases
- [TESTING.md](TESTING.md) – Testing and integration tests
- [BATCH_PROCESSING.md](BATCH_PROCESSING.md) – Batch processing

This project was developed as part of the **SWEN3 BIF5 Semester Project**.


**Contributors and Collaborators**

Lars Hosnedl - deadstx
Elena Dordevic - elenaDord
Abdussalim Sen - salimsn

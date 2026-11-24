# SWEN3 Sprint 5 · Gemini und Android

## Was geändert wurde und warum

| Datei oder Bereich | Änderung | Warum |
|---|---|---|
| `docker-compose.yml`, `.env.example`, `.gitignore` | GenAI-Worker angebunden, Gemini-Key und Modell konfigurierbar gemacht, PostgreSQL-Startprüfung ergänzt. Das Backend ist zusätzlich über Port 8080 erreichbar. | Der Worker ist Teil von `docker compose up`, der Upload erfüllt die vorgegebene Adresse und die vorhandene Weboberfläche kann weiter Port 8081 verwenden. |
| `swen3-ocr-worker-py/worker.py` | Schickt OCR-Text nach der Erkennung zu `index.queue` und `genai.queue`. Bei OCR-Fehler sendet er den Status `FAILED`. | Suche und Zusammenfassung laufen unabhängig voneinander. Der Upload bekommt auch bei OCR-Fehler einen sichtbaren Status. |
| `swen3-genai-worker/` | Konsumiert OCR-Text, ruft Gemini `generateContent` auf und publiziert Ergebnis oder Status in `genai.result.queue`. | Der externe API-Aufruf läuft im Hintergrund. Der Worker benötigt keinen Datenbankzugang. |
| `Messaging/`, `Service/DocumentService.java`, `application.properties` | OCR-Auftrag nach dem PostgreSQL-Commit senden und vom Broker bestätigen lassen. Der neue Listener nimmt GenAI-Ergebnisse entgegen und speichert sie. Queue-Namen, Dead-Letter-Queue und Wiederholungen sind konfiguriert. | Das Dokument existiert bereits, wenn ein Worker ein Ergebnis liefert. RabbitMQ kann eine unbestätigte Nachricht erneut zustellen. |
| `Entity/DocumentEntity.java`, `Repository/DocumentRepository.java`, `DTO/DocumentResponse.java`, `Mapper/DocumentMapper.java`, `Controller/DocumentController.java` | Speichert `summary` und `summaryStatus` in PostgreSQL und liefert beide im REST-JSON aus. `POST /` und `POST /api/documents` verwenden denselben Upload. | Web und Android lesen dieselben Daten. Das SQL-Update akzeptiert ein Ergebnis nur für dieselbe Dokument-ID, denselben Objektschlüssel und den Status `PENDING`. Doppelte oder veraltete Nachrichten überschreiben das Ergebnis nicht. |
| `swen3-frontend/src/` | Dokumentdetails zeigen Zusammenfassung und Status. Solange das Dokument verarbeitet wird, fragt das Detailfenster den Status alle drei Sekunden ab. Entfernt außerdem zwei ungenutzte Schrift-Downloads beim Build. | Die Zusammenfassung wird im bestehenden Web-UI sichtbar, sobald sie am Backend verfügbar ist. |
| `swen3-backend/.dockerignore`, `swen3-frontend/.dockerignore` | Schließt lokale Build-Ausgaben und Node-Module vom Docker-Kontext aus. | Die Container verwenden ihre eigene Build-Umgebung. |
| `swen3-android/` | Native Android-App in Java, mit Codex entwickelt. Installierbare Debug-APK, PDF-Dateiauswahl, Upload, Dokumentliste, Status und gespeicherte Zusammenfassung. | Die App verwendet dieselbe REST-API und funktioniert mit Emulator oder Smartphone im selben WLAN. |
| `swen3-backend/src/test/`, `pom.xml` | Testet den Versand nach Commit sowie Persistenz und Schutz gegen verspätete oder doppelte Ergebnisse mit einer H2-Testdatenbank. | Der Test prüft die neue JPQL-Schreiboperation und das Auslesen über den DTO-Mapper anhand echter Datenbankzeilen. |

## Abnahme unter Windows

Den Gemini-Key aus [Google AI Studio](https://aistudio.google.com/app/apikey) in eine lokale `.env` eintragen. Die Konfiguration nutzt standardmäßig `gemini-3.1-flash-lite`. Das Modell lässt sich bei Bedarf mit `GEMINI_MODEL` ändern.

```powershell
Copy-Item .env.example .env
notepad .env
docker compose up --build -d
docker compose ps
```

PDF über den im Aufgabenblatt verlangten Einstieg hochladen. Das Beispiel-PDF liegt unter `demo/HelloWorld.pdf`.

```powershell
$upload = curl.exe --fail --silent --show-error `
  -F "file=@demo/HelloWorld.pdf;type=application/pdf" http://localhost:8080/ |
  ConvertFrom-Json
$id = $upload.id
Invoke-RestMethod "http://localhost:8080/api/documents/$id" | ConvertTo-Json
docker compose logs ocr-worker genai-worker backend
docker compose exec -T postgres psql -U documentuser -d documentdb `
  -c "SELECT id, summary_status, summary FROM documents WHERE id = $id;"
```

Der POST liefert HTTP 201 und eine Dokument-ID. Direkt nach dem Upload steht der Status normalerweise auf `PENDING`. Nach erfolgreicher Verarbeitung liefert der GET-Aufruf `COMPLETED` und `summary` enthält Text. Dieselbe Zusammenfassung steht in PostgreSQL. Die Weboberfläche läuft unter [localhost:3000](http://localhost:3000). Unter „Dokumente“ kannst du das PDF öffnen und die Zusammenfassung sehen.

## Android

- **Emulator.** Installiere die APK aus dem ZIP, etwa per Drag-and-drop auf den laufenden Emulator. Die voreingestellte Serveradresse `http://10.0.2.2:8080` führt vom Android-Emulator zum Entwicklungsrechner.
- **Android-Smartphone.** Installiere die APK und verwende `http://<IPv4-Adresse-des-PCs>:8080`. PC und Smartphone müssen im selben WLAN sein, und Port 8080 muss vom Smartphone aus erreichbar sein.
- In der App „PDF auswählen“ drücken. Nach dem Upload zeigt „Dokumente aktualisieren“ den neuen Status und die fertige Zusammenfassung.
- Für den eigenen Build brauchst du Android SDK Platform 33, Build Tools 36.0.0 und ein JDK ab 17. Führe `.\swen3-android\build.ps1 -Sdk <SDK-Pfad> -Jdk <JDK-Pfad>` aus. Die signierte APK wird in `swen3-android/build/Paperless-SWEN3.apk` gespeichert.

## Fehlerverhalten

| Situation | Verhalten |
|---|---|
| PDF liefert keinen OCR-Text | `NO_TEXT`. Gemini wird übersprungen. |
| Rate Limit, Timeout, Netzwerkfehler oder vorübergehender Serverfehler | Bis zu drei Gemini-Aufrufe insgesamt mit Wartezeit zwischen den Aufrufen. |
| Fehlender Key, abgewiesene Anfrage, zu langer OCR-Text oder keine brauchbare Gemini-Antwort | `FAILED`. Die Worker-Logs enthalten Dokument-ID und Fehlergrund. |
| Backend kann ein Ergebnis vorübergehend nicht speichern | Der Listener wiederholt den Schreibvorgang. Nach ausgeschöpften Versuchen landet die Nachricht in `genai.result.queue.dead`. |
| OCR-Auftrag kann nach dem Upload nicht veröffentlicht werden | Der Upload bleibt gespeichert. Das Backend protokolliert den Fehler und setzt den Status auf `FAILED`. Zwischen Datenbank-Commit und Veröffentlichung bleibt bei einem Prozessabsturz ein Zeitfenster. |
| Dokument stammt aus einem früheren Sprint | Status und Summary können `null` sein. Die Verarbeitung startet bei neuen Uploads. |

Der Worker sendet OCR-Text an Google Gemini. Die Logs enthalten weder den API-Key noch den erkannten Text oder die Zusammenfassung. Für die lokale Demo erlaubt die Android-App HTTP zum Entwicklungsrechner.

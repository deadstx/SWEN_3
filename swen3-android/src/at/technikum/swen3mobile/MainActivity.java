package at.technikum.swen3mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int PICK_PDF = 1;
    private static final int INK = Color.rgb(21, 37, 48);
    private static final int MUTED = Color.rgb(67, 86, 97);
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private EditText server;
    private TextView status;
    private LinearLayout documents;
    private Button upload;
    private Button refresh;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(239, 245, 244));
        LinearLayout content = column();
        content.setPadding(dp(22), dp(24), dp(22), dp(20));
        scroll.addView(content);
        setContentView(scroll);

        TextView title = label("Paperless SWEN3", 30, INK);
        title.setTypeface(null, Typeface.BOLD);
        content.addView(title);
        content.addView(label("Serveradresse", 14, MUTED));
        server = new EditText(this);
        server.setSingleLine(true);
        server.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        server.setContentDescription("Serveradresse");
        server.setText(getPreferences(MODE_PRIVATE).getString("server", "http://10.0.2.2:8080"));
        content.addView(server);

        upload = new Button(this);
        upload.setText("PDF auswählen");
        upload.setOnClickListener(view -> {
            Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            picker.addCategory(Intent.CATEGORY_OPENABLE);
            picker.setType("application/pdf");
            startActivityForResult(picker, PICK_PDF);
        });
        content.addView(upload);
        refresh = new Button(this);
        refresh.setText("Dokumente aktualisieren");
        refresh.setOnClickListener(view -> load(null));
        content.addView(refresh);

        status = label("", 15, MUTED);
        content.addView(status);
        documents = column();
        content.addView(documents);
        content.setFocusableInTouchMode(true);
        content.requestFocus();
        load(null);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == PICK_PDF && result == RESULT_OK && data != null && data.getData() != null) {
            load(data.getData());
        }
    }

    private void load(Uri pdf) {
        String base = server.getText().toString().trim().replaceAll("/+$", "");
        getPreferences(MODE_PRIVATE).edit().putString("server", base).apply();
        upload.setEnabled(false);
        refresh.setEnabled(false);
        status.setText(pdf == null ? "Dokumente werden geladen …" : "PDF wird hochgeladen …");
        io.execute(() -> {
            boolean uploaded = false;
            try {
                URL url = new URL(base + "/api/documents");
                if ((!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol()))
                        || url.getHost().isEmpty()) {
                    throw new IOException("Gib eine Serveradresse mit http:// oder https:// ein.");
                }
                if (pdf != null) {
                    uploadPdf(url, pdf);
                    uploaded = true;
                }
                JSONArray result = fetchDocuments(url);
                finishLoad(result, uploaded
                        ? "PDF hochgeladen. Aktualisiere die Liste, um die Zusammenfassung abzurufen."
                        : result.length() + " Dokument(e)");
            } catch (Exception error) {
                finishLoad(null, (uploaded ? "PDF hochgeladen. Liste konnte nicht geladen werden. "
                        : "Anfrage fehlgeschlagen. ") + error.getMessage());
            }
        });
    }

    private void finishLoad(JSONArray result, String message) {
        runOnUiThread(() -> {
            if (isDestroyed()) {
                return;
            }
            upload.setEnabled(true);
            refresh.setEnabled(true);
            status.setText(message);
            if (result != null) {
                render(result);
            }
        });
    }

    private void render(JSONArray result) {
        documents.removeAllViews();
        for (int index = result.length() - 1; index >= 0; index--) {
            JSONObject document = result.optJSONObject(index);
            if (document == null) {
                continue;
            }
            LinearLayout card = column();
            card.setPadding(dp(16), dp(12), dp(16), dp(12));
            card.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.topMargin = dp(12);
            documents.addView(card, layout);
            TextView name = label(document.optString("name"), 19, INK);
            name.setTypeface(null, Typeface.BOLD);
            card.addView(name);
            card.addView(label(summaryLabel(document), 16, MUTED));
        }
    }

    private String summaryLabel(JSONObject document) {
        switch (document.optString("summaryStatus")) {
            case "COMPLETED":
                return document.optString("summary");
            case "PENDING":
                return "Zusammenfassung wird erstellt.";
            case "NO_TEXT":
                return "Im PDF wurde kein Text erkannt.";
            case "FAILED":
                return "Zusammenfassung konnte nicht erstellt werden.";
            default:
                return "Für dieses Dokument liegt keine Zusammenfassung vor.";
        }
    }

    private JSONArray fetchDocuments(URL url) throws Exception {
        HttpURLConnection connection = connect(url);
        try {
            checkResponse(connection);
            try (InputStream stream = connection.getInputStream();
                 ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                copy(stream, bytes);
                return new JSONArray(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    private void uploadPdf(URL url, Uri uri) throws IOException {
        HttpURLConnection connection = connect(url);
        String boundary = "paperless-" + UUID.randomUUID();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(8192);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        try {
            String name = fileName(uri);
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) {
                    throw new IOException("Die PDF-Datei konnte nicht geöffnet werden.");
                }
                try (OutputStream out = connection.getOutputStream()) {
                    write(out, "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\""
                            + name + "\"\r\nContent-Type: application/pdf\r\n\r\n");
                    copy(in, out);
                    write(out, "\r\n--" + boundary + "--\r\n");
                }
            }
            checkResponse(connection);
        } finally {
            connection.disconnect();
        }
    }

    private String fileName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getString(0).replaceAll("[\\r\\n\"\\\\/]", "_");
            }
        }
        return "Dokument.pdf";
    }

    private HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);
        return connection;
    }

    private void checkResponse(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Server meldet HTTP " + code + ".");
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }

    private void write(OutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView label(String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, dp(5), 0, dp(5));
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        io.shutdownNow();
        super.onDestroy();
    }
}

/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.download;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.util.logging.Log;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class DownloadUtils {
    private static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
        @Override
        public String read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            /* coerce booleans to strings for backwards compatibility */
            if (peek == JsonToken.BOOLEAN) {
                return Boolean.toString(in.nextBoolean());
            }
            return in.nextString();
        }

        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }
    };

    // A GSO parser that prints good looking output, and treats empty strings as nulls
    public static final Gson GSON = new GsonBuilder()
        .setLenient()
        .setPrettyPrinting()
        .registerTypeAdapter(String.class, new TypeAdapter<String>() {
            @Override
            public void write(final JsonWriter out, final String value) throws IOException {
                if (value == null || value.isEmpty())
                    out.nullValue();
                else
                    STRING.write(out, value);
            }

            @Override
            public String read(final JsonReader in) throws IOException {
                String value = STRING.read(in);
                return value != null && value.isEmpty() ? null : value; // Read empty strings as null
            }
        })
        .create();

    private static final int TIMEOUT = 5 * 1000;
    private static final int MAX_REDIRECTS = 3;

    private static URLConnection getConnection(String address) throws IOException {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        URL url = uri.toURL();

        List<String> redirections = new ArrayList<>();
        URLConnection con;
        for (int redirects = 0; ; redirects++) {
            con = url.openConnection();
            con.setConnectTimeout(TIMEOUT);
            con.setReadTimeout(TIMEOUT);

            if (!(con instanceof HttpURLConnection)) break;

            HttpURLConnection hcon = (HttpURLConnection) con;
            hcon.setRequestProperty("User-Agent", "MinecraftMaven");
            hcon.setRequestProperty("accept", "application/json");
            hcon.setInstanceFollowRedirects(false);

            int res = hcon.getResponseCode();
            if (res == HttpURLConnection.HTTP_MOVED_PERM || res == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = hcon.getHeaderField("Location");
                redirections.add(location);
                hcon.disconnect();

                if (redirects == MAX_REDIRECTS - 1) {
                    throw new IOException(String.format(
                        "Too many redirects: %s -- redirections: [%s]",
                        address, String.join(", ", redirections)
                    ));
                } else {
                    Log.debug("Following redirect: " + location);
                    uri = uri.resolve(location);
                    url = uri.toURL();
                }
            } else if (res == 404) {
                throw new FileNotFoundException("Returned 404: " + address);
            } else {
                break;
            }
        }

        return con;
    }

    /**
     * Downloads a string from the given URL, effectively acting as {@code curl}.
     *
     * @param url The URL to download from
     * @return The downloaded string
     * @throws IOException If the download failed
     */
    public static String downloadString(String url) throws IOException {
        URLConnection connection = getConnection(url);
        try (InputStream stream = connection.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = stream.read(buf)) > 0) {
                out.write(buf, 0, n);
            }

            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Downloads a string from the given URL, effectively acting as {@code curl}.
     * <p>Returns {@code null} on failure.</p>
     *
     * @param url The URL to download from
     * @return The downloaded string, or {@code null} if the download failed
     */
    public static @Nullable String tryDownloadString(boolean silent, String url) {
        try {
            return downloadString(url);
        } catch (IOException e) {
            if (!silent) {
                Log.warn("Failed to download " + url);
                e.printStackTrace(Log.WARN);
            }

            return null;
        }
    }

    /**
     * Downloads a file from the given URL into the target file, effectively acting as {@code wget}.
     *
     * @param target The file to download to
     * @param url    The URL to download from
     * @throws IOException If the download failed
     */
    public static void downloadFile(File target, String url) throws IOException {
        downloadFile(false, target, url);
    }

    /**
     * Downloads a file from the given URL into the target file, effectively acting as {@code wget}.
     *
     * @param silent If no log messages should be sent
     * @param target The file to download to
     * @param url    The URL to download from
     * @throws IOException If the download failed
     */
    public static void downloadFile(boolean silent, File target, String url) throws IOException {
        if (!silent) Log.quiet("Downloading " + url);

        URLConnection connection = getConnection(url);
        Files.createDirectories(target.toPath().getParent());
        Files.copy(connection.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Attempts to download a file from the given URL into the target file, effectively acting as {@code wget}.
     *
     * @param target The file to download to
     * @param url    The URL to download from
     * @return {@code true} if the download was successful
     */
    public static boolean tryDownloadFile(boolean silent, File target, String url) {
        try {
            downloadFile(silent, target, url);
            return true;
        } catch (IOException e) {
            if (!silent) {
                Log.warn("Failed to download " + url);
                e.printStackTrace(Log.WARN);
            }

            return false;
        }
    }
}

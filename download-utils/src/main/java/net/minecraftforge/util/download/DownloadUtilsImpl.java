/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.download;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

final class DownloadUtilsImpl {
    private DownloadUtilsImpl() { }

    private static final int TIMEOUT = 5 * 1000;
    private static final int MAX_REDIRECTS = 3;

    private static URLConnection openConnection(URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);

        if (con instanceof HttpURLConnection) {
            HttpURLConnection hcon = (HttpURLConnection) con;
            hcon.setRequestProperty("User-Agent", "MinecraftForge-Utils");
            hcon.setRequestProperty("Accept", "application/json");
            hcon.setInstanceFollowRedirects(false);
        }

        return con;
    }

    static InputStream connect(String address) throws IOException {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        URL url;
        List<String> redirections = new ArrayList<>();
        URLConnection con;
        for (int redirects = 0; ; redirects++) {
            url = uri.toURL();
            con = openConnection(url);

            if (!(con instanceof HttpURLConnection)) break;
            HttpURLConnection hcon = (HttpURLConnection) con;

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
                    uri = uri.resolve(location);
                }
            } else if (res == 404) {
                throw new FileNotFoundException("Returned 404: " + address);
            } else {
                break;
            }
        }

        final URLConnection connection = con;
        return connection.getInputStream();
    }

    static byte[] readInputStream(InputStream stream) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = stream.read(buf)) > 0) {
                out.write(buf, 0, n);
            }

            return out.toByteArray();
        }
    }
}

/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.download;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;

final class DownloadUtilsImpl {
    private DownloadUtilsImpl() { }

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_REDIRECTS = 3;

    private static final HttpClient CLIENT = HttpClient
        .newBuilder()
        .connectTimeout(TIMEOUT)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    private static final HttpRequest.Builder REQUEST_BUILDER = HttpRequest
        .newBuilder()
        .timeout(TIMEOUT)
        .header("User-Agent", "MinecraftForge-Utils")
        .header("Accept", "application/json");

    static InputStream connect(String address) throws IOException, InterruptedException {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        // if not http, then try a URLConnection. we might be trying to make a file or jar connection.
        // see jdk.internal.net.http.HttpRequestBuilderImpl#checkUri
        var scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))) {
            return uri.toURL().openStream();
        }

        var redirections = new ArrayList<String>();
        HttpResponse<InputStream> con;
        for (int redirects = 0; ; redirects++) {
            con = CLIENT.send(
                REQUEST_BUILDER.uri(uri).build(),
                info -> HttpResponse.BodySubscribers.ofInputStream()
            );

            int res = con.statusCode();
            if (res == HttpURLConnection.HTTP_MOVED_PERM || res == HttpURLConnection.HTTP_MOVED_TEMP) {
                var header = con.headers().firstValue("Location");
                if (header.isEmpty())
                    throw new IOException(String.format(
                        "No location header found in redirect response: %s -- previous redirections: [%s]",
                        uri, String.join(", ", redirections)
                    ));

                var location = header.get();
                redirections.add(location);

                if (redirects == MAX_REDIRECTS - 1) {
                    throw new IOException(String.format(
                        "Too many redirects: %s -- redirections: [%s]",
                        address, String.join(", ", redirections)
                    ));
                } else {
                    uri = uri.resolve(location);
                }
            } else if (res == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new FileNotFoundException("Returned 404: " + address);
            } else {
                break;
            }
        }

        return con.body();
    }

    static byte[] readInputStream(InputStream stream) throws IOException {
        return stream.readAllBytes();
    }
}

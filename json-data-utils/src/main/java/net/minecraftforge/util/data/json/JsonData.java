/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// TODO [MCMaven][Documentation] Document
public class JsonData {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static LauncherManifest launcherManifest(File file) {
        return fromJson(file, LauncherManifest.class);
    }
    public static LauncherManifest launcherManifest(InputStream stream) {
        return fromJson(stream, LauncherManifest.class);
    }
    public static LauncherManifest launcherManifest(byte[] data) {
        return fromJson(data, LauncherManifest.class);
    }

    public static MinecraftVersion minecraftVersion(File file) {
        return fromJson(file, MinecraftVersion.class);
    }
    public static MinecraftVersion minecraftVersion(InputStream stream) {
        return fromJson(stream, MinecraftVersion.class);
    }
    public static MinecraftVersion minecraftVersion(byte[] data) {
        return fromJson(data, MinecraftVersion.class);
    }

    private static <T> T fromJson(File file, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return fromJson(stream, classOfT);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }
    private static <T> T fromJson(byte[] data, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        return fromJson(new ByteArrayInputStream(data), classOfT);
    }
    private static <T> T fromJson(InputStream stream, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        return GSON.fromJson(new InputStreamReader(stream), classOfT);
    }
    private static <T> T fromJson(String data, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        return GSON.fromJson(data, classOfT);
    }

    public static String toJson(Object src) {
        return GSON.toJson(src);
    }
    public static void toJson(Object src, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(src, writer);
        }
    }
}

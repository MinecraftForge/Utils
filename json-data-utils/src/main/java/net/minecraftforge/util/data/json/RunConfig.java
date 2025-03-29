/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.data.json;

import java.util.List;
import java.util.Map;

public class RunConfig {
    public String name;
    public String main;
    public List<String> parents;
    public List<String> args;
    public List<String> jvmArgs;
    public boolean client;
    public boolean buildAllProjects;
    public Map<String, String> env;
    public Map<String, String> props;
}

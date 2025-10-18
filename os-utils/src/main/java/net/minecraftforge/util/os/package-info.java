/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
/// OS Utils is a small, simple library designed to be an agreement between libraries (i.e. Mavenizer and ForgeGradle 7)
/// on how to compute the local machine's operating system information.
///
/// The consumer is responsible for [checking the return value][org.jetbrains.annotations.CheckReturnValue] of all
/// methods in this library, as there are no nulls and unknowns (i.e. [net.minecraftforge.util.os.OS#UNKNOWN]) must be
/// handled accordingly.
@CheckReturnValue
package net.minecraftforge.util.os;

import org.jetbrains.annotations.CheckReturnValue;

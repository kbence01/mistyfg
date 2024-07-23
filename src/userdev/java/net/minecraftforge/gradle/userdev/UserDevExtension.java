/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.gradle.userdev;

import net.minecraftforge.gradle.common.util.MinecraftExtension;

import org.gradle.api.Project;

import javax.annotation.Nonnull;

public abstract class UserDevExtension extends MinecraftExtension {
    public static final String EXTENSION_NAME = "minecraft";

    private boolean reobf = true;

    public UserDevExtension(@Nonnull final Project project) {
        super(project);
    }

    public void setReobf(boolean value) {
    	this.reobf = false;
    }

    public boolean getReobf() {
    	return this.reobf;
    }
}

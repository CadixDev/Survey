/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.config;

import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

/**
 * Requirements for modifiers of a field.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public enum ModifierRequirement {

    PUBLIC("public") {
        @Override
        public boolean test(final int access) {
            return Modifier.isPublic(access);
        }
    },
    PRIVATE("private") {
        @Override
        public boolean test(final int access) {
            return Modifier.isPrivate(access);
        }
    },
    FINAL("final") {
        @Override
        public boolean test(final int access) {
            return Modifier.isFinal(access);
        }
    },
    STATIC("static") {
        @Override
        public boolean test(final int access) {
            return Modifier.isStatic(access);
        }
    },
    SYNTHETIC("synthetic") {
        @Override
        public boolean test(final int access) {
            return (access & Opcodes.ACC_SYNTHETIC) != 0;
        }
    },
    ;

    private final String id;

    ModifierRequirement(final String id) {
        this.id = id;
    }

    public abstract boolean test(final int access);

    public static ModifierRequirement byId(final String id) {
        return Arrays.stream(values())
                .filter(req -> Objects.equals(req.id, id))
                .findFirst().orElse(null);
    }

}

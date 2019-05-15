/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.intermediary;

import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.survey.context.SurveyContext;

/**
 * The class intermediary mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class ClassIntermediaryMapper extends AbstractIntermediaryMapper<ClassIntermediaryMapper.Config> {

    private int count = 0;

    public ClassIntermediaryMapper(final SurveyContext ctx, final ClassIntermediaryMapper.Config configuration) {
        super(ctx, configuration);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        final ClassMapping<?, ?> klass = this.ctx().mappings().getOrCreateClassMapping(name);
        if (!klass.hasDeobfuscatedName()) {
            if (name.contains("$")) {
                final String innerName = name.substring(name.lastIndexOf('$') + 1);
                klass.setDeobfuscatedName(this.getConfiguration().getMemberName(++this.count, innerName));
            }
            else {
                klass.setDeobfuscatedName(this.getConfiguration().getPackageName() +
                        this.getConfiguration().getMemberName(++this.count, name));
            }
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * The class intermediary mapper configuration.
     */
    public static class Config extends AbstractIntermediaryMapper.Config {

        private final String format;
        private final String packageName;

        public Config(final String format, final String packageName) {
            this.format = format;
            this.packageName = packageName;
        }

        @Override
        public String getFormat() {
            return this.format;
        }

        public String getPackageName() {
            return this.packageName;
        }

    }

}

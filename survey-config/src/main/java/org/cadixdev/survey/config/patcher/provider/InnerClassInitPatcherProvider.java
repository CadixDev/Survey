/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.patcher.provider;

import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.patcher.InnerClassInitPatcher;

public class InnerClassInitPatcherProvider extends SimplePatcherProvider<InnerClassInitPatcher, Void> {

    private static final String ID = "inner_class_init_adder";

    public InnerClassInitPatcherProvider() {
        super(ID, Void.class, null);
    }

    @Override
    public InnerClassInitPatcher create(final SurveyContext ctx, final Void config) {
        return new InnerClassInitPatcher(ctx);
    }

}

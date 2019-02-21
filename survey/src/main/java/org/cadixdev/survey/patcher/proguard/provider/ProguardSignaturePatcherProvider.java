/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher.proguard.provider;

import org.cadixdev.survey.SurveyContext;
import org.cadixdev.survey.patcher.proguard.ProguardSignaturePatcher;
import org.cadixdev.survey.patcher.provider.SimplePatcherProvider;

/**
 * The patcher provider for the Proguard signature patcher.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class ProguardSignaturePatcherProvider extends SimplePatcherProvider<ProguardSignaturePatcher, Void> {

    private static final String ID = "proguard_signature";

    public ProguardSignaturePatcherProvider() {
        super(ID, Void.class, null);
    }

    @Override
    public ProguardSignaturePatcher create(final SurveyContext ctx, final Void config) {
        return new ProguardSignaturePatcher(ctx);
    }

}

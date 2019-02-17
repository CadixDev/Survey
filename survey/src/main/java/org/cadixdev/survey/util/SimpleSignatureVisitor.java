/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.util;

import org.objectweb.asm.signature.SignatureVisitor;

public class SimpleSignatureVisitor extends SignatureVisitor {

    private final SignatureVisitor sv;

    public SimpleSignatureVisitor(final int api, final SignatureVisitor sv) {
        super(api);
        this.sv = sv;
    }

    @Override
    public void visitFormalTypeParameter(final String name) {
        if (this.sv != null) {
            this.sv.visitFormalTypeParameter(name);
        }
    }

    @Override
    public SignatureVisitor visitClassBound() {
        if (this.sv != null) {
            this.sv.visitClassBound();
        }
        return this;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        if (this.sv != null) {
            this.sv.visitInterfaceBound();
        }
        return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        if (this.sv != null) {
            this.sv.visitSuperclass();
        }
        return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
        if (this.sv != null) {
            this.sv.visitInterface();
        }
        return this;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        if (this.sv != null) {
            this.sv.visitParameterType();
        }
        return this;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        if (this.sv != null) {
            this.sv.visitReturnType();
        }
        return this;
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        if (this.sv != null) {
            this.sv.visitExceptionType();
        }
        return this;
    }

    @Override
    public void visitBaseType(final char descriptor) {
        if (this.sv != null) {
            this.sv.visitBaseType(descriptor);
        }
    }

    @Override
    public void visitTypeVariable(final String name) {
        if (this.sv != null) {
            this.sv.visitTypeVariable(name);
        }
    }

    @Override
    public SignatureVisitor visitArrayType() {
        if (this.sv != null) {
            this.sv.visitArrayType();
        }
        return this;
    }

    @Override
    public void visitClassType(final String name) {
        if (this.sv != null) {
            this.sv.visitClassType(name);
        }
    }

    @Override
    public void visitInnerClassType(final String name) {
        if (this.sv != null) {
            this.sv.visitInnerClassType(name);
        }
    }

    @Override
    public void visitTypeArgument() {
        if (this.sv != null) {
            this.sv.visitTypeArgument();
        }
    }

    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        if (this.sv != null) {
            this.sv.visitTypeArgument(wildcard);
        }
        return this;
    }

    @Override
    public void visitEnd() {
        if (this.sv != null) {
            this.sv.visitEnd();
        }
    }

}

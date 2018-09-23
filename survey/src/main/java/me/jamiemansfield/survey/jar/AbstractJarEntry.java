/*
 * Copyright (c) 2018, Jamie Mansfield <https://jamiemansfield.me/>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.jamiemansfield.survey.jar;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Represents an entry within a jar file.
 *
 * @author Jamie Mansfield
 */
public abstract class AbstractJarEntry {

    protected final String name;
    protected final byte[] contents;
    private String packageName;
    private String simpleName;

    protected AbstractJarEntry(final String name, final byte[] contents) {
        this.name = name;
        this.contents = contents;
    }

    public final String getName() {
        return this.name;
    }

    public final String getPackage() {
        if (this.packageName != null) return this.packageName;
        final int index = this.name.lastIndexOf('/');
        if (index == -1) return this.packageName = "";
        return this.packageName = this.name.substring(0, index);
    }

    public final String getSimpleName() {
        if (this.simpleName != null) return this.simpleName;
        final int packageLength = this.getPackage().isEmpty() ? -1 : this.getPackage().length();
        final int extensionLength = this.getExtension().isEmpty() ? -1 : this.getExtension().length();
        return this.simpleName = this.name.substring(
                packageLength + 1,
                this.name.length() - (extensionLength + 1)
        );
    }

    public abstract String getExtension();

    public final byte[] getContents() {
        return this.contents;
    }

    public final void write(final JarOutputStream jos) throws IOException {
        jos.putNextEntry(new JarEntry(this.name));
        jos.write(this.contents);
    }

    public abstract AbstractJarEntry accept(final JarEntryTransformer vistor);

}

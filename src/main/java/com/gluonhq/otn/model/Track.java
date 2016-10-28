/**
 * Copyright (c) 2016, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 *    or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.otn.model;

import com.gluonhq.otn.util.OTNBundle;
import java.util.Locale;
import javafx.css.PseudoClass;

public enum Track {
    CORE_PLATFORM       ("OTN.TRACK.CORE_JAVA_PLATFORM"),
    EMERGING_LANGUAGES  ("OTN.TRACK.EMERGING_LANGUAGES"),
    CLOUD_SERVER        ("OTN.TRACK.JAVA_CLOUD_AND_SERVER-SIDE_DEVELOPMENT"),
    BIGDATA_CLOUD       ("OTN.TRACK.JAVA_BIG_DATA_AND_THE_CLOUD"),
    DEVICES             ("OTN.TRACK.JAVA_AND_DEVICES"),
    CLIENT              ("OTN.TRACK.JAVA_CLIENTS_AND_USER_INTERFACES"),
    DEVTOOLS            ("OTN.TRACK.JAVA_DEVELOPMENT_TOOLS"),
    DEVOPS_METHODOLOGIES("OTN.TRACK.JAVA_DEVOPS_AND_METHODOLOGIES"),
    UNKNOWN             ("");

    private final String title;
    private final String style;
    private final PseudoClass pseudoClass;

    Track(String title) {
        this.title = title.isEmpty() ? "" : OTNBundle.getString(title);
        this.style = "track-" + name().toLowerCase(Locale.ROOT).replace('_','-');
        this.pseudoClass = PseudoClass.getPseudoClass(style);
    }

    public String getTitle() {
        return title;
    }

    public String getStyle() {
        return style;
    }

    public PseudoClass getPseudoClass() {
        return pseudoClass;
    }
}


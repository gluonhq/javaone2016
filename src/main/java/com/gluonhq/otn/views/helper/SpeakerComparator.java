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
package com.gluonhq.otn.views.helper;

import com.gluonhq.otn.model.Speaker;
import static com.gluonhq.otn.util.OTNLogging.LOGGING_ENABLED;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SpeakerComparator implements Comparator<Speaker> {
    
    private static final Logger LOG = Logger.getLogger(SpeakerComparator.class.getName());

    @Override
    public int compare(Speaker speaker1, Speaker speaker2) {
        // First criterium: sort by first letter of name (Headers)
        if (speaker1.getFullName() == null) {
            if (LOGGING_ENABLED) {
                LOG.log(Level.WARNING, "Speaker 1 has no Full Name: " + speaker1.getUuid());
            }
        } else if (speaker2.getFullName() == null) {
            if (LOGGING_ENABLED) {
                LOG.log(Level.WARNING, "Speaker 2 has no Full Name: " + speaker2.getUuid());
            }
        }
        if (speaker1.getFullName().substring(0, 1).toLowerCase()
                .equals(speaker2.getFullName().substring(0, 1).toLowerCase())) {
            // Second criterium: sort by name
            if (speaker1.getFullName().equals(speaker2.getFullName())) {
                // Third criterium: by Uuid
                return speaker1.getUuid().compareTo(speaker2.getUuid());
            }
            return speaker1.getFullName().compareTo(speaker2.getFullName());
        }
        return speaker1.getFullName().substring(0, 1).toLowerCase()
                .compareTo(speaker2.getFullName().substring(0, 1).toLowerCase());
    }
    
}

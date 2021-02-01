/*
 * Copyright (c) 2020 mhunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.communicate.fhirbridge.core.matrxi2fhir.common;

import net.fhirfactory.pegacorn.referencevalues.PegacornSystemReference;
import java.util.Date;
import javax.enterprise.context.ApplicationScoped;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MatrixAttribute2FHIRIdentifierBuilders {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixAttribute2FHIRIdentifierBuilders.class);

    PegacornSystemReference pegacornSystemReference = new PegacornSystemReference();

    public Identifier buildFHIRPractitionerIdentifierFromMatrixUserID(String userID) {
        LOG.debug("buildFHIRPractitionerIdentifierFromMatrixUserID(): Entry, userID --> " + userID);
        if ((userID == null) || userID.isEmpty()) {
            LOG.debug("buildFHIRPractitionerIdentifierFromMatrixUserID(): Exit, userID is empty or null");
            return (null);
        }
        LOG.trace("buildFHIRPractitionerIdentifierFromMatrixUserID(): not valid Identifier found, creating one");
        // Create an empty FHIR::Identifier element
        Identifier localSenderIdentifier = new Identifier();
        // Set the FHIR::Identifier.Use to "TEMP" (this id is not guaranteed)
        localSenderIdentifier.setUse(Identifier.IdentifierUse.TEMP);
        // Set the FHIR::Identifier.System to Pegacorn (it's our ID we're creating)
        localSenderIdentifier.setSystem(pegacornSystemReference.getDefaultIdentifierSystemForCommunicateGroupServer());
        // Set the FHIR::Identifier.Value to the "sender" from the RoomServer system
        localSenderIdentifier.setValue(userID);
        LOG.debug("buildFHIRPractitionerIdentifierFromMatrixUserID(): Exit, create Identifier --> " + localSenderIdentifier.toString());
        return (localSenderIdentifier);
    }

    public Identifier buildFHIRGroupIdentifierFromMatrixRoomID(String roomID, Long creationTime) {
        LOG.debug("buildFHIRGroupIdentifierFromMatrixRoomID(): Entry, roomID --> {}, creationTime --> {}", roomID, creationTime);
        if ((roomID == null) || roomID.isEmpty()) {
            LOG.debug("buildFHIRPractitionerIdentifierFromMatrixUserID(): Exit, roomID is empty or null");
            return (null);
        }
        // Create an empty FHIR::Identifier element
        Identifier localGroupIdentifier = new Identifier();
        // Set the FHIR::Identifier.Use to "TEMP" (this id is not guaranteed)
        localGroupIdentifier.setUse(Identifier.IdentifierUse.TEMP);
        // Set the FHIR::Identifier.System to Pegacorn (it's our ID we're creating)
        localGroupIdentifier.setSystem(pegacornSystemReference.getDefaultIdentifierSystemForCommunicateGroupServer());
        // Set the FHIR::Identifier.Value to the "room id" from the RoomServer system
        localGroupIdentifier.setValue(roomID);
        // Create a FHIR::Period as a container for the valid message start/end times
        Period lEventIDPeriod = new Period();
        // Set the FHIR::Period.start value to the time the message was created/sent
        if (creationTime > 0) {
            lEventIDPeriod.setStart(new Date(creationTime));
        } else {
            lEventIDPeriod.setStart(new Date());
        }
        // Set the FHIR::Identifier.period to created FHIR::Period (our messages have
        // not expire point)
        localGroupIdentifier.setPeriod(lEventIDPeriod);
        return (localGroupIdentifier);
    }

}

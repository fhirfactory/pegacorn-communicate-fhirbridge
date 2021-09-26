/*
 * Copyright (c) 2020 Mark A. Hunter
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
package net.fhirfactory.pegacorn.communicate.iris.fhirbridge.workshops.transform.resources.group.transformers;

import net.fhirfactory.pegacorn.common.model.dates.EffectivePeriod;
import net.fhirfactory.pegacorn.internals.fhir.r4.codesystems.PegacornIdentifierCodeEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.codesystems.PegacornIdentifierCodeSystemFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.identifier.PegacornIdentifierFactory;
import net.fhirfactory.pegacorn.referencevalues.PegacornSystemReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author ACT Health
 */
@ApplicationScoped
public class CommunicateRoomIDToFHIRGroupIdentifierTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(CommunicateRoomIDToFHIRGroupIdentifierTransformer.class);

    @Inject
    private PegacornIdentifierCodeSystemFactory pegacornIdentifierCodeSystemFactory;

    @Inject
    private PegacornIdentifierFactory pegacornIdentifierFactory;

    @Inject
    PegacornSystemReference pegacornSystemReference;


    /**
     * The method takes a roomID (a String) and the period (an EffectivePeriod) and creates an associated
     * FHIR::Reference for a FHIR::Group.
     *
     * @param roomID The Communicate/Synapse/Matrix room_id value
     * @param period The period (EffectivePeriod) that the room has existed
     * @return A FHIR::Reference (to a FHIR::Group)
     */
    public Reference transformRoomIDToGroupReference(String roomID, EffectivePeriod period) {
        LOG.debug(".transformRoomIDToGroupReference(): Entry, roomID->{}, period->{}", roomID, period);
        Identifier identifier = transformRoomIDToGroupIdentifier(roomID, period);
        Reference reference = new Reference();
        reference.setIdentifier(identifier);
        reference.setType(ResourceType.Group.name());
        reference.setDisplay("Group{"+identifier.getValue()+"}");
        LOG.debug(".transformRoomIDToGroupReference(): Exit, reference->{}", reference);
        return (reference);
    }

    /**
     * This method takes the roomID (a String) and the period (an EffectivePeriod) and creates an associated
     * FHIR::Identifier element.
     *
     * @param roomID The Communicate/Synapse/Matrix room_id value
     * @param period The period (EffectivePeriod) that the room has existed
     * @return A FHIR::Identifier
     */
    public Identifier transformRoomIDToGroupIdentifier(String roomID, EffectivePeriod period) {
        LOG.debug(".transformRoomIDToGroupIdentifier(): Entry, roomID->{}, period->{}", roomID, period);
        Period fhirPeriod = period.getPeriod();
        PegacornIdentifierCodeEnum identifierType = PegacornIdentifierCodeEnum.IDENTIFIER_CODE_COMMUNICATE_ROOM_ID;
        Identifier groupIdentifier = pegacornIdentifierFactory.newIdentifier(identifierType, roomID, fhirPeriod );
        LOG.debug(".transformRoomIDToGroupIdentifier(): Exit, identifier->{}", groupIdentifier);
        return (groupIdentifier);
    }
}

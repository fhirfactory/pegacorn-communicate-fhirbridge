/*
 * The MIT License
 *
 * Copyright 2020 Mark A. Hunter (ACT Health).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.fhirfactory.pegacorn.communicate.iris.fhirbridge.workshops.transform.resources.group.transformers;

import net.fhirfactory.pegacorn.internals.communicate.entities.rooms.datatypes.CommunicateRoomMember;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.extensions.GroupMemberTypeExtensionEnricher;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.practitioner.factories.PractitionerResourceHelpers;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RoomMemberToGroupMemberTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(RoomMemberToGroupMemberTransformer.class);

    @Inject
    private PractitionerResourceHelpers practitionerResourceHelpers;

    @Inject
    private GroupMemberTypeExtensionEnricher extensionHelper;

    public Group.GroupMemberComponent transformGroupMembership(CommunicateRoomMember roomMember) {
        LOG.debug(".newGeneralGroupMember(): Entry, roomMember->{}", roomMember);
        if (roomMember == null) {
            LOG.debug(".newGeneralGroupMember(): Exit, roomMember is null, returning null");
            return (null);
        }
        Group.GroupMemberComponent member = new Group.GroupMemberComponent();
        Reference practitionerReference = practitionerResourceHelpers.buildPractitionerReferenceUsingEmail(roomMember.getMember().getIdentifier().getValue());
        member.setEntity(practitionerReference);
        Period activePeriod = new Period();
        if (roomMember.getActivePeriod().getEffectiveStartDate() != null) {
            activePeriod.setStart(roomMember.getActivePeriod().getEffectiveStartDate());
        }
        if (roomMember.getActivePeriod().getEffectiveEndDate() != null) {
            activePeriod.setEnd(roomMember.getActivePeriod().getEffectiveEndDate());
        }
        member.setPeriod(activePeriod);
        if (roomMember.getActivePeriod().isEffectiveNow()) {
            member.setInactive(false);
        } else {
            member.setInactive(true);
        }
        extensionHelper.injectMemberType(member, roomMember.getMemberType());
        LOG.debug(".newPractitionerRoleFulfillmentGroupMember(): Exit, member->{}", member);
        return(member);
    }
}

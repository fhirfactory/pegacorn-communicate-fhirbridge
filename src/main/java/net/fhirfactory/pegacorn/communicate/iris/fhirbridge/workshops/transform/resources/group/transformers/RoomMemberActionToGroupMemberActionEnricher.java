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

import net.fhirfactory.pegacorn.communicate.matrix.model.r110.events.common.contenttypes.MEventContentType;
import net.fhirfactory.pegacorn.communicate.matrix.model.r110.events.room.MRoomMemberEvent;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.valuesets.GroupMemberActionExtensionEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.extensions.GroupMemberActionExtensionEnricher;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author ACT Health
 */
@ApplicationScoped
public class RoomMemberActionToGroupMemberActionEnricher {
    private static final Logger LOG = LoggerFactory.getLogger(RoomMemberActionToGroupMemberActionEnricher.class);

    @Inject
    private GroupMemberActionExtensionEnricher membershipHelper;

    public void encrichMembershipComponent(Group.GroupMemberComponent membership, MRoomMemberEvent roomEvent)
    {
        LOG.debug("encrichMembershipComponent(): Entry, updating GroupMembershipComponent->{}, with roomEvent->{}", membership, roomEvent);
        boolean isValidMemberEvent = true;
        LOG.trace("encrichMembershipComponent(): Check to see if -state_key- and -content- are present");
        if (!roomEvent.hasStateKey()) {
            LOG.trace("encrichMembershipComponent(): no -state_key-");
            isValidMemberEvent = false;
        }
        if (!roomEvent.hasContent()) {
            LOG.trace("encrichMembershipComponent(): no -content-");
            isValidMemberEvent = false;
        }
        if (!isValidMemberEvent) {
            LOG.debug("encrichMembershipComponent(): Exit, no -content- or -state_key-");
            return;
        }
        LOG.trace("encrichMembershipComponent(): Extract the -content- from the Event");
        MEventContentType roomMemberEventContent = (MEventContentType)roomEvent.getContent();
        if (!roomMemberEventContent.hasMembership()) {
            LOG.trace("encrichMembershipComponent(): no -membership- field, this is Required");
            isValidMemberEvent = false;
        }
        if (!isValidMemberEvent) {
            LOG.debug("encrichMembershipComponent(): Exit, no -membership- field in the -content-");
            return;
        }
        GroupMemberActionExtensionEnum membershipAction;
        LOG.trace("encrichMembershipComponent(): Assign the -membership- action");
        switch (roomMemberEventContent.getMembership()) {
            case INVITE:
                LOG.trace("encrichMembershipComponent(): Assigning -Invite- as the Membership action");
                membershipAction = GroupMemberActionExtensionEnum.MEMBERSHIP_STATUS_INVITED;
                membershipHelper.injectGroupMemberActionStatusExtension(membership, membershipAction);
                break;
            case JOIN:
                LOG.trace("encrichMembershipComponent(): Assigning -Join- as the Membership action");
                membershipAction = GroupMemberActionExtensionEnum.MEMBERSHIP_STATUS_JOINED;
                membershipHelper.injectGroupMemberActionStatusExtension(membership, membershipAction);
                break;
            case KNOCK:
                LOG.trace("encrichMembershipComponent(): Assigning -Knock- as the Membership action");
                membershipAction = GroupMemberActionExtensionEnum.MEMBERSHIP_STATUS_KNOCK;
                membershipHelper.injectGroupMemberActionStatusExtension(membership, membershipAction);
                break;
            case LEAVE:
                LOG.trace("encrichMembershipComponent(): Assigning -leave- as the Membership action");
                membershipAction = GroupMemberActionExtensionEnum.MEMBERSHIP_STATUS_LEFT;
                membershipHelper.injectGroupMemberActionStatusExtension(membership, membershipAction);
                break;
            case BAN:
                LOG.trace("encrichMembershipComponent(): Assigning -ban- as the Membership action");
                membershipAction = GroupMemberActionExtensionEnum.MEMBERSHIP_STATUS_BANNED;
                membershipHelper.injectGroupMemberActionStatusExtension(membership, membershipAction);
                break;
            default:
                LOG.trace("encrichMembershipComponent(): Assigning default action (nochange) to the Membership action");
                membershipAction = GroupMemberActionExtensionEnum.MEMBERSHIP_STATUS_NOCHANGE;
                membershipHelper.injectGroupMemberActionStatusExtension(membership, membershipAction);
        }
        LOG.debug("encrichMembershipComponent(): Exit, updated Group.GroupMemberComponent --> {}",membership );
    }
}

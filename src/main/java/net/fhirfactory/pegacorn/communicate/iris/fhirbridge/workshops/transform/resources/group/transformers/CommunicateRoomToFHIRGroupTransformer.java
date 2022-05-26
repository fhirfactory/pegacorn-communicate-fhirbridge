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

import net.fhirfactory.pegacorn.internals.communicate.entities.rooms.CommunicateRoom;
import net.fhirfactory.pegacorn.internals.communicate.entities.rooms.datatypes.CommunicateRoomMember;
import net.fhirfactory.pegacorn.internals.communicate.entities.rooms.valuesets.CommunicateRoomTypeEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.factories.GroupCodeFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.factories.GroupFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.factories.GroupIdentifierFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.valuesets.GroupCodeValueSet;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class CommunicateRoomToFHIRGroupTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(CommunicateRoomToFHIRGroupTransformer.class);

    @Inject
    private GroupFactory groupFactory;

    @Inject
    private CommunicateRoomIDToFHIRGroupIdentifierTransformer identifierFactory;

    @Inject
    private GroupCodeFactory groupCodeFactory;

    @Inject
    private GroupCodeValueSet groupCodeValueSet;

    /**
     * This method takes a roomType (CommunicateRoomTypeEnum) and generates a FHIR::CodeableConcept value for it. The CodeableConcept uses the
     * default deployment System details for qualifying the contained Code.
     * @param roomType The (Communicate) kind of roomType of the Room
     * @return A FHIR::CodeableConcept describing the code (FHIR::Group.kind) of the room.
     */
    public CodeableConcept transformRoomTypeToGroupCode(CommunicateRoomTypeEnum roomType){
        LOG.debug(".transformRoomTypeToGroupCode(): Entry, roomType->{}", roomType);
        CodeableConcept groupCode = groupCodeFactory.newGroupCode(roomType.getRoomType(), roomType.getRoomTypeDescription());
        LOG.debug(".transformRoomTypeToGroupCode(): Exit, groupCode->{}", groupCode);
        return(groupCode);
    }

    /**
     * This method takes a CodeableConcept (FHIR::Group.kind) and extracts the roomType (CommunicateRoomTypeEnum) of the room.
     *
     * @param groupCode The CodeableConcept used to describe the kind of a room (FHIR::Group.kind).
     * @return An enum (CommunicateRoomTypeEnum) describing the type of (Communicate) room.
     */
    public CommunicateRoomTypeEnum extractCommunicateRoomType(CodeableConcept groupCode){
        if(groupCode == null){
            return(null);
        }
        if(!groupCode.hasCoding()){
            return(null);
        }
        for(Coding code: groupCode.getCoding()){
            if(code.getSystem().contentEquals(groupCodeValueSet.getCommunicateRoomCodeSystem())){
                CommunicateRoomTypeEnum roomType = CommunicateRoomTypeEnum.fromRoomTypeString(code.getCode());
                if(roomType != null){
                    return(roomType);
                }
            }
        }
        return(null);
    }

    /**
     * This room takes a list (List) of CommunicateRoomMembers and a roomType enum to generate a set BackboneElements for inclusion in
     * the FHIR::Group.member field.
     *
     * @param roomType
     * @param memberList
     * @return
     */
    public List<BackboneElement> newGroupMember(CommunicateRoomTypeEnum roomType, List<CommunicateRoomMember> memberList){
        List<BackboneElement> groupMemberList = new ArrayList<>();
        if(memberList == null){
            return(groupMemberList);
        }
        if(memberList.isEmpty()){
            return(groupMemberList);
        }
        switch(roomType){
            case COMMUNICATE_PRACTITIONER_ROLE_FULFILMENT_DISCUSSION_ROOM:
        }
        for(CommunicateRoomMember roomMember: memberList){
            Group.GroupMemberComponent currentGroupMember = new Group.GroupMemberComponent();

        }
        return(groupMemberList);
    }

    /**
     *
     * @param group
     * @return
     */
    public List<CommunicateRoomMember> extractGroupMembership(Group group){
        List<CommunicateRoomMember> roomMembers = new ArrayList<>();

        return(roomMembers);
    }

    @Inject
    private GroupIdentifierFactory groupIdentifierFactory;

    public Group newGroup(CommunicateRoom room){
        LOG.debug(".newGroup(): Entry, room->{}", room);
        if(room == null){
            LOG.debug(".newGroup(): Exit, room is null");
            return(null);
        }
        String groupName = room.getDisplayName();
        Period period = room.getActivePeriod().getPeriod();
        Identifier groupIdentifier = groupIdentifierFactory.newCommunicateRoomBasedGroupIdentifier(room.getMatrixRoomID(), period);
        CodeableConcept roomType = transformRoomTypeToGroupCode(room.getRoomType());
        Group group = groupFactory.newGroup(groupName, groupIdentifier, roomType);

        return(group);
    }
}

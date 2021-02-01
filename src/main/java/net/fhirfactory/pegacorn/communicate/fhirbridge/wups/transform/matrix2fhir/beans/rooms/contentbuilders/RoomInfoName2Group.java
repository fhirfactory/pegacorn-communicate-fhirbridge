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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.rooms.contentbuilders;

import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.MinorTransformationException;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.common.MatrixAttribute2FHIRIdentifierBuilders;
import net.fhirfactory.pegacorn.referencevalues.PegacornSystemReference;
import org.hl7.fhir.r4.model.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;

/**
 *
 * @author Mark A. Hunter (ACT Health)
 */
public class RoomInfoName2Group {

    private static final Logger LOG = LoggerFactory.getLogger(RoomInfoName2Group.class);

    @Inject
    PegacornSystemReference pegacornSystemReference;

    @Inject
    MatrixAttribute2FHIRIdentifierBuilders identifierBuilders;
    
    
    public Bundle matrixRoomNameEvent2FHIRGroupBundle(String theMessage) throws MinorTransformationException {
        Bundle newBundleElement = new Bundle();
        LOG.debug(".matrixRoomNameEvent2FHIRGroupBundle(): Message In --> " + theMessage);
        Group groupElement = new Group();
        MessageHeader messageHeader = new MessageHeader();
        LOG.trace(".matrixRoomNameEvent2FHIRGroupBundle(): Message to be converted --> " + theMessage);
        try {
            groupElement = roomInfoNameEvent2Group(theMessage);
            messageHeader = matrix2MessageHeader(groupElement, theMessage);
            newBundleElement.setType(Bundle.BundleType.MESSAGE);
            Bundle.BundleEntryComponent bundleEntryForMessageHeaderElement = new Bundle.BundleEntryComponent();
            bundleEntryForMessageHeaderElement.setResource(messageHeader);
            Bundle.BundleEntryComponent bundleEntryForCommunicationElement = new Bundle.BundleEntryComponent();
            Bundle.BundleEntryRequestComponent bundleRequest = new Bundle.BundleEntryRequestComponent();
            bundleRequest.setMethod(Bundle.HTTPVerb.PUT);
            bundleRequest.setUrl("Group");
            bundleEntryForCommunicationElement.setRequest(bundleRequest);
            newBundleElement.addEntry(bundleEntryForMessageHeaderElement);
            newBundleElement.addEntry(bundleEntryForCommunicationElement);
            newBundleElement.setTimestamp(new Date());
            return (newBundleElement);
        } catch (JSONException jsonExtractionError) {
            throw (new MinorTransformationException("matrixRoomNameEvent2FHIRGroupBundle(): Bad JSON Message Structure -> ", jsonExtractionError));
        }
    }
    
    public MessageHeader matrix2MessageHeader(Group theResultantGroupElement, String theMessage) {
        MessageHeader messageHeaderElement = new MessageHeader();
        Coding messageHeaderCode = new Coding();
        messageHeaderCode.setSystem("http://pegacorn.fhirbox.net/pegacorn/R1/message-codes");
        messageHeaderCode.setCode("group-bundle");
        messageHeaderElement.setEvent(messageHeaderCode);
        MessageHeader.MessageSourceComponent messageSource = new MessageHeader.MessageSourceComponent();
        messageSource.setName("Pegacorn Matrix2FHIR Integration Service");
        messageSource.setSoftware("Pegacorn::Communicate::Iris");
//        messageSource.setEndpoint(communicateProperties.getIrisEndPointForIncomingGroupBundle());
        return (messageHeaderElement);
    }

    public Group roomInfoNameEvent2Group(String theMessage) throws MinorTransformationException {
        LOG.debug(".roomInfoNameEvent2Group(): Message In --> " + theMessage);
        Group newGroup;
        LOG.trace(".roomInfoNameEvent2Group(): Message to be converted --> " + theMessage);
        try {
            JSONObject roomStatusEvent = new JSONObject(theMessage);
            newGroup = buildGroupEntityFromRoomNameEvent(roomStatusEvent);
        } catch (Exception Ex) {
            Group emptyGroup = new Group();
            return (emptyGroup);
        }
        return (newGroup);
    }

    /**
     * This method constructs a basic FHIR::Group entity and then calls a the
     * other methods within this class to populate the relevant attributes.
     *
     * @param pMessageObject A Matrix(R) "m.room.create" message (see
     * https://matrix.org/docs/spec/client_server/r0.6.0#m-room-create)
     * @return Communication A FHIR::Communication resource (see
     * https://www.hl7.org/fhir/group.html)
     */
    private Group buildGroupEntityFromRoomNameEvent(JSONObject pRoomServerEvent) throws MinorTransformationException{
        LOG.debug(".buildGroupEntityFromRoomNameEvent() for Event --> " + pRoomServerEvent);
        if( !pRoomServerEvent.has("content") ){
            throw(new MinorTransformationException("m.room.name event has no -content-"));
        }
        JSONObject roomServerEventContent = pRoomServerEvent.getJSONObject("content");
        if( !roomServerEventContent.has("name")){
            throw(new MinorTransformationException("m.room.name event has no -name-"));
        }
        // Create the empty FHIR::Group entity.
        Group newGroup = new Group();
        // Add the FHIR::Group.Identifier (type = FHIR::Identifier) Set
        newGroup.addIdentifier(this.buildGroupIdentifier(pRoomServerEvent));
        Extension localRoomPriorityValue = new Extension();
//        newGroup.setGroupPriority(50);
        newGroup.setActive(true);
        newGroup.setType(Group.GroupType.PRACTITIONER);
        newGroup.setActual(true);
        newGroup.setName(roomServerEventContent.getString("name"));
//        if(roomNameMap.getName(pRoomServerEvent.getString("room_id")) == null){
//            LOG.trace("buildGroupEntityFromRoomNameEvent(): No existing name in RoomID2RoomNameReferenceMap, so adding");
//            roomNameMap.setName(pRoomServerEvent.getString("room_id"), roomServerEventContent.getString("name"));
//        } else {
//            LOG.trace("buildGroupEntityFromRoomNameEvent(): An existing name in RoomID2RoomNameReferenceMap, so modifying");
//            roomNameMap.modifyName(pRoomServerEvent.getString("room_id"), roomServerEventContent.getString("name"));
//        }
        LOG.debug(".buildGroupEntityFromRoomNameEvent(): Created Identifier --> " + newGroup.toString());
        return (newGroup);
    }

    /**
     * This method constructs a basic FHIR::Identifier entity from
     * the given message. Typically, there will be at least 2 FHIR::Identifiers
     * for a Group (where a group is used within the Pegacorn::Communicate
     * system), one being the base RoomServer ID and the other being canonical
     * defined within Pegacorn::Ladon
     *
     * @param pRoomEventMessage A Matrix(R) "m.room.name" message (see
     * https://matrix.org/docs/spec/client_server/r0.6.0#m-room-name)
     * @return Identifier A FHIR::Identifier resource (see
     * https://www.hl7.org/fhir/datatypes.html#Identifier)
     */
    private Identifier buildGroupIdentifier(JSONObject pRoomEventMessage) {
        if ((pRoomEventMessage == null) || pRoomEventMessage.isEmpty()) {
            LOG.debug("buildGroupIdentifier(): Room Event Message is Empty");
            return (null);
        }
        String localRoomID = pRoomEventMessage.getString("room_id");
        if (localRoomID.isEmpty()) {
            LOG.debug("buildGroupIdentifier(): Room ID from RoomServer is Empty");
            return (null);
        }
        LOG.trace(".buildGroupIdentifier(): for Event --> " + localRoomID);
        Long localGroupAge;
        if (pRoomEventMessage.has("origin_server_ts")) {
            localGroupAge = pRoomEventMessage.getLong("origin_server_ts");
        } else {
            localGroupAge = 0L;
        }
        // Create the empty FHIR::Identifier element
        Identifier localResourceIdentifier = this.identifierBuilders.buildFHIRGroupIdentifierFromMatrixRoomID(localRoomID, localGroupAge);
        LOG.debug(".buildGroupIdentifier(): Created Identifier --> " + localResourceIdentifier.toString());
        return (localResourceIdentifier);
    }
}

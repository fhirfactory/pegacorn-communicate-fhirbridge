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
package net.fhirfactory.pegacorn.communicate.iris.core.fhir2matrix.communication;

import net.fhirfactory.pegacorn.communicate.iris.core.matrxi2fhir.common.MatrixAttribute2FHIRIdentifierBuilders;
import net.fhirfactory.pegacorn.communicate.iris.core.common.keyidentifiermaps.MatrixUserID2PractitionerIDMap;
import net.fhirfactory.pegacorn.communicate.iris.core.common.keyidentifiermaps.MatrixRoomID2ResourceReferenceMap;

import java.util.ArrayList;

import java.util.List;
import javax.inject.Inject;

import org.hl7.fhir.r4.model.Communication;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.pegacorn.communicate.iris.core.common.exceptions.MinorTransformationException;
import net.fhirfactory.pegacorn.referencevalues.PegacornSystemReference;

import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Transform Room Based Message to a FHIR::Communication Resource</h1>
 * <p>
 * This class is used to perform the transformation of a Room Message
 * (encapsulated as a Matrix(R) room_message_event and convert it to a
 * FHIR::Communication resource.
 * <p>
 * To do this, the code needs to construct the apropriate references (for the
 * FHIR::Communication.Identifier of the message, the
 * FHIR::Communication.Sender, the FHIR::Communication.Recipient and the
 * FHIR::Communication.Subject) - it then needs to extract the content type and
 * encapsulate it into the FHIR::Communication.payload attribute.
 * <p>
 The Reference for the FHIR::Communication.Subject and
 FHIR::Communication.Recipient are extracted from a RoomID-ReferenceMap
 maintained in the AppServers shared memory cache (see
 MatrixRoomID2ResourceReferenceMap.java).
 <p>
 * <b> Note: </b> If the content within the message is video ("m.video"), audio
 * ("m.audio") or image ("m.image") the a discrete FHIR::Media resource is
 * created and the FHIR::Communication.payload attribute is set to point (via a
 * FHIR::Reference) to the FHIR::Media element.
 *
 * <b> Note: </b> the following configuration details need to be loaded into the
 * Wildfly Application Server configuration file (standalone-ha.xml) {@code
 * <cache-container name="pegacorn-communicate" default-cache=
 * "general" module="org.wildfly.clustering.server">
 * <transport lock-timeout="15000" />
 * <replicated-cache name="general">
 * <transaction locking="OPTIMISTIC" mode="FULL_XA"/>
 * </replicated-cache>
 * <replicated-cache name="room2resource_id_map">
 * <transaction locking="OPTIMISTIC" mode="FULL_XA"/>
 * </replicated-cache>
 * <replicated-cache name="user2practitioner_id_map">
 * <transaction locking="OPTIMISTIC" mode="FULL_XA"/>
 * </replicated-cache>>
 * </cache-container>}
 *
 *
 * @author Mark A. Hunter (ACT Health)
 * @since 2020-01-20
 *
 */
public class Communication2RoomMessage {

    private static final Logger LOG = LoggerFactory.getLogger(Communication2RoomMessage.class);

    PegacornSystemReference pegacornSystemReference = new PegacornSystemReference();

    FhirContext fhirContext = FhirContext.forR4();
    ca.uhn.fhir.parser.IParser fhirParser = this.fhirContext.newJsonParser();

    @Inject
    MatrixAttribute2FHIRIdentifierBuilders identifierBuilders;
    MatrixAttribute2FHIRIdentifierBuilders getIdentifierBuilders(){return (identifierBuilders);}
    
    @Inject
    protected MatrixRoomID2ResourceReferenceMap theRoom2ReferenceIDMap;
    @Inject
    protected MatrixUserID2PractitionerIDMap theUserID2PractitionerIDMap;

    /**
     * The method is the primary (exposed) method for performing the entity
     * transformation. It incorporates a switch statement to derive the nature
     * of the "payload" transformation (re-encapsulation) to be performed.
     *
     * @param communicationEvent A FHIR::Communication resource which has a Payload that is 
     * to be injected into the Room. (see
     * https://www.hl7.org/fhir/communication.html for information on the element structure)
     * @return A list of Matrix(R) "m.room.message" message (see
     * https://matrix.org/docs/spec/client_server/r0.6.0#room-event-fields)
     *
     * @throws MinorTransformationException
     */
    public List<JSONObject> transfromCommunicatinToMatrixRoomMessageSet(Communication communicationEvent) throws MinorTransformationException {
        LOG.debug(".doTransform(): Entry, Message In --> {}", communicationEvent);
        Communication localCommunicationEvent = new Communication();
        LOG.trace("Message to be converted --> " + fhirParser.encodeResourceToString(communicationEvent));
        try {
            LOG.trace("doTransform(): Create empty matrix room message");
            JSONObject newMatrixRoomMessage = new JSONObject();
            LOG.trace("doTransform(): Extract message content - check content/payload type");
            /*
            switch (pCommunicationEvent.) {
                case "m.audio": {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> m.audio");
                    List<CommunicationPayloadComponent> localPayloadList = new ArrayList<CommunicationPayloadComponent>();
                    CommunicationPayloadComponent localPayload = new CommunicationPayloadComponent();
                    List<Reference> localMediaEntityReferences = this.buildMediaReference(localMessageEvent);
                    for (Integer localCounter = 0; localCounter < localMediaEntityReferences.size(); localCounter += 1) {
                        localPayloadList.add(localPayload.setContent(localMediaEntityReferences.get(localCounter)));
                    }
                    localCommunicationEvent.setPayload(localPayloadList);
                    break;
                }
                case "m.emote": {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> m.emote");
                    break;
                }
                case "m.file": {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> m.file");
                    break;
                }
                case "m.image": {
                    LOG.trace(".doTransform(): Message Type (msgtype --> m.image");
                    List<CommunicationPayloadComponent> localPayloadList = new ArrayList<CommunicationPayloadComponent>();
                    CommunicationPayloadComponent localPayload = new CommunicationPayloadComponent();
                    List<Reference> localMediaEntityReferences = this.buildMediaReference(localMessageEvent);
                    for (Integer localCounter = 0; localCounter < localMediaEntityReferences.size(); localCounter += 1) {
                        localPayloadList.add(localPayload.setContent(localMediaEntityReferences.get(localCounter)));
                    }
                    localCommunicationEvent.setPayload(localPayloadList);
                    break;
                }
                case "m.location": {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> m.location");
                    break;
                }
                case "m.notice": {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> m.notice");
                    break;
                }
                case "m.server_notice": {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> m.server_notice");
                    break;
                }
                case "m.text": {
                    LOG.trace(".doTransform(): Case --> Message Type (msgtype) == m.text: Start");
                    List<CommunicationPayloadComponent> localPayloadList = this.buildMTextPayload(localMessageContent);
                    localCommunicationEvent.setPayload(localPayloadList);
                    Reference referredToCommunicationEvent = this.buildInResponseTo(localMessageContent);
                    if( referredToCommunicationEvent != null ){
                        localCommunicationEvent.addInResponseTo(referredToCommunicationEvent);
                    }
                    LOG.trace(".doTransform(): Case --> Message Type (msgtype) == m.text: Finished");
                    break;
                }
                case "m.video": {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> m.video");
                    List<CommunicationPayloadComponent> localPayloadList = new ArrayList<CommunicationPayloadComponent>();
                    CommunicationPayloadComponent localPayload = new CommunicationPayloadComponent();
                    List<Reference> localMediaEntityReferences = this.buildMediaReference(localMessageEvent);
                    for (Integer localCounter = 0; localCounter < localMediaEntityReferences.size(); localCounter += 1) {
                        localPayloadList.add(localPayload.setContent(localMediaEntityReferences.get(localCounter)));
                    }
                    localCommunicationEvent.setPayload(localPayloadList);
                    break;
                }
                default: {
                    LOG.trace(".doTransform(): Message Type (msgtype) --> unknown");
                    throw (new TransformErrorException("Unknown Message Type"));
                }
            }
             */

            LOG.debug(".doTransform(): Created Communication Message --> " + localCommunicationEvent.toString());
            ArrayList<JSONObject> messageList = new ArrayList<JSONObject>();
            return (messageList);
        } catch (JSONException jsonExtractionError) {
            throw (new MinorTransformationException("Bad JSON Message Structure -> ", jsonExtractionError));
        }
    }


}

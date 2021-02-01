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
package net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.instantmessages;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.MajorTransformationException;
import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.MatrixMessageException;
import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.MinorTransformationException;
import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.WrongContentTypeException;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.common.MatrixAttribute2FHIRIdentifierBuilders;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.instantmessages.contentbuilders.MatrixRoomID2FHIRGroupReference;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.instantmessages.contentbuilders.MatrixRoomIMMediaContent2FHIRMediaReferenceSet;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.instantmessages.contentbuilders.MatrixRoomIMTextMessageContent2FHIRCommunicationPayload;
import net.fhirfactory.pegacorn.datasets.fhir.r4.internal.topics.FHIRElementTopicIDBuilder;
import net.fhirfactory.pegacorn.deployment.properties.SystemWideProperties;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.referencevalues.PegacornSystemReference;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Communication.CommunicationPayloadComponent;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
 * The Reference for the FHIR::Communication.Subject and
 * FHIR::Communication.Recipient are extracted from a RoomID-ReferenceMap
 * maintained in the AppServers shared memory cache (see
 * MatrixRoomID2ResourceReferenceMap.java).
 * <p>
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
@ApplicationScoped
public class MatrixRoomIM2FHIRCommunication extends MatrixRoomIM2FHIRCommunicationBase
{

    private static final Logger LOG = LoggerFactory.getLogger(MatrixRoomIM2FHIRCommunication.class);

    @Inject
    PegacornSystemReference pegacornSystemReference;

    @Inject
    MatrixAttribute2FHIRIdentifierBuilders identifierBuilders;

    @Inject
    private MatrixRoomIMMediaContent2FHIRMediaReferenceSet mediaReferenceGenerator;

    @Inject
    private MatrixRoomID2FHIRGroupReference roomID2GroupReference;

    @Inject
    private MatrixRoomIMTextMessageContent2FHIRCommunicationPayload matrixTextContent2CommunicationPayloadMapper;

    @Inject
    private FHIRElementTopicIDBuilder fhirElementTopicIDBuilder;

    @Inject
    private FHIRContextUtility fhirContextUtility;

    @Inject
    private SystemWideProperties systemWideProperties;

    /**
     * The method is the primary method for performing the entity
     * transformation. It incorporates a switch statement to derive the nature
     * of the "payload" transformation (re-encapsulation) to be performed.
     *
     * @param theMatrixRoomInstantMessage A Matrix::m.room.message (see
     * https://matrix.org/docs/spec/client_server/r0.6.0#room-event-fields)
     * @return Communication A FHIR::Communication resource (see
     * https://www.hl7.org/fhir/communication.html)
     * @throws MinorTransformationException
     */
    public Communication buildCommunicationResourceFromMatrixIMMessage(String theMatrixRoomInstantMessage)
            throws MatrixMessageException, MajorTransformationException, JSONException
    {
        LOG.debug("buildCommunicationResourceFromMatrixIMMessage(): The incoming Matrix Instant Message is --> {}", theMatrixRoomInstantMessage);
        // The code wouldn't have got here if the Incoming Message was empty or null, so don't check again.
        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Creating our two primary working objects, fhirCommunication (Communication) & matrixMessageObject (JSONObject)");
        Communication fhirCommunication;
        JSONObject matrixMessageObject = new JSONObject(theMatrixRoomInstantMessage);
        // So we now have a valid JSONObject for the incoming Matrix Instant Message
        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Conversion of incoming Matrix Instant Message into JSONObject successful, now extracting Instant Message -content- field");
        if (!matrixMessageObject.has("content")) {
            LOG.error("buildCommunicationResourceFromMatrixIMMessage(): Exit, Matrix Room Instant Message (m.room.message) --> missing -content- field");
            throw (new MatrixMessageException("matrix2Communication(): Exit, Matrix Room Instant Message (m.room.message) --> missing -content- field"));
        }
        JSONObject messageContent = matrixMessageObject.getJSONObject("content");
        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Extracted -content- field from Message Object, -content- --> {}", messageContent);
        // OK, now build messageDate --> using present instant if none provided TODO : perhaps we shouldn't use instant
        Date messageDate;
        if (matrixMessageObject.has("origin_server_ts")) {
            messageDate = new Date(matrixMessageObject.getLong("origin_server_ts"));
        } else {
            messageDate = Date.from(Instant.now());
        }
        // OK, so now we want to build the basic structure of the Communication object, which is common irrespective of Instant Message type
        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Building the basic structure of the Communication object");
        fhirCommunication = buildDefaultCommunicationEntity(matrixMessageObject);
        ArrayList<CommunicationPayloadComponent> localPayloadList = new ArrayList<>();
        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Built default basic Communication object, now performing Swtich analysis for -content- type");
        switch (messageContent.getString("msgtype")) {
            case "m.audio": {
                LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.audio");
                CommunicationPayloadComponent localPayload = new CommunicationPayloadComponent();
                try {
                    Reference newMediaReference = this.mediaReferenceGenerator.buildVideoReference(messageContent, messageDate);
                    localPayloadList.add(localPayload.setContent(newMediaReference));
                } catch (WrongContentTypeException | MinorTransformationException minorException) {
                    if (minorException instanceof WrongContentTypeException) {
                        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> thought it was m.audio --> not creating a FHIR::Media reference!");
                    } else {
                        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.audio, error decoding --> not creating a FHIR::Media reference!");
                    }
                }
                break;
            }
            case "m.emote": {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.emote");
                break;
            }
            case "m.file": {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.file");
                CommunicationPayloadComponent localPayload = new CommunicationPayloadComponent();
                try {
                    Reference newMediaReference = this.mediaReferenceGenerator.buildFileReference(messageContent, messageDate);
                    localPayloadList.add(localPayload.setContent(newMediaReference));
                } catch (WrongContentTypeException | MinorTransformationException minorException) {
                    if (minorException instanceof WrongContentTypeException) {
                        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> thought it was m.file --> not creating a FHIR::Media reference!");
                    } else {
                        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.file, error decoding --> not creating a FHIR::Media reference!");
                    }
                }
                break;
            }
            case "m.image": {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): MMatrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.image");
                CommunicationPayloadComponent localPayload = new CommunicationPayloadComponent();
                try {
                    Reference newMediaReference = this.mediaReferenceGenerator.buildVideoReference(messageContent, messageDate);
                    localPayloadList.add(localPayload.setContent(newMediaReference));
                } catch (WrongContentTypeException | MinorTransformationException minorException) {
                    if (minorException instanceof WrongContentTypeException) {
                        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> thought it was m.image --> not creating a FHIR::Media reference!");
                    } else {
                        LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.image, error decoding --> not creating a FHIR::Media reference!");
                    }
                }
                break;
            }
            case "m.location": {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.location");
                break;
            }
            case "m.notice": {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.notice");
                break;
            }
            case "m.server_notice": {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.server_notice");
                break;
            }
            case "m.text": {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.text");
                LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.text: Finished");
                break;
            }
            case "m.video": {
                LOG.trace("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.video");
                CommunicationPayloadComponent localPayload = new CommunicationPayloadComponent();
                try {
                    Reference newMediaReference = this.mediaReferenceGenerator.buildVideoReference(messageContent, messageDate);
                    localPayloadList.add(localPayload.setContent(newMediaReference));
                } catch (WrongContentTypeException | MinorTransformationException minorException) {
                    if (minorException instanceof WrongContentTypeException) {
                        LOG.debug("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> thought it was m.video --> not creating a FHIR::Media reference!!");
                    } else {
                        LOG.debug("buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> m.video, error decoding --> not creating a FHIR::Media reference!");
                    }
                }
                break;
            }
            default: {
                LOG.trace(".buildCommunicationResourceFromMatrixIMMessage(): Matrix Room Instant Message (m.room.message), -content-, -msgtype- --> unknown");
                throw (new MatrixMessageException("matrix2Communication(): Matrix Room Instant Message (m.room.message) --> Unknown Message Type"));
            }
        }

        CommunicationPayloadComponent messageContentAsTextPayload = this.matrixTextContent2CommunicationPayloadMapper.buildTextPayload(messageContent);
        localPayloadList.add(messageContentAsTextPayload);
        fhirCommunication.setPayload(localPayloadList);
        Reference referredToCommunicationEvent = this.buildInResponseTo(messageContent);
        if (referredToCommunicationEvent != null) {
            fhirCommunication.addInResponseTo(referredToCommunicationEvent);
        }
        LOG.debug(".buildCommunicationResourceFromMatrixIMMessage(): Created Communication Message --> {}", fhirCommunication);
        return (fhirCommunication);
    }

    
    private Reference buildInResponseTo(JSONObject pRoomMessageContent)
    {
        LOG.debug(".buildInResponseTo(): Entry, for Event --> " + pRoomMessageContent.toString());
        if (!(pRoomMessageContent.has("m.relates_to"))) {
            return (null);
        }
        JSONObject referredToMessageContent = pRoomMessageContent.getJSONObject("m.relates_to");
        if (!(referredToMessageContent.has("m.in_reply_to"))) {
            return (null);
        }
        JSONObject referredToMessage = referredToMessageContent.getJSONObject("m.in_reply_to");
        if (!(referredToMessage.has("event_id"))) {
            return (null);
        }
        Reference referredCommunicationMessage = new Reference();
        LOG.trace(".buildInResponseTo(): Create the empty FHIR::Identifier element");
        Identifier localResourceIdentifier = new Identifier();
        LOG.trace(".buildInResponseTo(): Set the FHIR::Identifier.Use to -OFFICIAL- (we are the source of truth for this)");
        localResourceIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
        LOG.trace(".buildInResponseTo(): Set the FHIR::Identifier.System to Pegacorn (it's our ID we're creating)");
        localResourceIdentifier.setSystem(pegacornSystemReference.getDefaultIdentifierSystemForCommunicateGroupServer());
        LOG.trace(".buildInResponseTo(): Set the FHIR::Identifier.Value to the -event_id- from the RoomServer system {}", referredToMessage.getString("event_id"));
        localResourceIdentifier.setValue(referredToMessage.getString("event_id"));
        LOG.trace(".buildInResponseTo(): Identifier added to Reference --> " + localResourceIdentifier.toString());
        referredCommunicationMessage.setIdentifier(localResourceIdentifier);
        LOG.trace(".buildInResponseTo(): Add type to the Reference");
        referredCommunicationMessage.setType("Communication");
        LOG.debug(".buildInResponseTo(): Exit, created Reference --> " + referredCommunicationMessage.toString());
        return (referredCommunicationMessage);
    }

    /**
     * This method constructs a FHIR::Reference entity for the Subject of the
     * message based on the RoomServer.RoomID (i.e. "room_id").
     * <p>
     * There is only a single Subject (which may be a FHIR::Group).
     * <p>
     * The method extracts the RoomMessage.RoomID (i.e. "room_id") and attempts
     * to find the corresponding FHIR::Reference in the
     * MatrixRoomID2ResourceReferenceMap cache map.
     * <p>
     * The resulting single FHIR::Reference is then returned. If no Reference is
     * found, then an new (non-conanical) one is created that points to a
     * FHIR::Group.
     *
     * @param roomIM A Matrix(R) "m.room.message" message (see
     * https://matrix.org/docs/spec/client_server/r0.6.0#room-event-fields)
     * @return The FHIR::Reference for the subject (see
     * https://www.hl7.org/fhir/references.html#Reference)
     */
    private Reference buildSubjectReference(JSONObject roomIM)
            throws MatrixMessageException, JSONException
    {
        LOG.debug("buildSubjectReference(): Entry, for Matrix Room Instant Message --> {}", roomIM);
        // For now, we are assuming it is a "FHIR::Group"
        if (!roomIM.has("room_id")) {
            throw (new MatrixMessageException("Matrix Room Instant Message --> has not -room_id-"));
        }
        try {
            Reference subjectReference = roomID2GroupReference.buildFHIRGroupReferenceFromMatrixRoomID(roomIM.getString("room_id"), true);
            LOG.debug(".buildSubjectReference(): Exit, Created FHIR::Group Reference --> {}", subjectReference);
            return (subjectReference);
        } catch (MinorTransformationException transformException) {
            LOG.debug(".buildSubjectReference(): Exit, Could not create FHIR::Group Reference, room_id is null, returning null");
            return (null);
        }
    }

    public UoW convertMatrixRoomIM2FHIRCommunication(UoW unitOfWork){
        String roomIM = unitOfWork.getIngresContent().getPayload();
        Communication resultantResource = null;
        try{
            resultantResource = buildCommunicationResourceFromMatrixIMMessage(roomIM);
        } catch(Exception ex){
            unitOfWork.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            unitOfWork.setFailureDescription(ex.getMessage());
            return(unitOfWork);
        }
        TopicToken newToken = fhirElementTopicIDBuilder.createTopicToken(ResourceType.Communication.name(), systemWideProperties.getPegacornDefaultFHIRVersion());
        IParser fhirResourceParser = fhirContextUtility.getJsonParser();
        String communicationAsString = fhirResourceParser.encodeResourceToString(resultantResource);
        UoWPayload outputPayload = new UoWPayload(newToken,communicationAsString);
        unitOfWork.getEgressContent().addPayloadElement(outputPayload);
        unitOfWork.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        return(unitOfWork);
    }

}

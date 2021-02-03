package net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.instantmessages;

import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.MajorTransformationException;
import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.MatrixMessageException;
import net.fhirfactory.pegacorn.datasets.fhir.r4.base.entities.bundle.MessageHeaderHelper;
import net.fhirfactory.pegacorn.datasets.fhir.r4.internal.topics.FHIRElementTopicIDBuilder;
import net.fhirfactory.pegacorn.deployment.properties.communicate.CommunicateNames;
import net.fhirfactory.pegacorn.deployment.properties.ladon.LadonComponentNames;
import net.fhirfactory.pegacorn.datasets.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.ResourceType;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;

@ApplicationScoped
public class MatrixRoomIM2FHIRBundle {
    private static final Logger LOG = LoggerFactory.getLogger(MatrixRoomIM2FHIRBundle.class);

    @Inject
    private MatrixRoomIM2FHIRCommunication im2FHIRCommunication;

    @Inject
    private MessageHeaderHelper messageHeaderHelper;

    @Inject
    private CommunicateNames pegacornCommunicateNames;

    @Inject
    private LadonComponentNames ladonComponentNames;

    @Inject
    private FHIRContextUtility fhirContextUtility;

    @Inject
    private FHIRElementTopicIDBuilder fhirElementTopicIDBuilder;

    @Inject
    private PegacornReferenceProperties systemWideProperties;

    /**
     *
     * This function takes an incoming Matrix Room Instant Message
     * (Matrix::m.room.message) and converts it to a FHIR::Bundle containing a
     * FHIR::MessageHeader and a FHIR::Communication element set.
     * <p>
     * Where the message is originating from a Matrix::User, the function
     * looks-up the PractitionerID2MatrixName Cachemap to ascertain if there is
     * any pre-existing mapping between a Matrix User and a FHIR::Practitioner.
     * If not, it creates a temporary FHIR::Identifier (Use = TEMP). This
     * FHIR::Identifier is then used to construct the FHIR::Reference for the
     * FHIR::Communication.Sender field.
     * <p>
     * Similarly, depending on the Matrix::Room (Group) that the message is
     * associated with, the FHIR::Communication.Subject field is populated with
     * a FHIR::Reference associated with a FHIR::CareTeam, FHIR::Group,
     * FHIR::Organisation, FHIR::PractitionerRole or FHIR::Patient.
     * <p>
     * If the message (Matrix::m.room.message) is part of a peer-to-peer
     * dialogue, then the FHIR::Communication.Target is populated with the
     * appropriate FHIR::Practitioner or FHIR::PractitionerRole. Again, the
     * FHIR::Identifier for these may be queried from the
     * PractitionerID2MatrixName map.
     *
     * @param unitOfWork The incoming Matrix Room Instant Message
     * @return A List of FHIR::Bundle element, each comprising -->
     * FHIR::MessageHeader, FHIR::Communication
     * @throws MatrixMessageException
     * @throws JSONException
     * @throws MajorTransformationException
     *
     * @see
     * <a href="https://matrix.org/docs/spec/client_server/r0.6.0#room-event-fields">Matrix Client-Server API Specificaton, Release 0.6.0 - "room_instant_message" Message</a> <p>
     * <a href="https://www.hl7.org/fhir/bundle.html">FHIR Specification, Release 4.0.1, "Bundle" Resource</a>
     *
     */
    public UoW convertMatrixRoomIM2FHIRBundle(UoW unitOfWork){
        LOG.debug("convertMatrixRoomIM2FHIRBundle(): Entry, incoming UoW --> {}", unitOfWork);
        if (unitOfWork == null) {
            UoWPayload emptyPayload = new UoWPayload();
            UoW uowInstance = new UoW(emptyPayload);
            uowInstance.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            uowInstance.setFailureDescription("Empty UoW Provided");
            return(uowInstance);
        }
        String imInstance = unitOfWork.getIngresContent().getPayload();
        Communication generatedCommunication = null;
        try{
            generatedCommunication = im2FHIRCommunication.buildCommunicationResourceFromMatrixIMMessage(imInstance);
        } catch(Exception ex){
            unitOfWork.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            unitOfWork.setFailureDescription(ex.getMessage());
            return(unitOfWork);
        }
        // We need some helper functions to encode/decode the FHIR based message structures
        LOG.trace("convertMatrixRoomIM2FHIRBundle(): Initialising FHIR Resource Parser & Setting Pretty Print");
        IParser fhirResourceParser = fhirContextUtility.getJsonParser();
        LOG.trace("convertMatrixRoomIM2FHIRBundle(): Generating MessageHeader for Bundle");
        MessageHeader newMessageHeader = messageHeaderHelper.buildMessageHeader(pegacornCommunicateNames.getFHIRBridgeSubsystem(), ladonComponentNames.getLadonSubsystemDefault());
        LOG.trace("convertMatrixRoomIM2FHIRBundle(): Constructing Bundle");
        Bundle newCommunicationBundle = null;
        try{
            newCommunicationBundle = buildCommunicationBundle(newMessageHeader, generatedCommunication);
        } catch(Exception ex){
            unitOfWork.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            unitOfWork.setFailureDescription(ex.getMessage());
            return(unitOfWork);
        }
        LOG.trace("convertMatrixRoomIM2FHIRBundle(): Converting Bundle to String");
        String outputString = fhirResourceParser.encodeResourceToString(newCommunicationBundle);
        TopicToken bundleToken = fhirElementTopicIDBuilder.createTopicToken(ResourceType.Bundle.name(), systemWideProperties.getPegacornDefaultFHIRVersion());
        UoWPayload outputPayload = new UoWPayload(bundleToken,outputString);
        unitOfWork.getEgressContent().addPayloadElement(outputPayload);
        unitOfWork.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        LOG.debug(".convertMatrixRoomIM2FHIRBundle(): Exit, Resource --> {}", outputString);
        return (unitOfWork);
    }

    /**
     *
     * This function wraps FHIR::MessageHeader and FHIR::Communication element
     * set into a FHIR::Bundle.
     * <p>
     * Note that The bundles are always of type POST for FHIR::Communication
     * based FHIR::Bundles.
     *
     * @param newMessageHeader The FHIR::MessageHeader associated with the
     * FHIR::Communication element
     * @param newCommunication The FHIR::Communication message generated as part
     * of the transformation
     * @return Bundle: A singular FHIR::Bundle made up of the
     * FHIR::MessageHeader and FHIR::Communication elements.
     */
    private Bundle buildCommunicationBundle(MessageHeader newMessageHeader, Communication newCommunication)
            throws MatrixMessageException, JSONException, MajorTransformationException
    {
        LOG.debug("buildCommunicationBundle(): Entry, FHIR::MessageHeader --> {}, FHIR::Communication --> {}", newMessageHeader, newCommunication);

        // Before we do anything, let's confirm the (superficial) integrity of the incoming objects
        LOG.trace("buildCommunicationBundle(): Checking integrity of the incoming Matrix Instant Message");
        if (newMessageHeader == null) {
            LOG.error("buildCommunicationBundle: MessageHeader resource is null!!!");
            throw (new MajorTransformationException("wrapCommunicationBundle: FHIR::MessageHeader resource is null"));
        }
        if (newCommunication == null) {
            LOG.error("buildCommunicationBundle: Communication resource is null!!!");
            throw (new MajorTransformationException("wrapCommunicationBundle: FHIR::Communication resource is null"));
        }
        LOG.trace("buildCommunicationBundle(): Creating FHIR::Bundle and setting the FHIR::Bundle.BundleType");
        Bundle newBundleElement = new Bundle();
        newBundleElement.setType(Bundle.BundleType.MESSAGE);
        LOG.trace("buildCommunicationBundle(): Creating FHIR::BundleEntryComponent for FHIR::MessageHeader resource");
        Bundle.BundleEntryComponent bundleEntryForMessageHeaderElement = new Bundle.BundleEntryComponent();
        bundleEntryForMessageHeaderElement.setResource(newMessageHeader);
        LOG.trace("buildCommunicationBundle(): Creating FHIR::BundleEntryComponent for FHIR::Communication resource");
        Bundle.BundleEntryComponent bundleEntryForCommunicationElement = new Bundle.BundleEntryComponent();
        bundleEntryForCommunicationElement.setResource(newCommunication);
        LOG.trace("buildCommunicationBundle(): Creating Adding the MessageHeader BundleEntryComponent & Communication BundleEntryComponent to the Bundle resource");
        newBundleElement.addEntry(bundleEntryForMessageHeaderElement);
        newBundleElement.addEntry(bundleEntryForCommunicationElement);
        LOG.trace("buildCommunicationBundle(): Setting the message/bundle creation date");
        newBundleElement.setTimestamp(new Date());
        LOG.trace("buildCommunicationBundle(): Setting the message/bundle resource count - 2 in this case (i.e. MessageHeader + Communication resources");
        newBundleElement.setTotal(2);
        LOG.debug("wrapCommunicationBundle(): Exit, Created FHIR::Bundle --> {}", newBundleElement);
        return (newBundleElement);
    }
}

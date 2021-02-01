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
package net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.transform.matrix2fhir.beans.rooms.contentbuilders.RoomInfoName2Group;
import net.fhirfactory.pegacorn.petasos.model.processingplant.DefaultWorkshopSetEnum;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicTypeEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.MOAStandardWUP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */

@ApplicationScoped
public class MatrixRoomName2FHIRGroupWUP extends MOAStandardWUP {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixRoomName2FHIRGroupWUP.class);
    
    public MatrixRoomName2FHIRGroupWUP(){
        super();
    }

    @Override
    public Set<TopicToken> specifySubscriptionTopics() {
        LOG.debug(".getSubscribedTopics(): Entry");
        LOG.trace(".getSubscribedTopics(): Creating new TopicToken");
        FDN payloadTopicFDN = new FDN();
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_DEFINER.getTopicType(), "Matrix"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_CATEGORY.getTopicType(), "ClientServerAPI"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_SUBCATEGORY.getTopicType(), "InstantMessaging"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_RESOURCE.getTopicType(), "m.room.name"));
        TopicToken payloadTopicToken = new TopicToken();
        payloadTopicToken.setIdentifier(payloadTopicFDN.getToken());
        payloadTopicToken.setVersion("0.6.1");
        HashSet<TopicToken> myTopicsOfInterest = new HashSet<TopicToken>();
        myTopicsOfInterest.add(payloadTopicToken);
        LOG.debug("getSubscribedTopics(): Exit, myTopicsOfInterest --> {}", myTopicsOfInterest);
        return(myTopicsOfInterest);
    }

    @Override
    public String specifyWUPInstanceName() {
        return("MatrixRoomName2FHIRGroup");
    }

    @Override
    public String specifyWUPVersion() {
        return("0.0.1");
    }

    @Override
    public void configure() throws Exception {
        getLogger().debug(getClass().getSimpleName() + ":: + ingresFeed() --> {}", this.ingresFeed());
        getLogger().debug(getClass().getSimpleName() + ":: + egressFeed() --> {}", this.egressFeed());
        
        from(this.ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(RoomInfoName2Group.class, "matrixRoomNameEvent2FHIRGroupBundle")
                .to(this.egressFeed());
    }

    @Override
    protected Logger getLogger() {
        return (LOG);
    }

    @Override
    protected String specifyWUPWorkshop() {
        return (DefaultWorkshopSetEnum.TRANSFORM_WORKSHOP.getWorkshop());
    }
}

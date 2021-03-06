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
package net.fhirfactory.pegacorn.communicate.iris.wups.interact.ingres;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.communicate.iris.wups.interact.IncomingMatrixMessageSplitter;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicTypeEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.StandardWUP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class MatrixApplicationServicesEventsSeperatorWUP extends StandardWUP {

    private static final Logger LOG = LoggerFactory.getLogger(MatrixApplicationServicesEventsSeperatorWUP.class);

    public MatrixApplicationServicesEventsSeperatorWUP(){
        super();
    }
    
    @Override
    public Set<TopicToken> specifySubscriptionTopics() {
        LOG.debug(".getSubscribedTopics(): Entry");
        LOG.trace(".getSubscribedTopics(): Creating new TopicToken");
        FDN payloadTopicFDN = new FDN();
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_SECTOR.getTopicType(), "InformationTechnology"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_CATEGORY.getTopicType(), "CollaborationServices"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_DEFINER.getTopicType(), "Matrix"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_TOPIC_GROUP.getTopicType(), "ClientServerAPI"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_TOPIC.getTopicType(), "General"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_SUB_TOPIC.getTopicType(), "RawEventSet"));
        TopicToken payloadTopicToken = new TopicToken();
        payloadTopicToken.setIdentifier(payloadTopicFDN.getToken());
        payloadTopicToken.setVersion("1.0.0"); // TODO This version should be set & extracted somewhere
        HashSet<TopicToken> myTopicsOfInterest = new HashSet<TopicToken>();
        myTopicsOfInterest.add(payloadTopicToken);
        LOG.debug("getSubscribedTopics(): Exit, myTopicsOfInterest --> {}", myTopicsOfInterest);
        return(myTopicsOfInterest);
    }

    @Override
    public String specifyWUPInstanceName() {
        return("MatrixApplicationServicesEventsSeperatorWUP");
    }

    @Override
    public String specifyWUPVersion() {
        return("0.0.1");
    }

    @Override
    public void configure() throws Exception {
        LOG.debug(".configure(): Entry!, for wupFunctionToken --> {}, wupInstanceID --> {}", this.getWUPFunctionToken(), this.getWupInstanceID());
        
        from(this.ingresFeed())
                .bean(IncomingMatrixMessageSplitter.class, "splitMessageIntoEvents")
                .to(this.egressFeed());
    }

}

/*
 * The MIT License
 *
 * Copyright 2020 ACT Health.
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
package net.fhirfactory.pegacorn.communicate.fhirbridge.wups.interact.ingres.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.FDNToken;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.datasets.matrix.MatrixClientServiceAPIConstants;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicTypeEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayloadSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ACT Health
 */
@ApplicationScoped
public class IncomingMatrixEventSet2UoW
{
    private static final Logger LOG = LoggerFactory.getLogger(IncomingMatrixEventSet2UoW.class);

    @Inject
    private MatrixClientServiceAPIConstants matrixClientServiceAPIConstants;

    public UoW encapsulateMatrixMessage(String matrixMessage, NodeElementFunctionToken wupFunctionToken, FDNToken wupInstanceID)
    {
        LOG.debug(".encapsulateMatrixMessage(): Entry, Matrix Message --> {}, wupFunctionToken -> {}, wupInstanceID --> {}", matrixMessage, wupFunctionToken, wupInstanceID);
        LOG.trace(".encapsulateMatrixMessage(): Creating new Payload element, first the Payload TopicToken");
        FDN payloadTopicFDN = new FDN();
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_DEFINER.getTopicType(), "Matrix"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_CATEGORY.getTopicType(), "ClientServiceAPI"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_SUBCATEGORY.getTopicType(), "General"));
        payloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_RESOURCE.getTopicType(), "RawEventSet"));
        TopicToken payloadTopicToken = new TopicToken();
        payloadTopicToken.setIdentifier(payloadTopicFDN.getToken());
        payloadTopicToken.setVersion(matrixClientServiceAPIConstants.getMatrixClientServicesAPIRelease());
        LOG.trace(".encapsulateMatrixMessage(): Creating new Payload element, now the Payload itself");
        UoWPayload contentPayload = new UoWPayload(payloadTopicToken,matrixMessage );
        UoW newUoW = new UoW(contentPayload);
        LOG.debug("encapsulateMatrixMessage(): Exit, UoW created, newUoW --> {}", newUoW);
        return(newUoW);
    }
}

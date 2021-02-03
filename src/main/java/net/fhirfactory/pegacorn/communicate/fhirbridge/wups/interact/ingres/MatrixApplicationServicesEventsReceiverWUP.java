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

package net.fhirfactory.pegacorn.communicate.fhirbridge.wups.interact.ingres;

import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.interact.ingres.beans.IncomingMatrixEventSet2UoW;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.interact.ingres.beans.IncomingMatrixEventSetValidator;
import net.fhirfactory.pegacorn.communicate.fhirbridge.wups.interact.ingres.beans.IncomingMatrixMessageSplitter;
import net.fhirfactory.pegacorn.deployment.properties.communicate.CommunicateNames;
import net.fhirfactory.pegacorn.petasos.model.processingplant.DefaultWorkshopSetEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.InteractIngresMessagingGatewayWUP;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityBeginRegistration;
import org.apache.camel.ExchangePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MatrixApplicationServicesEventsReceiverWUP extends InteractIngresMessagingGatewayWUP {
    private static final Logger LOG = LoggerFactory.getLogger(MatrixApplicationServicesEventsReceiverWUP.class);

    @Inject
    private CommunicateNames pegacornCommunicateNames;
   
    @Override
    public void configure() throws Exception {
        LOG.info("MatrixApplicationServicesEventsReceiverWUP:: ingresFeed() --> {}", this.ingresFeed());
        LOG.info("MatrixApplicationServicesEventsReceiverWUP:: egressFeed() --> {}", this.egressFeed());

        from(this.ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .transform(simple("${bodyAs(String)}"))
                .bean(IncomingMatrixEventSetValidator.class, "validateEventSetMessage")
                .bean(IncomingMatrixEventSet2UoW.class, "encapsulateMatrixMessage")
                .bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange," + getWupTopologyNodeElement().extractNodeKey() + ")")
                .bean(IncomingMatrixMessageSplitter.class, "splitMessageIntoEvents")
                .to(ExchangePattern.InOnly, this.egressFeed())
                .transform().simple("{}")
                .end();
    }

    @Override
    public String specifyWUPInstanceName() {
        return("MatrixApplicationServicesEventsReceiverWUP");
    }

    @Override
    public String specifyWUPVersion() {
        return("1.0.0");
    }

    @Override
    protected Logger getLogger() {
        return (LOG);
    }

    @Override
    protected String specifyWUPWorkshop() {
        return (DefaultWorkshopSetEnum.INTERACT_WORKSHOP.getWorkshop());
    }

    @Override
    protected String specifyIngresTopologyEndpointName() {
        return (pegacornCommunicateNames.getEndpointFHIRBridgeMatrixAppServicesAPIPort());
    }

    @Override
    protected String specifyIngresEndpointVersion() {
        return (pegacornCommunicateNames.getFHIRBridgeProcessingPlantVersion());
    }

    @Override
    protected String specifyEndpointComponentDefinition() {
        return ("netty-http");
    }

    @Override
    protected String specifyEndpointProtocol() {
        return ("http");
    }

    @Override
    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    @Override
    protected String specifyEndpointProtocolLeadout() {
        return "/transactions/{id}";
    }
}

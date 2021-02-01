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

package net.fhirfactory.pegacorn.communicate.iris.wups.interact.ingres;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.communicate.iris.wups.interact.IncomingMatrixEventSetValidator;
import net.fhirfactory.pegacorn.communicate.iris.wups.interact.IncomingMatrixEventSet2UoW;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.InteractIngresMessagingGatewayWUP;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityBeginRegistration;
import org.apache.camel.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MatrixApplicationServicesEventsReceiverWUP extends InteractIngresMessagingGatewayWUP {
    private static final Logger LOG = LoggerFactory.getLogger(MatrixApplicationServicesEventsReceiverWUP.class);

    private static String INGRES_GATEWAY_COMPONENT = "netty-http";
    
    public MatrixApplicationServicesEventsReceiverWUP(){
        super();
    }
   
    @Override
    public void configure() throws Exception {

        LOG.debug(".configure(): Matrix Instant Message Room Notification Handler (RoomServer --> Iris) Endpoint = " + this.ingresFeed());
        
        LOG.info(".configure(): getWupIngresPoint --> {}", this.getWupIngresPoint());

        from(this.getWupIngresPoint())
                .routeId("MatrixEvents2FHIR-RoomServer2Iris-Route -->")
                .transform(simple("${bodyAs(String)}"))
                .log(LoggingLevel.TRACE, "Message received!!!")
                .bean(IncomingMatrixEventSetValidator.class, "validateEventSetMessage")
                .bean(IncomingMatrixEventSet2UoW.class, "encapsulateMatrixMessage(*, " + this.getWUPFunctionToken() + "," + this.getWupInstanceID() + ")")
                .bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange," + this.getWUPFunctionToken() + "," + this.getWupInstanceID() + ")")
                .log(LoggingLevel.TRACE, "Message Validated, Forwarding!!!")
                .to(this.egressFeed())
                .end();
    }



    @Override
    public String specifyWUPInstanceName() {
        return("MatrixApplicationServicesEventsReceiverWUP");
    }

    @Override
    public String specifyWUPVersion() {
        return("0.0.1");
    }
    
    @Override
    protected String getEndpointComponentDefinition() {
        return("netty-http");
    }

    @Override
    protected String getEndpointProtocol() {
        return("http");
    }

    @Override
    protected String getEndpointProtocolLeadIn() {
        return("://");
    }

    @Override
    protected String getEndpointProtocolLeadout() {
        return("/");
    }
}

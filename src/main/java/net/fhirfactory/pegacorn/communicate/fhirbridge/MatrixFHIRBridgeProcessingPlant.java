package net.fhirfactory.pegacorn.communicate.fhirbridge;

import net.fhirfactory.pegacorn.deployment.properties.communicate.CommunicateNames;
import net.fhirfactory.pegacorn.processingplatform.EdgeSubsystemProcessingPlatform;

import javax.inject.Inject;

public abstract class MatrixFHIRBridgeProcessingPlant extends EdgeSubsystemProcessingPlatform {

    @Inject
    private CommunicateNames pegacornCommunicateNames;

    protected CommunicateNames getPegacornCommunicateComponentNames(){
        getLogger().debug(".getPegacornCommunicateComponentNames(): Entry/Exit");
        return(pegacornCommunicateNames);
    }

}

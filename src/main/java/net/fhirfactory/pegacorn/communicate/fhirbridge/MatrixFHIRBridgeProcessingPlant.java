package net.fhirfactory.pegacorn.communicate.fhirbridge;

import net.fhirfactory.pegacorn.deployment.names.PegacornCommunicateComponentNames;
import net.fhirfactory.pegacorn.processingplatform.EdgeSubsystemProcessingPlatform;

import javax.inject.Inject;

public abstract class MatrixFHIRBridgeProcessingPlant extends EdgeSubsystemProcessingPlatform {

    @Inject
    private PegacornCommunicateComponentNames pegacornCommunicateComponentNames;

    protected PegacornCommunicateComponentNames getPegacornCommunicateComponentNames(){
        getLogger().debug(".getPegacornCommunicateComponentNames(): Entry/Exit");
        return(pegacornCommunicateComponentNames);
    }

}

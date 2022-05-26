/*
 * Copyright (c) 2020 Mark A. Hunter
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
package net.fhirfactory.pegacorn.communicate.iris.fhirbridge.workshops.transform.resources.group.transformers;

import net.fhirfactory.pegacorn.internals.communicate.entities.rooms.valuesets.CommunicateRoomTypeEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.group.valuesets.GroupCodeValueSet;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GroupCRTypeChecker {
    private static final Logger LOG = LoggerFactory.getLogger(GroupCRTypeChecker.class);

    @Inject
    private GroupCodeValueSet groupCodeValueSet;

    /**
     * The methods extracts the FHIR::Group.code value (a CodeableConcept) and ascertains if the
     * (one of) the FHIR::Group.code.coding[] types is set to the GroupCodeValueSet.getCommunicateRoomCodeSystem() value.
     * If it is, it returns that value (converted to the appropriate enum), if not, it returns null.
     *
     * @param group The FHIR::Group resource to be tested and Communicate Group Type enum extracted from.
     * @return Either an enum (CommunicateRoomTypeEnum) or null if the FHIR::Group does not contain a Communicate Group Type value.
     */
    public CommunicateRoomTypeEnum getCommunicateGroupType(Group group){
        LOG.debug(".getCommunicateGroupType(): Entry, group->{}", group);
        if(!group.hasCode()){
            LOG.debug(".getCommunicateGroupType(): Exit, does not contain a FHIR::Group.code attribute, returning null");
            return(null);
        }
        CodeableConcept groupMemberKind = group.getCode();
        for(Coding currentCode: groupMemberKind.getCoding()){
            if(currentCode.getSystem().contentEquals(groupCodeValueSet.getCommunicateRoomCodeSystem())){
                String codeValue = currentCode.getCode();
                CommunicateRoomTypeEnum roomType = CommunicateRoomTypeEnum.fromRoomTypeString(codeValue);
                LOG.debug(".getCommunicateGroupType(): Exit, roomType->{}", roomType);
                return(roomType);
            }
        }
        LOG.debug(".getCommunicateGroupType(): Exit, Communicate based room type not found, returning null");
        return(null);
    }
}

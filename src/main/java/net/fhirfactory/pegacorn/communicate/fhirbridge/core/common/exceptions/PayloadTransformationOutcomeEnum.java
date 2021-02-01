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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.fhirfactory.pegacorn.communicate.iris.core.common.exceptions;

/**
 *
 * @author mhunter
 */
public enum PayloadTransformationOutcomeEnum {
    PAYLOAD_TRANSFORM_SUCCESSFUL("pegacorn.communicate.iris.transforms.outcome.success"),
    PAYLOAD_TRANSFORM_FAILURE("pegacorn.communicate.iris.transforms.outcome.failure"),
    PAYLOAD_TRANSFORM_FAILURE_INGRES_CONTENT_MALFORMED("pegacorn.communicate.iris.transforms.outcome.failure.content_malformed"),
    PAYLOAD_TRANSFORM_FAILURE_INGRES_CONTENT_INCOMPLETE("pegacorn.communicate.iris.transforms.outcome.failure.ingres_content_incomplete"),
    PAYLOAD_TRANSFORM_FAILURE_EGRES_CONTENT_INSUFFICIENT("pegacorn.communicate.iris.transforms.outcome.failure.egres_content_insufficient");
    
    private String payloadTransformationOutcome;
    
    private PayloadTransformationOutcomeEnum(String outcome ){
        this.payloadTransformationOutcome = outcome;
    }
    
    public String getPayloadTransformationOutcome(){
        return(this.payloadTransformationOutcome);
    } 
}

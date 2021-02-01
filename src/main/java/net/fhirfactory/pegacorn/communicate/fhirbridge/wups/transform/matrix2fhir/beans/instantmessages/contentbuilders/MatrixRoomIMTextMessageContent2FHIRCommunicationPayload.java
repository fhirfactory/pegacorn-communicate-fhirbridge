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
package net.fhirfactory.pegacorn.communicate.fhirbridge.core.matrxi2fhir.instantmessaging.contentbuilders;

import javax.enterprise.context.ApplicationScoped;
import net.fhirfactory.pegacorn.communicate.fhirbridge.core.common.exceptions.MatrixMessageException;
import org.hl7.fhir.r4.model.Communication.CommunicationPayloadComponent;
import org.hl7.fhir.r4.model.StringType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter (ACT Health)
 */
@ApplicationScoped
public class MatrixRoomIMTextMessageContent2FHIRCommunicationPayload
{

    private static final Logger LOG = LoggerFactory.getLogger(MatrixRoomIMTextMessageContent2FHIRCommunicationPayload.class);

    public CommunicationPayloadComponent buildTextPayload(JSONObject roomIMContent)
            throws MatrixMessageException, JSONException
    {
        LOG.debug("buildTextPayload(): Entry, roomIMContent --> {}", roomIMContent);
        if (roomIMContent == null) {
            throw (new MatrixMessageException("buildTextPayload(): Message is null"));
        }
        if (roomIMContent == null) {
            throw (new MatrixMessageException("buildTextPayload(): Message is empty"));
        }
        LOG.trace("buildTextPayload(): Creating empty CommunicationPayloadComponent");
        CommunicationPayloadComponent payload = new CommunicationPayloadComponent();
        payload.setContent(new StringType(roomIMContent.toString()));
        LOG.debug("buildMTextPayload(): Exit, payload = {}", payload);
        return (payload);
    }
}

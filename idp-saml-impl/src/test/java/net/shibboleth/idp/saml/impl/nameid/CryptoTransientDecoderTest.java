/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.saml.impl.nameid;

import java.io.IOException;

import net.shibboleth.idp.authn.SubjectCanonicalizationException;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.DataSealer;
import net.shibboleth.utilities.java.support.security.DataSealerException;

import org.opensaml.profile.ProfileException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * test for {@link CryptoTransientDecoder}.
 */
public class CryptoTransientDecoderTest {

    private final static long TIMEOUT = 5000;

    private final static String PRINCIPAL = "ThePrincipal";

    private final static String ISSUER = "https://idp.example.org/issuer";

    private final static String RECIPIENT = "https://sp.example.org/recipient";

    private DataSealer dataSealer;

    private CryptoTransientDecoder decoder;

    /**
     * Set up the data sealer. We take advantage of the fact that Spring a {@link ClassPathResource} wraps a files.
     * 
     * @throws IOException
     * @throws DataSealerException
     * @throws ComponentInitializationException
     */
    @BeforeClass public void setupDataSealer() throws IOException, DataSealerException,
            ComponentInitializationException {

        final Resource keyStore = new ClassPathResource("SealerKeyStore.jks");
        Assert.assertTrue(keyStore.exists());

        final String keyStorePath = keyStore.getFile().getAbsolutePath();

        dataSealer = new DataSealer();
        dataSealer.setCipherKeyAlias("secret");
        dataSealer.setCipherKeyPassword("kpassword");

        dataSealer.setKeystorePassword("password");
        dataSealer.setKeystorePath(keyStorePath);

        dataSealer.initialize();

        decoder = new CryptoTransientDecoder();
        decoder.setDataSealer(dataSealer);
        decoder.setId("Decoder");
        decoder.initialize();
    }

    private String code(String principalName, String attributeIssuerID, String attributeRecipientID, long timeout)
            throws DataSealerException {
        final String principalTokenId =
                new StringBuilder().append(attributeIssuerID).append("!").append(attributeRecipientID).append("!")
                        .append(principalName).toString();
        return dataSealer.wrap(principalTokenId, System.currentTimeMillis() + timeout);
    }

    private String code(String principalName, String attributeIssuerID, String attributeRecipientID)
            throws DataSealerException {
        return code(principalName, attributeIssuerID, attributeRecipientID, TIMEOUT);
    }

    @Test public void testSucess() throws ProfileException, ComponentInitializationException, IOException,
            DataSealerException {
        String ct = code(PRINCIPAL, ISSUER, RECIPIENT);

        Assert.assertEquals(decoder.decode(ct, ISSUER, RECIPIENT), PRINCIPAL);
    }

    @Test(expectedExceptions = {NameDecoderException.class,}) public void noTransient()
            throws SubjectCanonicalizationException, NameDecoderException {
        decoder.decode(null, ISSUER, RECIPIENT);
    }

    @Test(expectedExceptions = {NameDecoderException.class,}) public void timeout()
            throws SubjectCanonicalizationException, DataSealerException, NameDecoderException {
        String ct = code(PRINCIPAL, ISSUER, RECIPIENT, -10);

        decoder.decode(ct, ISSUER, RECIPIENT);
    }

    @Test(expectedExceptions = {NameDecoderException.class,}) public void baddata()
            throws SubjectCanonicalizationException, DataSealerException, NameDecoderException {
        String ct = code(PRINCIPAL, ISSUER, RECIPIENT);

        decoder.decode(ct.toUpperCase(), ISSUER, RECIPIENT);
    }

    @Test(expectedExceptions = {SubjectCanonicalizationException.class,}) public void baddata2()
            throws SubjectCanonicalizationException, DataSealerException, NameDecoderException {

        final String principalTokenId =
                new StringBuilder().append(ISSUER).append("!").append(RECIPIENT).append("+").append(PRINCIPAL)
                        .toString();
        String ct = dataSealer.wrap(principalTokenId, System.currentTimeMillis() + TIMEOUT);

        decoder.decode(ct, ISSUER, RECIPIENT);
    }

    @Test(expectedExceptions = {SubjectCanonicalizationException.class,}) public void badSP()
            throws SubjectCanonicalizationException, DataSealerException, NameDecoderException {
        String ct = code(PRINCIPAL, ISSUER, RECIPIENT);

        decoder.decode(ct + ct, ISSUER, "my" + RECIPIENT);
    }

    @Test(expectedExceptions = {SubjectCanonicalizationException.class,}) public void badIdP()
            throws SubjectCanonicalizationException, DataSealerException, NameDecoderException {
        String ct = code(PRINCIPAL, ISSUER, RECIPIENT);

        decoder.decode(ct + ct, "my" + ISSUER, RECIPIENT);
    }

}

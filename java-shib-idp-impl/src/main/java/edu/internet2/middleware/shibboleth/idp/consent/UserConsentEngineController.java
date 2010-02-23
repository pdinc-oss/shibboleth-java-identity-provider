/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.idp.consent;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.internet2.middleware.shibboleth.idp.consent.entities.Attribute;
import edu.internet2.middleware.shibboleth.idp.consent.entities.Principal;
import edu.internet2.middleware.shibboleth.idp.consent.entities.RelyingParty;
import edu.internet2.middleware.shibboleth.idp.consent.entities.TermsOfUse;
import edu.internet2.middleware.shibboleth.idp.consent.logic.AttributeList;
import edu.internet2.middleware.shibboleth.idp.consent.logic.RelyingPartyBlacklist;
import edu.internet2.middleware.shibboleth.idp.consent.logic.UserConsentContext;
import edu.internet2.middleware.shibboleth.idp.consent.logic.UserConsentContextBuilder;
import edu.internet2.middleware.shibboleth.idp.consent.mock.ProfileContext;

/**
 *
 */

@Controller
@RequestMapping("/userconsent/")
public class UserConsentEngineController {

    private final Logger logger = LoggerFactory.getLogger(UserConsentEngineController.class);

    @Autowired
    private TermsOfUse termsOfUse;
    
    @Autowired
    private RelyingPartyBlacklist relyingPartyBlacklist;
    
    @Autowired
    private AttributeList attributeList;
    
    @Autowired
    private UserConsentContextBuilder userConsentContextBuilder;
    
    @RequestMapping(method = RequestMethod.GET)
    public String service(ProfileContext profileContext) throws UserConsentException {        
        
        UserConsentContext userConsentContext;
        try {
            userConsentContext = userConsentContextBuilder.buildUserConsentContext(profileContext);
        } catch (UserConsentException e) {
            logger.error("Error building user consent context", e);
            throw e;
        }
        
        Principal principal = userConsentContext.getPrincipal();
        RelyingParty relyingParty = userConsentContext.getRelyingParty();
        
        if (termsOfUse != null && !principal.hasAcceptedTermsOfUse(termsOfUse)) {
            logger.info("{} has not accepted {}", principal, termsOfUse);
            return "redirect:/userconsent/tou";
        }
        
        if (userConsentContext.isConsentRevocationRequested()) {
            principal.setGlobalConsent(false);
            principal.setAttributeReleaseConsents(relyingParty, null);            
            // TODO: log into audit log
        }
        
        if (principal.hasGlobalConsent()) {
            return "redirect:/idp";
        }
        
        if (relyingPartyBlacklist.contains(relyingParty)) {
            return "redirect:/idp";
        }
        
        Collection<Attribute> attributes = userConsentContext.getAttributesToBeReleased();
        attributes = attributeList.removeBlacklisted(attributes);  
        if (!principal.hasApproved(attributes, relyingParty)) {
            return "redirect:/userconsent/attributerelease";
        }
        
        return "redirect:/idp";
    }
}

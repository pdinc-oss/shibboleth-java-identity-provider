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

package net.shibboleth.idp.session.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.TestPrincipal;
import net.shibboleth.idp.authn.UsernamePrincipal;
import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.SPSessionSerializerRegistry;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.criterion.SPSessionCriterion;
import net.shibboleth.idp.session.criterion.SessionIdCriterion;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;

import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/** {@link StorageBackedSessionManager} unit test. */
public class StorageBackedSessionManagerTest {

    private static final long sessionSlop = 60 * 5 * 1000;
    
    private static MemoryStorageService storageService;
    
    private static StorageBackedSessionManager manager;

    private static Collection<AuthenticationFlowDescriptor> flowDescriptors;
    
    private static SPSessionSerializerRegistry serializerRegistry;
    
    @BeforeClass public static void setUp() throws ComponentInitializationException {
        storageService = new MemoryStorageService();
        storageService.initialize();

        serializerRegistry = new SPSessionSerializerRegistry();
        serializerRegistry.register(BasicSPSession.class, new BasicSPSessionSerializer(sessionSlop));
        serializerRegistry.register(ExtendedSPSession.class, new ExtendedSPSessionSerializer(sessionSlop));
        
        AuthenticationFlowDescriptor foo = new AuthenticationFlowDescriptor("AuthenticationFlow/Foo");
        foo.setLifetime(60 * 1000);
        foo.setInactivityTimeout(60 * 1000);
        AuthenticationFlowDescriptor bar = new AuthenticationFlowDescriptor("AuthenticationFlow/Bar");
        bar.setLifetime(60 * 1000);
        bar.setInactivityTimeout(60 * 1000);
        flowDescriptors = Arrays.asList(foo, bar);
        
        manager = new StorageBackedSessionManager();
        manager.setAuthenticationFlowDescriptors(flowDescriptors);
        manager.setTrackSPSessions(true);
        manager.setSecondaryServiceIndex(true);
        manager.setStorageService(storageService);
        manager.setSessionSlop(sessionSlop);
        manager.setIDGenerator(new SecureRandomIdentifierGenerationStrategy());
        manager.setSPSessionSerializerRegistry(serializerRegistry);
        manager.initialize();
    }
    
    @AfterClass public static void tearDown() {
        manager.destroy();
        storageService.destroy();
    }

    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testSimpleSession() throws ResolverException, SessionException, InterruptedException {
        
        // Test a failed lookup.
        Assert.assertNull(manager.resolveSingle(new CriteriaSet(new SessionIdCriterion("test"))));
        
        // Username should be required.
        try {
            manager.createSession(null, null);
            Assert.fail("A null username should not have worked");
        } catch (ConstraintViolationException e) {
            
        }
        
        // Test basic session content.
        IdPSession session = manager.createSession("joe", null);
        Assert.assertTrue(session.getCreationInstant() <= System.currentTimeMillis());
        Assert.assertEquals(session.getCreationInstant(), session.getLastActivityInstant());
        Assert.assertEquals(session.getPrincipalName(), "joe");
        Assert.assertTrue(session.getAuthenticationResults().isEmpty());
        Assert.assertTrue(session.getSPSessions().isEmpty());
        
        Thread.sleep(1000);
        
        // checkTimeout should update the last activity time.
        session.checkTimeout();
        Assert.assertNotEquals(session.getCreationInstant(), session.getLastActivityInstant());

        // Do a lookup and compare the results.
        long creation = session.getCreationInstant();
        long lastActivity = session.getLastActivityInstant();
        String sessionId = session.getId();
        session = manager.resolveSingle(new CriteriaSet(new SessionIdCriterion(sessionId)));
        Assert.assertNotNull(session);
        Assert.assertEquals(session.getPrincipalName(), "joe");
        Assert.assertEquals(session.getCreationInstant(), creation);
        Assert.assertEquals(session.getLastActivityInstant(), lastActivity);
        
        // Test a destroy and a failed lookup.
        manager.destroySession(sessionId);
        Assert.assertNull(manager.resolveSingle(new CriteriaSet(new SessionIdCriterion(sessionId))));
    }
    
    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testAddress() throws SessionException, ResolverException {
        
        // Interleave checks of addresses of the two types.
        IdPSession session = manager.createSession("joe", "192.168.1.1");
        Assert.assertTrue(session.checkAddress("192.168.1.1"));
        Assert.assertFalse(session.checkAddress("192.168.1.2"));
        Assert.assertTrue(session.checkAddress("fe80::ca2a:14ff:fe2a:3e04"));
        Assert.assertTrue(session.checkAddress("fe80::ca2a:14ff:fe2a:3e04"));
        Assert.assertFalse(session.checkAddress("fe80::ca2a:14ff:fe2a:3e05"));
        Assert.assertTrue(session.checkAddress("192.168.1.1"));
        
        // Try a bad address type.
        Assert.assertFalse(session.checkAddress("1,1,1,1"));
        
        // Interleave manipulation of a session between two copies to check for resync.
        IdPSession one = manager.createSession("joe", null);
        IdPSession two = manager.resolveSingle(new CriteriaSet(new SessionIdCriterion(one.getId())));
        
        Assert.assertTrue(one.checkAddress("192.168.1.1"));
        Assert.assertFalse(two.checkAddress("192.168.1.2"));
        Assert.assertTrue(two.checkAddress("fe80::ca2a:14ff:fe2a:3e04"));
        Assert.assertFalse(one.checkAddress("fe80::ca2a:14ff:fe2a:3e05"));
        
        manager.destroySession(session.getId());
    }

    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testAuthenticationResults() throws ResolverException, SessionException, InterruptedException {
        
        IdPSession session = manager.createSession("joe", null);
        Assert.assertTrue(session.getAuthenticationResults().isEmpty());

        // Add some results.
        AuthenticationResult foo = new AuthenticationResult("AuthenticationFlow/Foo", new UsernamePrincipal("joe"));
        foo.getSubject().getPrincipals().add(new TestPrincipal("test1"));
        AuthenticationResult bar = new AuthenticationResult("AuthenticationFlow/Bar", new UsernamePrincipal("joe"));
        bar.getSubject().getPrincipals().add(new TestPrincipal("test2"));
        AuthenticationResult baz = new AuthenticationResult("AuthenticationFlow/Baz", new UsernamePrincipal("joe"));

        Assert.assertNull(session.addAuthenticationResult(foo));
        Assert.assertNull(session.addAuthenticationResult(bar));
        try {
            session.addAuthenticationResult(baz);
            Assert.fail("An unserializable AuthenticationResult should not have worked");
        } catch (SessionException e) {
            
        }
        
        // Test various methods and removals.
        Assert.assertEquals(session.getAuthenticationResults().size(), 2);
        
        Assert.assertFalse(session.removeAuthenticationResult(baz));
        Assert.assertTrue(session.removeAuthenticationResult(bar));
        
        Assert.assertEquals(session.getAuthenticationResults().size(), 1);
        
        // Test access and compare to original.
        Assert.assertNull(session.getAuthenticationResult("AuthenticationFlow/Bar"));
        AuthenticationResult foo2 = session.getAuthenticationResult("AuthenticationFlow/Foo");
        Assert.assertNotNull(foo2);
        Assert.assertEquals(foo.getAuthenticationInstant(), foo2.getAuthenticationInstant());
        Assert.assertEquals(foo.getLastActivityInstant(), foo2.getLastActivityInstant());
        Assert.assertEquals(foo.getSubject(), foo2.getSubject());
        
        // Load from storage and re-test.
        IdPSession session2 = manager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertNull(session2.getAuthenticationResult("AuthenticationFlow/Bar"));
        foo2 = session2.getAuthenticationResult("AuthenticationFlow/Foo");
        Assert.assertNotNull(foo2);
        Assert.assertEquals(foo.getAuthenticationInstant(), foo2.getAuthenticationInstant());
        Assert.assertEquals(foo.getLastActivityInstant(), foo2.getLastActivityInstant());
        Assert.assertEquals(foo.getSubject(), foo2.getSubject());
        
        // Test removal while multiple objects are active.
        session2 = manager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertTrue(session.removeAuthenticationResult(foo));
        Assert.assertNull(session2.getAuthenticationResult("AuthenticationFlow/Foo"));
        
        manager.destroySession(session.getId());
    }
    
    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testSPSessions() throws ResolverException, SessionException, InterruptedException {
        
        IdPSession session = manager.createSession("joe", null);
        Assert.assertTrue(session.getSPSessions().isEmpty());

        // Add some sessions.
        SPSession foo = new BasicSPSession("https://sp.example.org/shibboleth", "AuthenticationFlow/Foo",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);
        SPSession bar = new BasicSPSession("https://sp2.example.org/shibboleth", "AuthenticationFlow/Bar",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);

        Assert.assertNull(session.addSPSession(foo));
        Assert.assertNull(session.addSPSession(bar));
        
        // Test various methods and removals.
        Assert.assertEquals(session.getSPSessions().size(), 2);
        
        Assert.assertTrue(session.removeSPSession(bar));
        Assert.assertFalse(session.removeSPSession(bar));
        
        Assert.assertEquals(session.getSPSessions().size(), 1);
        
        // Test access and compare to original.
        Assert.assertNull(session.getSPSession("https://sp2.example.org/shibboleth"));
        SPSession foo2 = session.getSPSession("https://sp.example.org/shibboleth");
        Assert.assertNotNull(foo2);
        Assert.assertEquals(foo.getCreationInstant(), foo2.getCreationInstant());
        Assert.assertEquals(foo.getExpirationInstant(), foo2.getExpirationInstant());
        
        // Load from storage and re-test.
        IdPSession session2 = manager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertNull(session.getSPSession("https://sp2.example.org/shibboleth"));
        foo2 = session2.getSPSession("https://sp.example.org/shibboleth");
        Assert.assertNotNull(foo2);
        Assert.assertEquals(foo.getCreationInstant(), foo2.getCreationInstant());
        Assert.assertEquals(foo.getExpirationInstant(), foo2.getExpirationInstant());

        // Test removal while multiple objects are active.
        session2 = manager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertTrue(session.removeSPSession(foo));
        Assert.assertNull(session2.getSPSession("https://sp.example.org/shibboleth"));
        
        manager.destroySession(session.getId());
    }
    
    @Test
    public void testSecondaryLookup() throws ResolverException, SessionException, InterruptedException {
        
        IdPSession session = manager.createSession("joe", null);
        IdPSession session2 = manager.createSession("joe2", null);

        // Add some sessions.
        SPSession foo = new ExtendedSPSession("https://sp.example.org/shibboleth", "AuthenticationFlow/Foo",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);
        SPSession bar = new ExtendedSPSession("https://sp2.example.org/shibboleth", "AuthenticationFlow/Bar",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);

        Assert.assertNull(session.addSPSession(foo));
        Assert.assertNull(session.addSPSession(bar));

        Assert.assertNull(session2.addSPSession(foo));
        Assert.assertNull(session2.addSPSession(bar));
        
        // Do a lookup.
        Assert.assertFalse(manager.resolve(new CriteriaSet(
                new SPSessionCriterion("https://sp.example.org/shibboleth", "None"))).iterator().hasNext());
        
        List<IdPSession> sessions = Lists.newArrayList(manager.resolve(
                new CriteriaSet(new SPSessionCriterion("https://sp.example.org/shibboleth",
                        ExtendedSPSession.SESSION_KEY))));
        Assert.assertEquals(sessions.size(), 2);
        
        manager.destroySession(session.getId());
        
        sessions = Lists.newArrayList(manager.resolve(
                new CriteriaSet(new SPSessionCriterion("https://sp2.example.org/shibboleth",
                        ExtendedSPSession.SESSION_KEY))));
        Assert.assertEquals(sessions.size(), 1);
        
        manager.destroySession(session2.getId());
        sessions = Lists.newArrayList(manager.resolve(
                new CriteriaSet(new SPSessionCriterion("https://sp2.example.org/shibboleth",
                        ExtendedSPSession.SESSION_KEY))));
        Assert.assertEquals(sessions.size(), 0);
    }

    private static class ExtendedSPSession extends BasicSPSession {

        public static final String SESSION_KEY = "PerSessionNameWouldGoHere";
        
        public ExtendedSPSession(String id, String flowId, long creation, long expiration) {
            super(id, flowId, creation, expiration);
        }

        /** {@inheritDoc} */
        public String getSPSessionKey() {
            return SESSION_KEY;
        }
    }

    private static class ExtendedSPSessionSerializer extends BasicSPSessionSerializer {

        public ExtendedSPSessionSerializer(long offset) {
            super(offset);
        }
        
        /** {@inheritDoc} */
        protected SPSession doDeserialize(JsonObject obj, String id, String flowId, long creation, long expiration)
                throws IOException {
            // Check if field got serialized.
            obj.getString("sk");
            return new ExtendedSPSession(id, flowId, creation, expiration);
        }

        /** {@inheritDoc} */
        protected void doSerializeAdditional(SPSession instance, JsonGenerator generator) {
            generator.write("sk", ExtendedSPSession.SESSION_KEY);
        }

    }
}
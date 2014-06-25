package org.backmeup.logic.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.impl.helper.AuthenticationPerformer;
import org.backmeup.logic.impl.helper.DropboxAutomaticAuthorizer;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

//TODO: Remove plugin based tests from this class; they should be performed from within python or within the plugin itself!
@Ignore("just compiles, probably did not work for long time")
public class BusinessLogicImplTest {

    private static BusinessLogic logic;

    @BeforeClass
    public static void setUp() {
        // ApplicationContext context = new ClassPathXmlApplicationContext( new String[] { "spring.xml" }); 
        // logic = context.getBean(BusinessLogic.class);

        // Weld weld = new Weld();
        // WeldContainer container = weld.initialize();
        // logic = container.instance().select(BusinessLogic.class).get();
    }

    @AfterClass
    public static void tearDown() {
        logic.shutdown();
    }

    @Test
    public void testSomething() throws IOException {
        // your plugins describable returns e.g.: org.backmeup.moodle 
        try {
//            logic.register("fjungwirth", "apassword", "apassword", "fjungwirth@something.at");
        } catch (AlreadyRegisteredException are) {
        }

        // register moodle datasource 
        AuthRequest ar = logic.preAuth("fjungwirth", "org.backmeup.dropbox", "My Dropbox-Source Profile", "apassword");

        System.out.println("Open the following URL: " + ar.getRedirectURL());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // print ar to shell if needed 
        Properties props = new Properties();
        System.out.print("code: ");
        props.setProperty("code", br.readLine());
        logic.postAuth(ar.getProfile().getProfileId(), props, "apassword");

        // register skydrive datasink 
        AuthRequest ar2 = logic.preAuth("fjungwirth", "org.backmeup.skydrive", "My Skydrive-Sink Profile", "apassword");
        // print ar2 to shell if needed
        System.out.println("Open the following URL: " + ar2.getRedirectURL());
        props = new Properties();
        System.out.print("code: ");
        String code = br.readLine();
        props.setProperty("code", code);
        logic.postAuth(ar2.getProfile().getProfileId(), props, "apassword");

//        List<SourceProfileEntry> sources = new ArrayList<>();
//        sources.add(new SourceProfileEntry(ar.getProfile().getProfileId()));
//
//        // create and exceute a backupjob from moodle to skydrive
//        JobCreationRequest r = new JobCreationRequest();
//        r.setSourceProfiles(sources);
//        r.setSinkProfileId(ar2.getProfile().getProfileId());
//        r.setTimeExpression("now");
//        r.setKeyRing("apassword");
//        
//        logic.createBackupJob("fjungwirth", r);
    }

    @Test
    public void testGetUser() {
        try {
            logic.deleteUser("Seppl");
        } catch (Exception e) {
        }
//        BackMeUpUser u = logic.register("Seppl", "12345678", "12345678", "backmeup1@trash-mail.com");
//        logic.verifyEmailAddress(u.getVerificationKey());
//        BackMeUpUser u2 = logic.getUser("Seppl");

//        assertNotNull(u);
//        assertNotNull(u2);
//        assertNotNull(u2.getUserId());
//        assertNotNull(u.getUserId());
//        assertEquals(u.getUserId(), u2.getUserId());
//        assertEquals(u.getUsername(), u2.getUsername());
//        assertEquals(u.getEmail(), u2.getEmail());
    }

    @Test
    public void testRegister() {
        try {
            logic.deleteUser("Seppl");
        } catch (Exception e) {

        }
//        BackMeUpUser u = logic.register("Seppl", "superlongpassword", "superlongpassword", "backmeup1@trash-mail.com");
//        assertNotNull(u);
//        assertEquals("Seppl", u.getUsername());
//        assertEquals("backmeup1@trash-mail.com", u.getEmail());
//
//        try {
//            u = logic.register("James", "123", "12345678", "backmeup1@trash-mail.com");
//            fail("Should not be reached!");
//        } catch (PasswordTooShortException pts) {
//        }
//
//        try {
//            u = logic.register("James", "12345678", "123", "backmeup1@trash-mail.com");
//            fail("Should not be reached!");
//        } catch (PasswordTooShortException pts) {
//        }
//
//        try {
//            u = logic.register("James", "12345678", "12345678", "invalidmailmail.at");
//            fail("Should not be reached!");
//        } catch (NotAnEmailAddressException pts) {
//        }
//
//        try {
//            u = logic.register("James", "12345678", "12345678", "invalidmailmail.@at");
//            fail("Should not be reached!");
//        } catch (NotAnEmailAddressException pts) {
//        }
//
//        try {
//            u = logic.register("James", "12345678", "12345678", "invalidmailmail@at");
//            fail("Should not be reached!");
//        } catch (NotAnEmailAddressException pts) {
//        }
//
//        try {
//            u = logic.register("James", "12345678", "12345678", "invalidmailmail@.at");
//            fail("Should not be reached!");
//        } catch (NotAnEmailAddressException pts) {
//        }
//
//        try {
//            u = logic.register(null, "12345678", "12345678", "invalidmailmail@.at");
//            fail("Should not be reached!");
//        } catch (IllegalArgumentException iae) {
//        }
//
//        try {
//            u = logic.register("James", null, "12345678", "invalidmailmail@.at");
//            fail("Should not be reached!");
//        } catch (IllegalArgumentException iae) {
//        }
//
//        try {
//            u = logic.register("James", "12345678", null, "invalidmailmail@.at");
//            fail("Should not be reached!");
//        } catch (IllegalArgumentException iae) {
//        }
//
//        try {
//            u = logic.register("James", "12345678", "invalidmailmail@.at", null);
//            fail("Should not be reached!");
//        } catch (IllegalArgumentException iae) {
//        }
    }

    @Test
    public void testGetDatasources() {
        List<SourceSinkDescribable> describables = logic.getDatasources();
        for (SourceSinkDescribable ssd : describables) {
            assertNotNull(ssd.getTitle());
            assertNotNull(ssd.getDescription());
            assertNotNull(ssd.getType());
            assertNotNull(ssd.getId());
            assertNotNull(ssd.getImageURL());
        }
    }

    @Test
    public void testCreateBackupJob() {
        BackMeUpUser u = null;
//        try {
//            u = logic.register("backuper", "hi", "hi", "amail");
//        } catch (AlreadyRegisteredException are) {
//            u = logic.getUser("backuper");
//        }

        List<SourceSinkDescribable> sources = logic.getDatasources();
        assertTrue(sources.size() > 0);

        List<SourceSinkDescribable> sinks = logic.getDatasinks();
        assertTrue(sinks.size() > 0);

        System.out.println("Register source:");
        AuthRequest ar = logic.preAuth(u.getUsername(), "org.backmeup.dropbox", "Dropbox-Profile", "hi");
        String url = ar.getRedirectURL();
        System.out.println(url);
        String data = AuthenticationPerformer.performAuthentication(url, new DropboxAutomaticAuthorizer());
        Properties p = new Properties();
        String[] entries = data.split("&");
        for (String entry : entries) {
            String[] pair = entry.split("=");
            p.setProperty(pair[0], pair[1]);
        }
        logic.postAuth(ar.getProfile().getProfileId(), p, "hi");

//        System.out.println("Using source as sink...");
//        Long sinkProfileId = ar.getProfile().getProfileId();
//        List<SourceProfileEntry> sourcesList = new ArrayList<>();
//        sourcesList.add(new SourceProfileEntry(ar.getProfile().getProfileId()));
//
//        JobCreationRequest r = new JobCreationRequest();
//        r.setSourceProfiles(sourcesList);
//        r.setSinkProfileId(sinkProfileId);
//        r.setTimeExpression("weekly");
//        r.setKeyRing("hi");
//        
//        BackupJob bj = logic.createBackupJob(u.getUsername(), r).getJob();
//        assertNotNull(bj.getId());
//        // assertNotNull(bj.getCronExpression());
//        assertNotNull(bj.getUser());
//        assertNotNull(bj.getSinkProfile());
//        assertNotNull(bj.getSourceProfiles());
//
//        List<Status> results = logic.getStatus("backuper", bj.getId());
//        for (Status s : results) {
//            System.out.println(s.getProgress() + ", " + s.getType() + ": " + s.getMessage());
//        }
    }

    @Test
    public void testMoodle() throws IOException {
        try {
            logic.deleteUser("fjungwirth");
        } catch (Exception e) {
        }

        try {
//            BackMeUpUser u = logic.register("fjungwirth", "12345678", "12345678", "jungwirth.florian@gmail.com");
//            logic.verifyEmailAddress(u.getVerificationKey());

        } catch (AlreadyRegisteredException are) {
        }
        // register moodle datasource

        AuthRequest ar = logic.preAuth("fjungwirth", "org.backmeup.moodle", "My Moodle Profile", "12345678");

        // print ar to shell if needed

        Properties props = new Properties();

        props.setProperty("Username", "backmeup");
        // pw: BMUbmu123!

        // local db:
        //props.setProperty("Password", "22598af74c6d2ba1cb00eb639f2e0779");
        // server db:
        props.setProperty("Password", "286bafbb1a9faf4dc4e104a33e222304");
        // server-side bmu moodle plugin has to be installed
        props.setProperty("Moodle Server Url", "http://gtn02.gtn-solutions.com/moodle20/");

        logic.postAuth(ar.getProfile().getProfileId(), props, "12345678");
        //logic.validateProfile("fjungwirth", ar.getProfile().getProfileId());

        // register skydrive datasink (changed to dropbox)

        AuthRequest ar2 = logic.preAuth("fjungwirth", "org.backmeup.dropbox", "Dropbox-Profile", "12345678");

        // print ar2 to shell if needed

        System.out.println("Open the following URL: " + ar2.getRedirectURL());

        props = new Properties();
        // automatically open web page and get code
        // String data =
        // AuthenticationPerformer.performAuthentication(ar2.getRedirectURL(),
        // new DropboxAutomaticAuthorizer());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String data = br.readLine();

        // data => code=1234567&somethingelse=otherPropery&...
        String[] entries = data.split("&");
        for (String entry : entries) {
            String[] pair = entry.split("=");
            props.setProperty(pair[0], pair[1]);
        }

        logic.postAuth(ar2.getProfile().getProfileId(), props, "12345678");

//        List<SourceProfileEntry> sources = new ArrayList<>();
//
//        sources.add(new SourceProfileEntry(ar.getProfile().getProfileId()));
//
//        // create and exceute a backupjob from moodle to skydrive
//        JobCreationRequest r = new JobCreationRequest();
//        r.setSourceProfiles(sources);
//        r.setSinkProfileId(ar2.getProfile().getProfileId());
//        r.setTimeExpression("now");
//        r.setKeyRing("12345678");
//
//        logic.createBackupJob("fjungwirth", r);
    }

    @Test
    public void testTwitter() throws IOException {
        try {
            logic.deleteUser("michaela.murauer@yahoo.com");
        } catch (Exception e) {
        }
        try {
//            BackMeUpUser u = logic.register("michaela.murauer@yahoo.com", "12345678", "12345678", "michaela.murauer@yahoo.com");
//            logic.verifyEmailAddress(u.getVerificationKey());

        } catch (AlreadyRegisteredException are) {
        }

        // register twitter datasource

        AuthRequest ar = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.twitter", "My Twitter Profile", "12345678");

        Properties props = new Properties();

        System.out.println("Open the following URL: " + ar.getRedirectURL());

        props = new Properties();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String data = br.readLine();
        String[] tmp = data.split("\\?");

        // data => code=1234567&somethingelse=otherPropery&...
        String[] entries = tmp[1].split("&");
        for (String entry : entries) {
            String[] pair = entry.split("=");
            props.setProperty(pair[0], pair[1]);
        }
        logic.postAuth(ar.getProfile().getProfileId(), props, "12345678");

        //logic.validateProfile("mmurauer", ar.getProfile().getProfileId());

        // register skydrive datasink (changed to dropbox)
        AuthRequest ar2 = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.dropbox", "Dropbox-Profile", "12345678");

        System.out.println("Open the following URL: " + ar2.getRedirectURL());

        props = new Properties();
        br = new BufferedReader(new InputStreamReader(System.in));
        data = br.readLine();

        // data => code=1234567&somethingelse=otherPropery&...
        entries = data.split("&");
        for (String entry : entries) {
            String[] pair = entry.split("=");
            props.setProperty(pair[0], pair[1]);
        }

        logic.postAuth(ar2.getProfile().getProfileId(), props, "12345678");

//        List<SourceProfileEntry> sources = new ArrayList<>();
//
//        sources.add(new SourceProfileEntry(ar.getProfile().getProfileId()));
//
//        // create and exceute a backupjob from moodle to skydrive
//        JobCreationRequest r = new JobCreationRequest();
//        r.setSourceProfiles(sources);
//        r.setSinkProfileId(ar2.getProfile().getProfileId());
//        r.setTimeExpression("now");
//        r.setKeyRing("12345678");
//
//        logic.createBackupJob("michaela.murauer@yahoo.com", r);
    }

    @Test
    public void testFacebook() throws IOException {
        try {
            logic.deleteUser("michaela.murauer@yahoo.com");
        } catch (Exception e) {
        }

        try {
//            BackMeUpUser u = logic.register("michaela.murauer@yahoo.com", "12345678", "12345678", "michaela.murauer@yahoo.com");
//            logic.verifyEmailAddress(u.getVerificationKey());
        } catch (AlreadyRegisteredException are) {
        }

        // register twitter datasource
        AuthRequest ar = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.facebook", "My Facebook Profile", "12345678");

        Properties props = new Properties();

        System.out.println("Open the following URL: " + ar.getRedirectURL());

        props = new Properties();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = br.readLine();

        String data = input.substring(input.indexOf('#') + 1);
        String[] entries = data.split("&");
        for (String entry : entries) {
            String[] pair = entry.split("=");
            props.setProperty(pair[0], pair[1]);
        }

        logic.postAuth(ar.getProfile().getProfileId(), props, "12345678");

        // logic.validateProfile("mmurauer", ar.getProfile().getProfileId());

        // register skydrive datasink (changed to dropbox)
        AuthRequest ar2 = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.dropbox", "Dropbox-Profile", "12345678");

        System.out.println("Open the following URL: " + ar2.getRedirectURL());

        props = new Properties();
        br = new BufferedReader(new InputStreamReader(System.in));
        data = br.readLine();

        // data => code=1234567&somethingelse=otherPropery&...
        entries = data.split("&");
        for (String entry : entries) {
            String[] pair = entry.split("=");
            props.setProperty(pair[0], pair[1]);
        }

        logic.postAuth(ar2.getProfile().getProfileId(), props, "12345678");

//        List<SourceProfileEntry> sources = new ArrayList<>();
//        sources.add(new SourceProfileEntry(ar.getProfile().getProfileId()));
//
//        // create and exceute a backupjob from moodle to skydrive
//        JobCreationRequest r = new JobCreationRequest();
//        r.setSourceProfiles(sources);
//        r.setSinkProfileId(ar2.getProfile().getProfileId());
//        r.setTimeExpression("now");
//        r.setKeyRing("12345678");

//        logic.createBackupJob("michaela.murauer@yahoo.com", r);
    }
}

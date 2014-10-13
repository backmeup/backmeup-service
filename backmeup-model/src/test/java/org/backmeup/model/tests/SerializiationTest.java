package org.backmeup.model.tests;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.junit.Assert;
import org.junit.Test;

public class SerializiationTest {
  
  private void testProfiles(Profile p1, Profile p2) {
    
    Assert.assertEquals(p1.getDescription(), p2.getDescription());
    Assert.assertEquals(p1.getProfileId(), p2.getProfileId());
    Assert.assertEquals(p1.getProfileName(), p2.getProfileName());
    Assert.assertEquals(p1.getType(), p2.getType());
    testUser(p1.getUser(), p2.getUser());    
  }
  
  private void testUser(BackMeUpUser u1, BackMeUpUser u2) {
    Assert.assertEquals(u1.getUserId(), u2.getUserId());
    Assert.assertEquals(u1.getEmail(), u2.getEmail());
  }
  
  private void testActionProfile(ActionProfile ap1, ActionProfile ap2) {
    Assert.assertEquals(ap1.getActionId(), ap2.getActionId());
    Assert.assertEquals(ap1.getId(), ap2.getId());
  }
  
  private void testProfileOptions(ProfileOptions po1 ,ProfileOptions po2) {
    Assert.assertEquals(po1.getOptionId(), po2.getOptionId());
    for (int i=0; i < po1.getOptions().length; i++) {
      Assert.assertEquals(po1.getOptions()[i], po2.getOptions()[i]);  
    }
    testProfiles(po1.getProfile(), po2.getProfile());
  }
  
  @Test
  public void testBackupJobSerializiation() {
    BackMeUpUser user = new BackMeUpUser(1L, "john.doe", "John", "Doe", "Sepp@Mail.at", "John123!#");
    Profile source = new Profile(2L, user, "TestProfile", "org.backmeup.source", PluginType.Source);
    ProfileOptions options = new ProfileOptions(source, new String[]{"folder1", "folder2"});
    Profile sink = new Profile(2L, user, "TestProfile2", "org.backmeup.sink", PluginType.Sink);
    List<ActionProfile> actions = new ArrayList<>();
    BackupJob job = new BackupJob(user, options, sink, actions, new Date(), new Date().getTime() + 1000000L, "TestJob1", false);
    String serializedJob = JsonSerializer.serialize(job);
    BackupJob restored = JsonSerializer.deserialize(serializedJob, BackupJob.class);
    restored.toString();
    Assert.assertEquals(job.getDelay(), restored.getDelay());
    Assert.assertEquals(job.getId(), restored.getId());
    Assert.assertEquals(job.getStart(), restored.getStart());
    testUser(job.getUser(), restored.getUser());
    for (int i=0; i < job.getRequiredActions().size(); i++) {
      Iterator<ActionProfile> apIt1 = job.getRequiredActions().iterator();
      Iterator<ActionProfile> apIt2 = restored.getRequiredActions().iterator();
      while(apIt1.hasNext()) {
        testActionProfile(apIt1.next(), apIt2.next());
      }
    }
    testProfiles(job.getSinkProfile(), restored.getSinkProfile());
    
    ProfileOptions po1 = job.getSourceProfiles();
    ProfileOptions po2 = restored.getSourceProfiles();
    testProfileOptions (po1, po2);    
  }
}
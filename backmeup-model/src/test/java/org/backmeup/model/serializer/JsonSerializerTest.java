package org.backmeup.model.serializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.junit.Assert;
import org.junit.Test;

public class JsonSerializerTest {
  
  private void testProfiles(Profile p1, Profile p2) {
    Assert.assertEquals(p1.getPluginId(), p2.getPluginId());
    Assert.assertEquals(p1.getId(), p2.getId());
    Assert.assertEquals(p1.getName(), p2.getName());
    Assert.assertEquals(p1.getType(), p2.getType());
    
    if((p1.getOptions() != null) && (p2.getOptions() != null)) {
    for (int i=0; i < p1.getOptions().size(); i++) {
        Assert.assertEquals(p1.getOptions().get(i), p2.getOptions().get(i));  
    }
    }
    
    testUser(p1.getUser(), p2.getUser());    
  }
  
  private void testUser(BackMeUpUser u1, BackMeUpUser u2) {
    Assert.assertEquals(u1.getUserId(), u2.getUserId());
    Assert.assertEquals(u1.getEmail(), u2.getEmail());
  }
  
  @Test
  public void testBackupJobSerializiation() {
    BackMeUpUser user = new BackMeUpUser(1L, "john.doe", "John", "Doe", "Sepp@Mail.at", "John123!#");
    
    Profile source = new Profile(2L, user, "TestProfile", "org.backmeup.source", PluginType.Source);
    source.addOption("folder1");
    source.addOption("folder2");
    
    Profile sink = new Profile(2L, user, "TestProfile2", "org.backmeup.sink", PluginType.Sink);
    
    List<Profile> actions = new ArrayList<>();
    
    BackupJob job = new BackupJob(user, source, sink, actions, new Date(), new Date().getTime() + 1000000L, "TestJob1", false);
    
    String serializedJob = JsonSerializer.serialize(job);
    BackupJob restored = JsonSerializer.deserialize(serializedJob, BackupJob.class);
    restored.toString();
    
    Assert.assertEquals(job.getDelay(), restored.getDelay());
    Assert.assertEquals(job.getId(), restored.getId());
    Assert.assertEquals(job.getStart(), restored.getStart());
    
    testUser(job.getUser(), restored.getUser());
    
    for (int i=0; i < job.getActionProfiles().size(); i++) {
      Iterator<Profile> apIt1 = job.getActionProfiles().iterator();
      Iterator<Profile> apIt2 = restored.getActionProfiles().iterator();
      while(apIt1.hasNext()) {
        testProfiles(apIt1.next(), apIt2.next());
      }
    }
    testProfiles(job.getSinkProfile(), restored.getSinkProfile());
    
    Profile po1 = job.getSourceProfile();
    Profile po2 = restored.getSourceProfile();
    testProfiles(po1, po2);    
  }
}

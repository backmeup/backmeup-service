package org.backmeup.logic.impl;

import java.util.Date;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.index.api.SharingPolicyClient;
import org.backmeup.index.client.SharingPolicyClientFactory;
import org.backmeup.index.model.User;
import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.logic.HeritageLogic;
import org.backmeup.model.BackMeUpUser;

@ApplicationScoped
public class HeritageLogicImpl implements HeritageLogic {

    @Inject
    private SharingPolicyClientFactory sharingPolicyClientFactory;

    private SharingPolicyClient getSharingPolicyClient(Long userId) {
        return this.sharingPolicyClientFactory.getSharingPolicyClient(userId);
    }

    @Override
    public Set<SharingPolicyEntry> getAllOwned(BackMeUpUser currUser) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            Set<SharingPolicyEntry> result = client.getAllOwnedHeritagePolicies();
            return result;
        }
    }

    @Override
    public Set<SharingPolicyEntry> getAllIncoming(BackMeUpUser currUser) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            Set<SharingPolicyEntry> result = client.getAllIncomingHeritagePolicies();
            return result;
        }
    }

    @Override
    public SharingPolicyEntry add(BackMeUpUser currUser, BackMeUpUser sharingWith, SharingPolicyTypeEntry policy,
            String sharedElementID, String name, String description, Date lifespanstart, Date lifespanend) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            SharingPolicyEntry result = client.addHeritagePolicy(new User(sharingWith.getUserId()), policy,
                    sharedElementID, name, description, lifespanstart, lifespanend);
            return result;
        }
    }

    @Override
    public SharingPolicyEntry updateOwned(BackMeUpUser currUser, Long policyID, String name, String description,
            Date lifespanstart, Date lifespanend) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            SharingPolicyEntry result = client.updateHeritagePolicy(policyID, name, description, lifespanstart,
                    lifespanend);
            return result;
        }
    }

    @Override
    public String removeOwned(BackMeUpUser currUser, Long policyID) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            String result = client.removeOwnedHeritagePolicy(policyID);
            return result;
        }
    }

    @Override
    public String activateDeadMannSwitchAndImport(BackMeUpUser currUser) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            String result = client.activateDeadMannSwitchAndImport();
            return result;
        }
    }

}

package org.backmeup.logic.impl;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.index.api.SharingPolicyClient;
import org.backmeup.index.client.SharingPolicyClientFactory;
import org.backmeup.index.model.User;
import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.logic.SharingLogic;
import org.backmeup.model.BackMeUpUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SharingLogicImpl implements SharingLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private SharingPolicyClientFactory sharingPolicyClientFactory;

    private SharingPolicyClient getSharingPolicyClient(Long userId) {
        return this.sharingPolicyClientFactory.getSharingPolicyClient(userId);
    }

    @Override
    public Set<SharingPolicyEntry> getAllOwned(BackMeUpUser currUser) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            Set<SharingPolicyEntry> result = client.getAllOwned();
            return result;
        }
    }

    @Override
    public Set<SharingPolicyEntry> getAllIncoming(BackMeUpUser currUser) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            Set<SharingPolicyEntry> result = client.getAllIncoming();
            return result;
        }
    }

    @Override
    public SharingPolicyEntry add(BackMeUpUser currUser, BackMeUpUser sharingWith, SharingPolicyTypeEntry policy,
            String sharedElementID, String name, String description) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            SharingPolicyEntry result = client.add(new User(sharingWith.getUserId()), policy, sharedElementID, name,
                    description);
            return result;
        }
    }

    @Override
    public String removeOwned(BackMeUpUser currUser, Long policyID) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            String result = client.removeOwned(policyID);
            return result;
        }
    }

    @Override
    public String removeAllOwned(BackMeUpUser currUser) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            String result = client.removeAllOwned();
            return result;
        }
    }

    @Override
    public String acceptIncomingSharing(BackMeUpUser currUser, Long policyID) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            String result = client.acceptIncomingSharing(policyID);
            return result;
        }
    }

    @Override
    public String declineIncomingSharing(BackMeUpUser currUser, Long policyID) {
        try (SharingPolicyClient client = getSharingPolicyClient(currUser.getUserId())) {
            String result = client.declineIncomingSharing(policyID);
            return result;
        }
    }

}

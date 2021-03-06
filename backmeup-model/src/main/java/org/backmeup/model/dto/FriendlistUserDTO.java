package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.FriendlistUser.FriendListType;

@XmlRootElement
public class FriendlistUserDTO {
    private Long friendId;
    private String name;
    private String description;
    private String email;
    private Long bmuUserId;
    private FriendListType friendListType;

    public FriendlistUserDTO() {

    }

    public FriendlistUserDTO(Long friendId, String name, String description, String email, boolean bmuUser,
            Long bmuUserId, FriendListType friendListType) {
        this.friendId = friendId;
        this.name = name;
        this.description = description;
        this.email = email;
        this.bmuUserId = bmuUserId;
        this.friendListType = friendListType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isBmuUser() {
        if (this.bmuUserId != null) {
            return true;
        }
        return false;
    }

    public Long getFriendId() {
        return this.friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public Long getBmuUserId() {
        return this.bmuUserId;
    }

    public void setBmuUserId(Long bmuUserId) {
        this.bmuUserId = bmuUserId;
    }

    public FriendListType getFriendListType() {
        return this.friendListType;
    }

    public void setFriendListType(FriendListType friendListType) {
        this.friendListType = friendListType;
    }

}

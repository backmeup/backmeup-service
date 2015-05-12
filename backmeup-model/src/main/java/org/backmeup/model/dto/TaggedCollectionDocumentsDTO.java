package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@SuppressWarnings("unused")
public class TaggedCollectionDocumentsDTO {

    private Long Id;
    private List<UUID> documentIds = new ArrayList<UUID>();

    public List<UUID> getDocumentIds() {
        return this.documentIds;
    }

    public void setDocumentIds(List<UUID> documentIds) {
        this.documentIds = documentIds;
    }

    public Long getId() {
        return this.Id;
    }

    public void setId(Long id) {
        this.Id = id;
    }

}

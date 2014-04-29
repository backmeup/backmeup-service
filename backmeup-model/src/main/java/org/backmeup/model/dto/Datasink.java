package org.backmeup.model.dto;

public class Datasink {
	private String datasinkId;
	private String title;
	private String imageURL;
	private String description;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDatasinkId() {
		return datasinkId;
	}

	public void setDatasinkId(String datasinkId) {
		this.datasinkId = datasinkId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public Datasink() {
		
	}
	
	public Datasink(String datasinkId, String title, String imageURL, String description) {
		super();
		this.datasinkId = datasinkId;
		this.title = title;
		this.imageURL = imageURL;
		this.description = description;
	}
}

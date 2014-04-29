package org.backmeup.model.dto;

public class Datasource {
	private String datasourceId;
	private String title;
	private String imageURL;
	
	public Datasource() {
	}

	public Datasource(String datasourceId, String title, String imageURL) {
		super();
		this.datasourceId = datasourceId;
		this.title = title;
		this.imageURL = imageURL;
	}

	public String getDatasourceId() {
		return datasourceId;
	}

	public void setDatasourceId(String datasourceId) {
		this.datasourceId = datasourceId;
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
}
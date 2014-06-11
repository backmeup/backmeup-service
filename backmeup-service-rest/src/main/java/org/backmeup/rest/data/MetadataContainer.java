package org.backmeup.rest.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MetadataContainer {
	private Map<String, String> metadata = new HashMap<>();

	public MetadataContainer() {
	}

	public MetadataContainer(Properties props) {
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			String keyStr = (String) entry.getKey();
			String valueStr = (String) entry.getValue();
			setProperty(keyStr, valueStr);
		}
	}

	public void setProperty(String key, String value) {
		this.metadata.put(key, value);
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
}

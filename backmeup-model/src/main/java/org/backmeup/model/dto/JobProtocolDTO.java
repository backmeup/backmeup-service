package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobProtocolDTO {
	private Long protocolId;
	private Long timestamp;
	private Long start;
	private Long executionTime;
	private boolean successful;
	private long processedItems;
	private int space;
	private String message = ""; 

	public JobProtocolDTO() {
	}

	public Long getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(Long protocolId) {
		this.protocolId = protocolId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public Long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Long executionTime) {
		this.executionTime = executionTime;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public long getProcessedItems() {
		return processedItems;
	}

	public void setProcessedItems(long processedItems) {
		this.processedItems = processedItems;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

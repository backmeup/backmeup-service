package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.dto.ActionElement;
import org.backmeup.model.spi.ActionDescribable;

@XmlRootElement
public class ActionContainer {
	private List<ActionElement> actions;
	
	public ActionContainer() {
	}

	public ActionContainer(List<ActionDescribable> actions) {
		this.actions = new ArrayList<ActionElement>();
		for (ActionDescribable ad : actions) {
			this.actions.add(new ActionElement(ad.getTitle(), ad.getId(), ad.getDescription(), ad.getActionVisibility()));
		}
	}

	public List<ActionElement> getActions() {
		return actions;
	}

	public void setActions(List<ActionElement> actions) {
		this.actions = actions;
	}
}

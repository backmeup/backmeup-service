package org.backmeup.rest;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

@ApplicationScoped
public class MapperProducer {
	private static final String DOZER_USER_MAPPING = "dozer-user-mapping.xml";
	
	private Mapper mapper;
	
	@Produces
	@SuppressWarnings("serial")
	public Mapper getMapper() {
		if(mapper == null) {
			List<String> list = new ArrayList<String>() { 
				{add(DOZER_USER_MAPPING);}
			};
			mapper = new DozerBeanMapper(list);
		}
		return mapper;
	}
}

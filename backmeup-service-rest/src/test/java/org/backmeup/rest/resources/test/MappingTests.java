package org.backmeup.rest.resources.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.dto.UserDTO;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Test;

public class MappingTests {
	private static final String DOZER_USER_MAPPING = "dozer-user-mapping.xml";

	@Test
	public void testUserMapping() {
		Long userId = 1L;
		String username = "johndoe";
		String email = "johndoe@example.com";
		boolean activated = true;
		String verificationKey = "123ABC";
		
		BackMeUpUser srcUser = new BackMeUpUser(userId, username, email);
		srcUser.setActivated(activated);
		srcUser.setVerificationKey(verificationKey);
		
		Mapper mapper = getMapper(DOZER_USER_MAPPING);
		
		UserDTO destUser = mapper.map(srcUser, UserDTO.class);
		
		assertEquals(srcUser.getUserId(), destUser.getUserId());
		assertEquals(srcUser.getUsername(), destUser.getFirstname());
		assertEquals(srcUser.getUsername(), destUser.getName());
		assertEquals(srcUser.getEmail(), destUser.getEmail());
		assertEquals(srcUser.isActivated(), destUser.isActivated());
		assertEquals(srcUser.getVerificationKey(), destUser.getVerificationKey());
	}
	
	@SuppressWarnings("serial")
	private Mapper getMapper(final String mappingFile) {
		List<String> list = new ArrayList<String>() { 
			{add(mappingFile);}
		};
		return new DozerBeanMapper(list);
	}
}

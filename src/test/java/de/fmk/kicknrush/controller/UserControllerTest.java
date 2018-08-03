package de.fmk.kicknrush.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fmk.kicknrush.ServerStart;
import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.dto.UserDTO;
import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.security.PasswordUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


/**
 * @author FabianK
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerStart.class)
@WebAppConfiguration
public class UserControllerTest {
	@Autowired
	private ObjectMapper          objectMapper;
	@Autowired
	private WebApplicationContext webAppContext;

	@MockBean
	private DatabaseHandler dbHandler;
	@MockBean
	private JdbcTemplate    jdbcTemplate;

	private JacksonTester<List<String>>  usernameListJson;
	private JacksonTester<List<UserDTO>> userListJson;
	private JacksonTester<UserDTO>       userJson;
	private MockMvc                      mockMvc;
	private UserDTO                      userDTO;


	@Before
	public void setUp() {
		JacksonTester.initFields(this, objectMapper);

		mockMvc = webAppContextSetup(webAppContext).build();
		userDTO = new UserDTO();
	}


	@Test
	public void createUserTest() throws Exception {
		final UUID sessionID;
		final UUID userID;
		final UUID wrongSessionID;

		sessionID      = UUID.randomUUID();
		userID         = UUID.randomUUID();
		wrongSessionID = UUID.randomUUID();

		given(dbHandler.isAdminSession(wrongSessionID)).willReturn(false);
		given(dbHandler.isAdminSession(sessionID)).willReturn(true);
		given(dbHandler.updateSession(sessionID)).willReturn(true);

		userDTO.setUserID(userID.toString());
		performPost("/api/user/admin/createUser", userDTO, status().isBadRequest());

		userDTO.setSessionID(wrongSessionID.toString());
		userDTO.setUsername("ValidUser");
		performPost("/api/user/admin/createUser", userDTO, status().isForbidden());

		userDTO.setSessionID(sessionID.toString());
		given(dbHandler.createUser(User.fromDTO(userDTO))).willReturn(true);
		performPost("/api/user/admin/createUser", userDTO, status().isOk());

		userDTO.setSessionID("Test");
		performPost("/api/user/admin/createUser", userDTO, status().isBadRequest());
	}


	@Test
	public void deleteUserTest() throws Exception {
		final UUID sessionID;
		final UUID userID;
		final UUID wrongSessionID;

		sessionID      = UUID.randomUUID();
		userID         = UUID.randomUUID();
		wrongSessionID = UUID.randomUUID();

		given(dbHandler.isAdminSession(sessionID)).willReturn(true);
		given(dbHandler.isAdminSession(wrongSessionID)).willReturn(false);
		given(dbHandler.deleteUser(userID)).willReturn(true);

		userDTO.setUserID(userID.toString());
		performPost("/api/user/admin/deleteUser", userDTO, status().isBadRequest());

		userDTO.setSessionID(wrongSessionID.toString());
		performPost("/api/user/admin/deleteUser", userDTO, status().isForbidden());

		userDTO.setSessionID(sessionID.toString());
		performPost("/api/user/admin/deleteUser", userDTO, status().isOk());
	}


	@Test
	public void getUsernamesTest() throws Exception {
		final List<String> usernames;
		final UUID         sessionID;
		final UUID         userID;
		final UUID         wrongUserID;

		sessionID   = UUID.randomUUID();
		userID      = UUID.randomUUID();
		wrongUserID = UUID.randomUUID();

		usernames = new ArrayList<>();
		usernames.add("User1");
		usernames.add("User2");

		given(dbHandler.getUsernames()).willReturn(usernames);
		given(dbHandler.checkSession(sessionID, wrongUserID)).willReturn(false);
		given(dbHandler.checkSession(sessionID, userID)).willReturn(true);

		mockMvc.perform(get("/api/user/getUsernames").param("sessionID", "Test").param("userID", userID.toString()))
		       .andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/user/getUsernames").param("sessionID", sessionID.toString())
		                                             .param("userID", wrongUserID.toString()))
		       .andExpect(status().isForbidden());

		mockMvc.perform(get("/api/user/getUsernames").param("sessionID", sessionID.toString())
		                                             .param("userID", userID.toString()))
		       .andExpect(status().isOk())
		       .andExpect(content().json(usernameListJson.write(usernames).getJson()));
	}


	@Test
	public void getUsersTest() throws Exception {
		final List<User>    users;
		final List<UserDTO> result;
		final UUID          sessionID;
		final UUID          userID;

		result    = new ArrayList<>();
		sessionID = UUID.randomUUID();
		userID    = UUID.randomUUID();

		users = new ArrayList<>();
		users.add(new User(true, null, "Admin", UUID.randomUUID()));
		users.add(new User(false, null, "User", UUID.randomUUID()));

		users.forEach(user -> result.add(user.toDTO()));

		given(dbHandler.getUsers()).willReturn(users);
		given(dbHandler.checkSession(sessionID, userID)).willReturn(true);

		mockMvc.perform(get("/api/user/getUsers").param("sessionID", sessionID.toString())
		                                         .param("userID", userID.toString()))
		       .andExpect(status().isOk())
		       .andExpect(content().json(userListJson.write(result).getJson()));
	}


	@Test
	public void loginTest() throws Exception {
		final String  salt;
		final UUID    sessionID;
		final UUID    userID;
		final User    user;
		final UserDTO result;

		salt      = PasswordUtils.getSalt(20);
		sessionID = UUID.randomUUID();
		userID    = UUID.randomUUID();

		user = new User();
		user.setUsername("ValidUser");
		user.setId(userID);
		user.setPassword(PasswordUtils.generateSecurePassword("correctPassword", salt));
		user.setSalt(salt);

		given(dbHandler.findUser("NoUser")).willReturn(null);
		given(dbHandler.findUser("ValidUser")).willReturn(user);
		given(dbHandler.createSession(userID)).willReturn(sessionID);

		performPost("/api/user/login", userDTO, status().isBadRequest());

		userDTO.setUsername("NoUser");
		userDTO.setPassword("TestPW");
		performPost("/api/user/login", userDTO, status().isForbidden());

		userDTO.setUsername("ValidUser");
		userDTO.setPassword("correctPassword");

		result = new UserDTO();
		result.setUserID(userID.toString());
		result.setUsername("ValidUser");
		result.setSalt(salt);
		result.setSessionID(sessionID.toString());

		performPost("/api/user/login", userDTO, status().isOk()).andExpect(content().json(userJson.write(result).getJson()));
	}


	@Test
	public void logoutTest() throws Exception {
		final String userID;
		final UUID   sessionID;
		final UUID   wrongSessionID;


		sessionID      = UUID.randomUUID();
		userID         = UUID.randomUUID().toString();
		wrongSessionID = UUID.randomUUID();

		given(dbHandler.closeSession(sessionID, userID)).willReturn(true);
		given(dbHandler.closeSession(wrongSessionID, userID)).willReturn(false);

		performPost("/api/user/logout", userDTO, status().isBadRequest());

		userDTO.setSessionID(sessionID.toString());
		performPost("/api/user/logout", userDTO, status().isBadRequest());

		userDTO.setUserID(userID);
		performPost("/api/user/logout", userDTO, status().isOk());

		userDTO.setSessionID("Test");
		performPost("/api/user/logout", userDTO, status().isBadRequest());

		userDTO.setSessionID(wrongSessionID.toString());
		performPost("/api/user/logout", userDTO, status().isBadRequest());
	}


	@Test
	public void updateUserTest() throws Exception {
		final UUID sessionID;
		final UUID userID;

		sessionID = UUID.randomUUID();
		userID    = UUID.randomUUID();

		given(dbHandler.checkSession(sessionID, userID)).willReturn(true);
		given(dbHandler.isAdminSession(sessionID)).willReturn(false);

		performPost("/api/user/updateUser", userDTO, status().isBadRequest());

		userDTO.setSessionID(sessionID.toString());
		userDTO.setUserID(userID.toString());
		userDTO.setUsername("TestUser");

		given(dbHandler.updateSession(sessionID)).willReturn(true);
		given(dbHandler.updateUser(User.fromDTO(userDTO))).willReturn(true);
		performPost("/api/user/updateUser", userDTO, status().isOk());
	}


	private ResultActions performPost(final String path, final UserDTO userDTO, final ResultMatcher expected)
			throws Exception {
		final String json;

		json = userJson.write(userDTO).getJson();

		return mockMvc.perform(post(path).content(json).contentType(MediaType.APPLICATION_JSON_UTF8))
		              .andExpect(expected);
	}
}

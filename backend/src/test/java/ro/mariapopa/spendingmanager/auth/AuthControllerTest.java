package ro.mariapopa.spendingmanager.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ro.mariapopa.spendingmanager.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String LOGOUT_URL = "/api/auth/logout";
    private static final String ME_URL = "/api/auth/me";

    @Autowired
    MockMvc mockMvc;

    @Test
    void whenMeWithoutLogin_thenReturn401() throws Exception {
        mockMvc.perform(get(ME_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void whenMeWithLoggedInUser_thenReturnUsername() throws Exception {
        mockMvc.perform(get(ME_URL).with(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("maria"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void whenLoginWithCorrectPassword_thenReturn200AndAuthenticated() throws Exception {
        mockMvc.perform(formLogin(LOGIN_URL).user("maria").password("test-password"))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("maria"));
    }

    @Test
    void whenLoginWithWrongPassword_thenReturn401AndUnauthenticated() throws Exception {
        mockMvc.perform(formLogin(LOGIN_URL).user("maria").password("wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated());
    }

    @Test
    void whenLoginWithUnknownUser_thenReturn401AndUnauthenticated() throws Exception {
        mockMvc.perform(formLogin(LOGIN_URL).user("cristi").password("test-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated());
    }

    @Test
    void whenLogout_thenReturn204AndUnauthenticated() throws Exception {
        mockMvc.perform(post(LOGOUT_URL).with(user("maria")).with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(unauthenticated());
    }

    @Test
    void whenApiCalledWithoutLogin_thenReturn401InsteadOfRedirect() throws Exception {
        mockMvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
    }

    @Test
    void whenPostWithoutCsrfToken_thenReturn403() throws Exception {
        mockMvc.perform(post("/api/transactions").with(user("maria"))).andExpect(status().isForbidden());
    }

    @Test
    void whenHealthWithoutLogin_thenReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}

package io.pivotal.accounts.controller;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.accounts.config.SecurityConfig;
import io.pivotal.accounts.configuration.ServiceTestConfiguration;
import io.pivotal.accounts.configuration.TestSecurityConfiguration;
import io.pivotal.accounts.domain.AccountType;
import io.pivotal.accounts.service.AccountService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Tests for the AccountsController.
 *
 * @author David Ferreira Pinto
 */

@RunWith(SpringRunner.class)
@Import(TestSecurityConfiguration.class)
@WebMvcTest(controllers = AccountController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
public class AccountControllerTest {

    private static final String EXPECTED_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'+0000'";

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private AccountService service;

    private JwtAuthenticationToken token;

    @Before
    public void before() {
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("header1", "value1");
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("sub", "user@user.com");
        Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.MAX, headers, claims);
        token = new JwtAuthenticationToken(jwt, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ACCOUNT"), new SimpleGrantedAuthority("ROLE_TRADE")));
    }


    /**
     * Test the POST to <code>/account</code>.
     * test creation of accounts.
     *
     * @throws Exception
     */
    @Test
    public void doPostAccount() throws Exception {
        when(service.saveAccount(ServiceTestConfiguration.account()))
                .thenReturn(ServiceTestConfiguration.ACCOUNT_ID);

        mockMvc.perform(
                post("/accounts").with(authentication(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(
                                convertObjectToJson(ServiceTestConfiguration
                                        .account())))
                .andExpect(status().isCreated()).andDo(print());
    }

    /**
     * Test the GET to <code>/account</code>.
     * test retrieval of accounts.
     *
     * @throws Exception
     */
    @Test
    public void doGetAccount() throws Exception {
        when(service.findAccount(ServiceTestConfiguration.ACCOUNT_ID))
                .thenReturn(ServiceTestConfiguration.account());

        mockMvc.perform(
                get("/accounts/" + ServiceTestConfiguration.ACCOUNT_ID)
                        .with(authentication(token))
                        .contentType(MediaType.APPLICATION_JSON).content(
                        convertObjectToJson(ServiceTestConfiguration
                                .account())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.id").value(
                                ServiceTestConfiguration.ACCOUNT_ID))
                .andExpect(
                        jsonPath("$.creationdate").value(
                                ServiceTestConfiguration.ACCOUNT_DATE.toString(EXPECTED_DATE_FORMAT)))
                .andExpect(
                        jsonPath("$.openbalance").value(
                                ServiceTestConfiguration.ACCOUNT_OPEN_BALANCE
                                        .doubleValue()))
                .andExpect(
                        jsonPath("$.balance").value(
                                ServiceTestConfiguration.ACCOUNT_BALANCE))
                .andDo(print());
    }

    /**
     * Test the GET to <code>/accounts</code>.
     * test retrieval of accounts by username.
     *
     * @throws Exception
     */
    @Test
    public void doGetAccounts() throws Exception {
        when(service.findAccounts())
                .thenReturn(Collections.singletonList(ServiceTestConfiguration.account()));

        mockMvc.perform(
                get("/accounts?name=" + ServiceTestConfiguration.USER_ID)
                        .with(authentication(token))
                        .contentType(MediaType.APPLICATION_JSON).content(
                        convertObjectToJson(ServiceTestConfiguration
                                .account())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$[0].id").value(
                                ServiceTestConfiguration.ACCOUNT_ID))
                .andExpect(
                        jsonPath("$[0].creationdate").value(
                                ServiceTestConfiguration.ACCOUNT_DATE.toString(EXPECTED_DATE_FORMAT)))
                .andExpect(
                        jsonPath("$[0].openbalance").value(
                                ServiceTestConfiguration.ACCOUNT_OPEN_BALANCE
                                        .doubleValue()))
                .andExpect(
                        jsonPath("$[0].balance").value(
                                ServiceTestConfiguration.ACCOUNT_BALANCE))
                .andDo(print());
    }

    /**
     * Test the GET to <code>/accounts</code>.
     * test retrieval of accounts by username and type.
     *
     * @throws Exception
     */
    @Test
    public void doGetAccountsWithType() throws Exception {
        when(service.findAccountsByType(AccountType.CURRENT))
                .thenReturn(Collections.singletonList(ServiceTestConfiguration.account()));

        mockMvc.perform(
                get("/accounts")
                        .with(authentication(token))
                        .param("name", ServiceTestConfiguration.USER_ID)
                        .param("type", "CURRENT")
                        .contentType(MediaType.APPLICATION_JSON).content(
                        convertObjectToJson(ServiceTestConfiguration
                                .account())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$[0].id").value(
                                ServiceTestConfiguration.ACCOUNT_ID))
                .andExpect(
                        jsonPath("$[0].creationdate").value(
                                ServiceTestConfiguration.ACCOUNT_DATE.toString(EXPECTED_DATE_FORMAT)))
                .andExpect(
                        jsonPath("$[0].openbalance").value(
                                ServiceTestConfiguration.ACCOUNT_OPEN_BALANCE
                                        .doubleValue()))
                .andExpect(
                        jsonPath("$[0].balance").value(
                                ServiceTestConfiguration.ACCOUNT_BALANCE))
                .andDo(print());
    }

    /**
     * Test the GET to <code>/accounts/transaction/</code>.
     * test increase of balance.
     *
     * @throws Exception
     */
    @Test
    public void doIncreaseBalance() throws Exception {
        when(service.findAccount(ServiceTestConfiguration.ACCOUNT_ID))
                .thenReturn(ServiceTestConfiguration.account());

        MvcResult result = mockMvc.perform(
                post("/accounts/transaction")
                        .with(authentication(token))
                        .contentType(MediaType.APPLICATION_JSON).content(
                        convertObjectToJson(ServiceTestConfiguration
                                .getCreditTransaction())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("SUCCESS"))
                .andReturn();
    }

    /**
     * Test the GET to <code>/account/transaction</code>.
     * test increase of balance with negative amount.
     *
     * @throws Exception
     */
    @Test
    public void doIncreaseBalanceNegative() throws Exception {
        when(service.findAccount(ServiceTestConfiguration.ACCOUNT_ID))
                .thenReturn(ServiceTestConfiguration.account());

        MvcResult result = mockMvc.perform(
                post("/accounts/transaction")
                        .with(authentication(token))
                        .contentType(MediaType.APPLICATION_JSON).content(
                        convertObjectToJson(ServiceTestConfiguration
                                .getBadCreditTransaction())))
                .andExpect(status().isExpectationFailed())
                .andDo(print())
                .andExpect(content().string("FAILED"))
                .andReturn();
    }

    /**
     * Test the GET to <code>/account/transaction</code>.
     * test decrease of balance.
     *
     * @throws Exception
     */
    @Test
    public void doDecreaseBalance() throws Exception {
        when(service.findAccount(ServiceTestConfiguration.ACCOUNT_ID))
                .thenReturn(ServiceTestConfiguration.account());

        mockMvc.perform(
                post("/accounts/transaction")
                        .with(authentication(token))
                        .contentType(MediaType.APPLICATION_JSON).content(
                        convertObjectToJson(ServiceTestConfiguration
                                .getDebitTransaction())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("SUCCESS"))
                .andDo(print());
    }

    /**
     * Test the GET to <code>/account/transaction</code>.
     * test decrease of balance with not enough funds.
     *
     * @throws Exception
     */

    @Test
    public void doDecreaseBalanceNoFunds() throws Exception {
        when(service.findAccount(ServiceTestConfiguration.ACCOUNT_ID))
                .thenReturn(ServiceTestConfiguration.account());

        mockMvc.perform(
                post("/accounts/transaction")
                        .with(authentication(token))
                        .contentType(MediaType.APPLICATION_JSON).content(
                        convertObjectToJson(ServiceTestConfiguration
                                .getBadDebitTransaction())))
                .andExpect(status().isExpectationFailed())
                .andDo(print())
                .andExpect(content().string("FAILED"))
                .andDo(print());
    }

    private String convertObjectToJson(Object request) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper.writeValueAsString(request);
    }
}

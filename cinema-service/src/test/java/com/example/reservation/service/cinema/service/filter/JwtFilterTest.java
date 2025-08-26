package com.example.reservation.service.cinema.service.filter;


import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.AuthServiceClientMock;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import org.example.authservice.client.config.AuthClientConfiguration;
import org.example.authservice.domain.entity.User;
import org.example.authservice.filter.utils.JwtGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "service.authservice.client.url=http://localhost:8080",
        "service.authservice.client.timeout=2s",
        "service.jwt.enabled=true"
})
@DirtiesContext
public class JwtFilterTest {

    private static final PostgresContainer container = new PostgresContainer();

    @Autowired
    private AuthClientConfiguration authClientConfiguration;

    @Autowired
    TestRestTemplate testRestTemplate;

    private AuthServiceClientMock authServiceClientMock;
    private JwtGenerator jwtGenerator;

    @BeforeAll
    void beforeAll() throws IOException {
     jwtGenerator = new JwtGenerator(10000, 10000, "2ADDFD5C436226A765CHSADFDAS33212332138A3BE26A");
     authServiceClientMock = new AuthServiceClientMock(authClientConfiguration);
     authServiceClientMock.start();
     container.initDatabase();
     container.initRecords();
    }

    @AfterAll
    void afterAll() throws IOException {
        container.clearRecords();
        container.clearDatabase();
        authServiceClientMock.stop();
    }

    @Test
    void shouldReturnOkWhenUserHasValidJwtToken(){
        // given
        String email = "email";
        authServiceClientMock.stubResponse(email, "userUid");
        User user = User.builder()
                .email(email)
                .userUid("userUid")
                .password("password")
                .build();
        String jwt = jwtGenerator.generateToken(user);

        // when
        ResponseEntity<String> result = sendRequest(jwt);

        // then
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void shouldNotReturnOkWhenUserHasExpiredJwt(){
        // given
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWFpbCIsImlhdCI6MTc1MzUyOTUzMiwiZXhwIjoxNzUzNTI5NTMyfQ.3GERjv6DpRkMh2ZLme1Z6M_xg6dRHg9Sdc36X1lChcU";

        // when
        ResponseEntity<String> result = sendRequest(jwt);

        // then
        Assertions.assertFalse(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void shouldNotReturnOkWhenUserHasInvalidJwt(){
        // given
        String jwt = "invalid-jwt";

        // when
        ResponseEntity<String> result = sendRequest(jwt);

        // then
        Assertions.assertFalse(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void shouldNotReturnOkWhenUserDoesNotHaveJwt(){
        // when
        ResponseEntity<String> result = sendRequest(null);

        // then
        Assertions.assertFalse(result.getStatusCode().is2xxSuccessful());
    }

    private ResponseEntity<String> sendRequest(String jwt){
        HttpHeaders headers = new HttpHeaders();
        if(jwt != null){
            headers.set("Authorization", "Bearer "+ jwt);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "/api/v1/seance/reserved/seance-id1";
        return testRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

}
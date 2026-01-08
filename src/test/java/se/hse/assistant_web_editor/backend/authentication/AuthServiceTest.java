package se.hse.assistant_web_editor.backend.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.hse.assistant_web_editor.backend.dto.AuthRequest;
import se.hse.assistant_web_editor.backend.dto.AuthResponse;
import se.hse.assistant_web_editor.backend.dto.UserDto;
import se.hse.assistant_web_editor.backend.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void registerAndLoginTest() {
        String username = "login@hse.ru";
        String pwd = "StrongPa$$99";
        AuthRequest dto = new AuthRequest(username, pwd);
        AuthResponse authResponse = authService.register(dto);
        assertTrue(authResponse.success());

        authResponse = authService.authenticate(dto);
        assertTrue(authResponse.success());
        assertNotNull(authResponse.message());
        UserDto info = authService.getUserInfo(username);
        assertEquals(username, info.getUsername());
    }
}

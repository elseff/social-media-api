package ru.elseff.socialmedia.web.api.modules.auth.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.elseff.socialmedia.persistense.RoleEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.RoleRepository;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.security.JwtProvider;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthLoginRequest;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthRegisterRequest;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthResponse;
import ru.elseff.socialmedia.web.api.modules.auth.exception.AuthUserNotFoundException;
import ru.elseff.socialmedia.web.api.modules.auth.exception.AuthenticationException;
import ru.elseff.socialmedia.web.api.modules.user.dto.mapper.UserDtoMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserDtoMapper userDtoMapper;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Зарегистрироваться, если пользователь уже существует")
    void register_If_User_Already_Exists() {
        String email = getAuthRegisterRequest().getEmail();

        given(userRepository.existsByEmail(email)).willReturn(true);

        AuthenticationException authenticationException = Assertions.assertThrows(AuthenticationException.class,
                () -> authService.register(getAuthRegisterRequest()));

        String expectedMessage = "Пользователь уже существует";
        String actualMessage = authenticationException.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).existsByEmail(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Зарегистрироваться")
    void register() {
        String email = getAuthRegisterRequest().getEmail();
        UserEntity user = UserEntity.builder()
                .email(email)
                .password("test")
                .build();
        AuthRegisterRequest authRegisterRequest = getAuthRegisterRequest();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(roleRepository.getByName("ROLE_USER")).willReturn(getRoleUser());
        given(userDtoMapper.mapAuthRequestToUserEntity(authRegisterRequest)).willReturn(user);
        given(passwordEncoder.encode(authRegisterRequest.getPassword())).willReturn("test");
        given(userRepository.save(user)).willReturn(user);
        given(jwtProvider.generateToken(anyString())).willReturn("token");

        AuthResponse authResponse = authService.register(authRegisterRequest);

        String expectedEmail = "test@test.com";
        String actualEmail = authResponse.getEmail();

        Assertions.assertNotNull(authResponse);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(userRepository, times(1)).save(user);
        verify(userDtoMapper, times(1)).mapAuthRequestToUserEntity(authRegisterRequest);
        verify(passwordEncoder, times(1)).encode(authRegisterRequest.getPassword());
        verify(roleRepository, times(1)).getByName(anyString());
        verify(jwtProvider, times(1)).generateToken(anyString());
        verifyNoMoreInteractions(userDtoMapper);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(roleRepository);
        verifyNoMoreInteractions(jwtProvider);
    }

    @Test
    @DisplayName("Залогиниться, если пользователя не существует")
    void login_If_User_Not_Found() {
        String email = "test@test.com";
        AuthLoginRequest authLoginRequest = getAuthLoginRequest();

        given(userRepository.existsByEmail(email)).willReturn(false);

        AuthUserNotFoundException authenticationException = Assertions.assertThrows(AuthUserNotFoundException.class,
                () -> authService.login(authLoginRequest));

        String expectedMessage = String.format("Пользователь %s не найден", email);
        String actualMessage = authenticationException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Залогиниться, если неверный пароль")
    void login_If_Password_Is_Incorrect() {
        AuthLoginRequest authLoginRequest = getAuthLoginRequest();
        UserEntity userFromDb = getUserFromDb();
        String email = userFromDb.getEmail();

        given(userRepository.findByEmail(email)).willReturn(java.util.Optional.of(userFromDb));
        given(passwordEncoder.matches(authLoginRequest.getPassword(), userFromDb.getPassword())).willReturn(false);

        AuthenticationException authenticationException = Assertions.assertThrows(AuthenticationException.class,
                () -> authService.login(authLoginRequest));

        String expectedMessage = "Неверный пароль!";
        String actualMessage = authenticationException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(authLoginRequest.getPassword(), userFromDb.getPassword());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Залогиниться")
    void login() {
        AuthLoginRequest authLoginRequest = getAuthLoginRequest();
        UserEntity userFromDb = getUserFromDb();
        String email = userFromDb.getEmail();

        given(userRepository.findByEmail(email)).willReturn(java.util.Optional.of(userFromDb));
        given(passwordEncoder.matches(authLoginRequest.getPassword(), userFromDb.getPassword())).willReturn(true);
        given(jwtProvider.generateToken(anyString())).willReturn("token");

        AuthResponse login = authService.login(authLoginRequest);

        String expectedEmail = "test@test.com";
        String actualEmail = login.getEmail();

        Assertions.assertNotNull(login);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(authLoginRequest.getPassword(), userFromDb.getPassword());
        verify(jwtProvider, times(1)).generateToken(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    private UserEntity getUserFromDb() {
        return UserEntity.builder()
                .username("test")
                .email("test@test.com")
                .password("test")
                .build();
    }

    private AuthLoginRequest getAuthLoginRequest() {
        return AuthLoginRequest.builder()
                .email("test@test.com")
                .username("test")
                .password("test")
                .build();
    }

    private RoleEntity getRoleUser() {
        return new RoleEntity("ROLE_USER");
    }

    private AuthRegisterRequest getAuthRegisterRequest() {
        return AuthRegisterRequest.builder()
                .username("test")
                .email("test@test.com")
                .password("test")
                .build();
    }
}
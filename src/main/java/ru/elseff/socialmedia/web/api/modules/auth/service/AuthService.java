package ru.elseff.socialmedia.web.api.modules.auth.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
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

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;

    RoleRepository roleRepository;

    UserDtoMapper userDtoMapper;

    PasswordEncoder passwordEncoder;

    JwtProvider jwtProvider;

    public AuthResponse register(AuthRegisterRequest authRegisterRequest) {
        if (userRepository.existsByEmail(authRegisterRequest.getEmail())
                || userRepository.existsByUsername(authRegisterRequest.getUsername())) {
            log.warn("Пользователь уже существует");
            throw new AuthenticationException("Пользователь уже существует");
        }

        RoleEntity roleUser = roleRepository.getByName("ROLE_USER");
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleUser);

        UserEntity user = userDtoMapper.mapAuthRequestToUserEntity(authRegisterRequest);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);

        UserEntity userFromDb = userRepository.save(user);
        log.info("Пользователь {} успешно зарегистрировался", userFromDb.getEmail());
        String token = jwtProvider.generateToken(user.getEmail());

        return new AuthResponse(userFromDb.getId(), authRegisterRequest.getEmail(), authRegisterRequest.getUsername(), token);
    }

    public AuthResponse login(AuthLoginRequest authLoginRequest) {
        String requestEmail = authLoginRequest.getEmail();
        String requestUsername = authLoginRequest.getUsername();

        UserEntity userFromDb;
        boolean existsByEmail = false;
        if (requestUsername == null && requestEmail == null)
            throw new AuthUserNotFoundException("Введите имя или электронную почту");
        else if (requestEmail != null) {
            userFromDb = userRepository.findByEmail(requestEmail)
                    .orElseThrow(() -> new AuthUserNotFoundException("Пользователь " + requestEmail + " не найден"));
            existsByEmail = true;

        } else {
            userFromDb = userRepository.findByUsername(requestUsername)
                    .orElseThrow(() -> new AuthUserNotFoundException("Пользователь " + requestUsername + " не найден"));
        }

        String requestPassword = authLoginRequest.getPassword();
        String actualPassword = userFromDb.getPassword();

        if (!passwordEncoder.matches(requestPassword, actualPassword)) {
            log.info("Неверный пароль!");
            throw new AuthenticationException("Неверный пароль!");
        }

        if (existsByEmail) {
            String token = jwtProvider.generateToken(requestEmail);
            log.info("Пользователь {} успешно вошёл", requestEmail);
            return new AuthResponse(userFromDb.getId(), requestEmail, null, token);
        } else {
            String token = jwtProvider.generateToken(requestUsername);
            log.info("Пользователь {} успешно вошёл", requestUsername);
            return new AuthResponse(userFromDb.getId(), null, requestUsername, token);

        }
    }
}

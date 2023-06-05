package ru.elseff.socialmedia.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.UserRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Pattern pattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"); // is string email regex
        Matcher matcher = pattern.matcher(username);

        boolean isEmail = matcher.matches();


        UserEntity user;

        if (isEmail)
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
        else
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));

        UserDetailsImpl userDetails = UserDetailsImpl.toUserDetails(user);

        return userDetails;
    }
}

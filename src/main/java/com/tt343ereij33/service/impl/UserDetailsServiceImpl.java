package com.tt343ereij33.service.impl;

import com.tt343ereij33.entity.Role;
import com.tt343ereij33.entity.User;
import com.tt343ereij33.exceptions.UserCreationException;
import com.tt343ereij33.repository.impl.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

import static com.tt343ereij33.utils.SqlErrorMessageConstants.EMAIL_ALREADY_EXISTS;
import static com.tt343ereij33.utils.SqlErrorMessageConstants.USERNAME_ALREADY_EXISTS;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDAO userDAO;

    @Autowired
    public UserDetailsServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userDAO.readByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Username not found");
        }
    }

    public boolean register(String username, String email, String password) throws UserCreationException {
        try {
            return userDAO.create(User.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .role(Role.ROLE_USER)
                    .build());
        } catch (SQLException e) {
            if (e.getMessage().contains(USERNAME_ALREADY_EXISTS)) {
                throw new UserCreationException("Username already exists");
            } else if (e.getMessage().contains(EMAIL_ALREADY_EXISTS)) {
                throw new UserCreationException("Email already exists");
            }
            throw new UserCreationException("Unknown error during registration occurred");
        }
    }
}

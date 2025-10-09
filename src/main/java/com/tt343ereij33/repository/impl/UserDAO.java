package com.tt343ereij33.repository.impl;

import com.tt343ereij33.entity.enums.Role;
import com.tt343ereij33.entity.UserEntity;
import com.tt343ereij33.repository.BaseDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDAO implements BaseDAO<UserEntity> {
    private static final String CREATE_USER = "INSERT INTO users (username, first_name, last_name, password, email, role) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String READ_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String READ_USER_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
    private static final String READ_USER_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String READ_ALL_USER = "SELECT * FROM users";
    private static final String UPDATE_USER = "UPDATE users SET username = ?, first_name = ?, last_name = ?, password = ?, email = ?, verified = ?, role = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";
    private static final String EXISTS_BY_USERNAME = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
    private static final String EXISTS_BY_EMAIL = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
    private final DataSource dataSource;
    // TODO: Move passwordEncoder to service layer
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean create(UserEntity user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CREATE_USER)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getPassword() != null ? encryptPassword(user.getPassword()) : null);
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.setString(6, user.getRole().getAuthority());
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    @Override
    public Optional<UserEntity> readById(Serializable id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(READ_USER_BY_ID)) {
            preparedStatement.setLong(1, Long.parseLong(id.toString()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildUser(resultSet));
            }
            return Optional.empty();
        }
    }

    public Optional<UserEntity> readByUsername(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(READ_USER_BY_USERNAME)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildUser(resultSet));
            }
            return Optional.empty();
        }
    }

    public Optional<UserEntity> readByEmail(String email) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(READ_USER_BY_EMAIL)) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildUser(resultSet));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<UserEntity> readAll() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(READ_ALL_USER);
            List<UserEntity> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        }
    }

    @Override
    public boolean update(UserEntity user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getPassword() != null ? encryptPassword(user.getPassword()) : null);
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.setBoolean(6, user.isVerified());
            preparedStatement.setString(7, user.getRole().getAuthority());
            preparedStatement.setLong(8, user.getId());
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    @Override
    public boolean delete(Serializable id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_USER)) {
            preparedStatement.setLong(1, Long.parseLong(id.toString()));
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    public boolean existsByUsername(String username) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(EXISTS_BY_USERNAME)) {
           preparedStatement.setString(1, username);
           return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            // TODO: Handle it properly
            return false;
        }
    }

    public boolean existsByEmail(String email) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(EXISTS_BY_EMAIL)) {
            preparedStatement.setString(1, email);
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            // TODO: Handle it properly

            return false;
        }
    }

    private UserEntity buildUser(ResultSet resultSet) throws SQLException {
        return UserEntity
                .builder()
                .id(resultSet.getLong("id"))
                .username(resultSet.getString("username"))
                .firstName(resultSet.getString("first_name"))
                .lastName(resultSet.getString("last_name"))
                .password(resultSet.getString("password"))
                .email(resultSet.getString("email"))
                .verified(resultSet.getBoolean("verified"))
                .role(Role.valueOf(resultSet.getString("role")))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}

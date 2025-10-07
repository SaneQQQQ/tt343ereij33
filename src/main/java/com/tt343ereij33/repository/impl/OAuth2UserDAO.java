package com.tt343ereij33.repository.impl;

import com.tt343ereij33.entity.OAuth2User;
import com.tt343ereij33.repository.BaseDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
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
public class OAuth2UserDAO implements BaseDAO<OAuth2User> {
    private static final String CREATE_OAUTH2_USER = "INSERT INTO oauth2_users (provider, provider_user_id, user_id) VALUES (?, ?, ?)";
    private static final String READ_OAUTH2_USER_BY_ID = "SELECT * FROM oauth2_users WHERE id = ?";
    private static final String READ_OAUTH2_USER_BY_USER_ID = "SELECT * FROM oauth2_users WHERE user_id = ?";
    private static final String READ_ALL_OAUTH2_USERS = "SELECT * FROM oauth2_users";
    private static final String UPDATE_OAUTH2_USER = "UPDATE oauth2_users SET provider = ?, provider_user_id = ?, user_id = ? WHERE id = ?";
    private static final String DELETE_OAUTH2_USER = "DELETE FROM oauth2_users WHERE id = ?";
    private static final String DELETE_OAUTH2_USER_BY_USER_ID = "DELETE FROM oauth2_users WHERE user_id = ?";
    private final DataSource dataSource;
    private final UserDAO userDAO;

    @Override
    public boolean create(OAuth2User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CREATE_OAUTH2_USER)) {
            preparedStatement.setString(1, user.getProvider().toString());
            preparedStatement.setString(2, user.getProviderUserId());
            preparedStatement.setLong(3, user.getUser().getId());
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    @Override
    public Optional<OAuth2User> readById(Serializable id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(READ_OAUTH2_USER_BY_ID)) {
            preparedStatement.setLong(1, Long.parseLong(id.toString()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildUser(resultSet));
            }
            return Optional.empty();
        }
    }

    public Optional<OAuth2User> readByUserId(Serializable id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(READ_OAUTH2_USER_BY_USER_ID)) {
            preparedStatement.setLong(1, Long.parseLong(id.toString()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildUser(resultSet));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<OAuth2User> readAll() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(READ_ALL_OAUTH2_USERS);
            List<OAuth2User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        }
    }

    @Override
    public boolean update(OAuth2User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_OAUTH2_USER)) {
            preparedStatement.setString(1, user.getProvider().toString());
            preparedStatement.setString(2, user.getProviderUserId());
            preparedStatement.setLong(3, user.getUser().getId());
            preparedStatement.setLong(4, user.getId());
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    @Override
    public boolean delete(Serializable id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_OAUTH2_USER)) {
            preparedStatement.setLong(1, Long.parseLong(id.toString()));
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    public boolean deleteByUserId(Serializable id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_OAUTH2_USER_BY_USER_ID)) {
            preparedStatement.setLong(1, Long.parseLong(id.toString()));
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    private OAuth2User buildUser(ResultSet resultSet) throws SQLException {
        return OAuth2User
                .builder()
                .id(resultSet.getLong("id"))
                .provider(CommonOAuth2Provider.valueOf(resultSet.getString("provider")))
                .providerUserId(resultSet.getString("provider_user_id"))
                .user(userDAO.readById(resultSet.getLong("user_id")).orElse(null))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}


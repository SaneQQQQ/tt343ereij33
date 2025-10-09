package com.tt343ereij33.repository.impl;

import com.tt343ereij33.entity.OAuth2UserEntity;
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
public class OAuth2UserDAO implements BaseDAO<OAuth2UserEntity> {
    private static final String CREATE_OAUTH2_USER = "INSERT INTO oauth2_users (provider, provider_user_id, user_id) VALUES (?, ?, ?)";
    private static final String READ_OAUTH2_USER_BY_ID = "SELECT * FROM oauth2_users WHERE id = ?";
    private static final String READ_OAUTH2_USER_BY_USER_ID = "SELECT * FROM oauth2_users WHERE user_id = ?";
    private static final String READ_OAUTH2_USER_BY_PROVIDER_ID = "SELECT * FROM oauth2_users WHERE provider = ? AND provider_user_id = ?";
    private static final String READ_ALL_OAUTH2_USERS = "SELECT * FROM oauth2_users";
    private static final String UPDATE_OAUTH2_USER = "UPDATE oauth2_users SET provider = ?, provider_user_id = ?, user_id = ? WHERE id = ?";
    private static final String DELETE_OAUTH2_USER = "DELETE FROM oauth2_users WHERE id = ?";
    private static final String DELETE_OAUTH2_USER_BY_USER_ID = "DELETE FROM oauth2_users WHERE user_id = ?";
    private static final String DELETE_OAUTH2_USER_BY_PROVIDER_ID = "DELETE FROM oauth2_users WHERE provider = ? AND provider_user_id = ?";
    private static final String EXISTS_BY_PROVIDER_ID = "SELECT 1 FROM oauth2_users WHERE provider = ? AND provider_user_id = ? LIMIT 1";
    private final DataSource dataSource;
    private final UserDAO userDAO;

    @Override
    public boolean create(OAuth2UserEntity user) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CREATE_OAUTH2_USER)) {
            preparedStatement.setString(1, user.getProvider().toString());
            preparedStatement.setString(2, user.getProviderUserId());
            preparedStatement.setLong(3, user.getUser().getId());
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        } catch (SQLException e) {
            // TODO: Handle it properly
            return false;
        }
    }

    @Override
    public Optional<OAuth2UserEntity> readById(Serializable id) throws SQLException {
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

    public Optional<OAuth2UserEntity> readByUserId(Serializable id) throws SQLException {
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

    public Optional<OAuth2UserEntity> readByProviderId(CommonOAuth2Provider provider, Serializable providerId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(READ_OAUTH2_USER_BY_PROVIDER_ID)) {
            preparedStatement.setString(1, provider.name());
            preparedStatement.setLong(2, Long.parseLong(providerId.toString()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildUser(resultSet));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<OAuth2UserEntity> readAll() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(READ_ALL_OAUTH2_USERS);
            List<OAuth2UserEntity> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        }
    }

    @Override
    public boolean update(OAuth2UserEntity user) throws SQLException {
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

    public boolean deleteByProviderId(CommonOAuth2Provider provider, Serializable providerId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_OAUTH2_USER_BY_PROVIDER_ID)) {
            preparedStatement.setString(1, provider.name());
            preparedStatement.setLong(2, Long.parseLong(providerId.toString()));
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    public boolean existsByProviderId(CommonOAuth2Provider provider, String providerId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(EXISTS_BY_PROVIDER_ID)) {
            preparedStatement.setString(1, provider.name());
            preparedStatement.setString(2, providerId);
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            // TODO: handle it somehow properly
            return false;
        }
    }

    private OAuth2UserEntity buildUser(ResultSet resultSet) throws SQLException {
        return OAuth2UserEntity
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


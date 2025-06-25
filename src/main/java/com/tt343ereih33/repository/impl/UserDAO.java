package com.tt343ereih33.repository.impl;

import com.tt343ereih33.entity.User;
import com.tt343ereih33.repository.BaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserDAO implements BaseDAO<User> {
    private static final String CREATE_USER = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
    private static final String READ_USER = "SELECT * FROM users WHERE id = ?";
    private static final String READ_ALL_USER = "SELECT * FROM users";
    private static final String UPDATE_USER = "UPDATE users SET username = ?, email = ?, password_hash = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";
    private final DataSource dataSource;

    @Autowired
    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean create(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CREATE_USER)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            int isSuccessful = preparedStatement.executeUpdate();
            return isSuccessful > 0;
        }
    }

    @Override
    public Optional<User> read(Serializable id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(READ_USER)) {
            preparedStatement.setLong(1, Long.parseLong(id.toString()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildUser(resultSet));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<User> readAll() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(READ_ALL_USER);
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        }
    }

    @Override
    public boolean update(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setLong(4, user.getId());
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

    private User buildUser(ResultSet resultSet) throws SQLException {
        return User
                .builder()
                .id(resultSet.getLong("id"))
                .username(resultSet.getString("username"))
                .email(resultSet.getString("email"))
                .password(resultSet.getString("password_hash"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}

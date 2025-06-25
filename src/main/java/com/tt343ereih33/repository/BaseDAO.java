package com.tt343ereih33.repository;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BaseDAO<T> {
    boolean create(T t) throws SQLException;

    Optional<T> read(Serializable id) throws SQLException;

    List<T> readAll() throws SQLException;

    boolean update(T t) throws SQLException;

    boolean delete(Serializable id) throws SQLException;
}

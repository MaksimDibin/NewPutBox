package ru.dibin.implementsClass;

import java.sql.Connection;
import java.sql.SQLException;

public interface BD {
    Connection startBD();

    String createAnAccount(String nickName, String login, String password) throws SQLException;

    String signIn(String login, String password) throws SQLException;

    void deletingAnAccount(String login, String password) throws SQLException;
}

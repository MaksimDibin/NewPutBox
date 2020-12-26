package ru.dibin.connectingDB;

import java.sql.SQLException;

public interface Connecting {
    String start() throws SQLException;

    void repeatedAuthorization();

    void repeatedCreateAnAccount(String login, String password) throws SQLException;

    void closeScanner();
}

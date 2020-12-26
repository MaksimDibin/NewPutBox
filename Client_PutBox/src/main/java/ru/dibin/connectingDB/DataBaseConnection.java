package ru.dibin.connectingDB;

import java.io.FileInputStream;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DataBaseConnection implements BD {
    static Logger LOGGER;
    public ConnectingToOutbox connectingToOutbox;

    static {
        try (FileInputStream ins = new FileInputStream ( "C:\\PutBox\\log.config" )) {
            LogManager.getLogManager ( ).readConfiguration ( ins );
            LOGGER = Logger.getLogger ( DataBaseConnection.class.getName ( ) );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }

    @Override
    public Connection startBD() {

        Connection connection = null;

        assert false;

        try {
            Class.forName ( "com.mysql.jdbc.Driver" );
        } catch (ClassNotFoundException e) {
            LOGGER.log ( Level.WARNING, "Не получилось подключить jdbc.Driver" );
        }
        try {
            DriverManager.registerDriver ( new com.mysql.jdbc.Driver ( ) );
            connection = DriverManager.getConnection ( "jdbc:mysql://localhost:3306/base_auth", "root", "" );
        } catch (SQLException throwables) {
            LOGGER.log ( Level.WARNING, "Не получилось подключиться к баазе данных \"base_auth\"." );
        }
        return connection;
    }

    @Override
    public String createAnAccount(String nickName, String login, String password) throws SQLException {

        Connection connection = startBD ( );
        PreparedStatement prepareStatement =
                connection.prepareStatement ( "SELECT nickName FROM bd where nickName = ?" ); // решить вопрос!!! Есть два пользователя с разными никами, но при этом одинаковыми логинами и паролями.
        prepareStatement.setString ( 1, nickName );
        ResultSet resultSet = prepareStatement.executeQuery ( );
        while (resultSet.next ( )) {
            if (resultSet.getString ( "nickName" ) != null) {
                System.out.println ( "Пользователь с таким ником уже есть." );
                connectingToOutbox.repeatedCreateAnAccount ( login, password );
            } else {
                connection.setAutoCommit ( false );
                try {
                    PreparedStatement preparedStatement =
                            connection.prepareStatement ( "INSERT INTO bd (nickName, login, password)"
                                    + " VALUES ( ?, ?, ? )" );
                    preparedStatement.setString ( 1, nickName );
                    preparedStatement.setString ( 2, login );
                    preparedStatement.setString ( 3, password );
                    preparedStatement.executeUpdate ( );
                    connection.commit ( );
                    LOGGER.log ( Level.INFO, "Клиент под ником " + nickName + " зарегистрировался." );
                } catch (SQLException e) {
                    connection.rollback ( );
                }
                prepareStatement.close ( );
                connection.close ( );
            }
        }
        return nickName;
    }

    @Override
    public String signIn(String login, String password) throws SQLException {

        Connection connection = startBD ( );
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement ( "SELECT nickName  FROM bd where login = ?" +
                " and password = ?" ); // Проблема с двумя пользователями осталось открытой, надо решить.
        preparedStatement.setString ( 1, login );
        preparedStatement.setString ( 2, password );
        ResultSet resultSet = preparedStatement.executeQuery ( );
        while (resultSet.next ( )) {
            if (resultSet.getString ( "nickName" ) != null) {
                LOGGER.log ( Level.INFO, "Клиент под ником " + resultSet.getString ( "nickName" )
                        + " вошел в систему." );
                preparedStatement.close ( );
                return resultSet.getString ( "nickName" );
            } else {
                System.out.println ( "Неверный логин или пароль." );
                connectingToOutbox.repeatedAuthorization ( );
            }
        }
        try {
            preparedStatement.close ( );
            connection.close ( );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
        return null;
    }

    @Override
    public void deletingAnAccount(String login, String password) throws SQLException {
        Connection connection = startBD ( );
        PreparedStatement preparedStatement;
        connection.setAutoCommit ( false );
        try {
            preparedStatement = connection.prepareStatement ( "DELETE FROM bd where login = ? and password = ?" );
            preparedStatement.setString ( 1, login );
            preparedStatement.setString ( 2, password );
            int val = preparedStatement.executeUpdate ( );
            connection.commit ( );
            System.out.println ( "Аккаунт удален." );//Позже реализую удаление папки личной после удаление аккаунта.

            preparedStatement.close ( );
            connection.close ( );
        } catch (SQLException e) {
            System.out.println ( "Произошла ошибка, повторите команду позже." );
            connection.rollback ( );
        }
    }
}

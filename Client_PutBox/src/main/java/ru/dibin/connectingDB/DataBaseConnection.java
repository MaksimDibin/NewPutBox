package ru.dibin.connectingDB;

import ru.dibin.implementsClass.BD;

import java.io.FileInputStream;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DataBaseConnection implements BD {

    static Logger LOGGER;
    public Connection connection = startBD ( );

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

        try {
            Class.forName ( "com.mysql.jdbc.Driver" );
        } catch (ClassNotFoundException e) {
            LOGGER.log ( Level.WARNING, "Не получилось подключить jdbc.Driver" );
        }
        try {
            DriverManager.registerDriver ( new com.mysql.jdbc.Driver ( ) );
            connection = DriverManager.getConnection ( "jdbc:mysql://localhost:3306/base_auth?useSSL=false", "root", "" );
        } catch (SQLException throwables) {
            LOGGER.log ( Level.WARNING, "Не получилось подключиться к баазе данных \"base_auth\"" );
        }
        return connection;
    }

    @Override
    public String createAnAccount(String nickName, String login, String password) throws SQLException {

        PreparedStatement prepareStatement =
                connection.prepareStatement ( "SELECT nickName FROM bd where nickName = ?" ); // решить вопрос!!! Есть два пользователя с разными никами, но при этом одинаковыми логинами и паролями.
        prepareStatement.setString ( 1, nickName );
        ResultSet resultSet = prepareStatement.executeQuery ( );
        if (resultSet.next ( )) {
            System.out.println ( "Пользователь с таким ником уже есть" );
            return null;
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
                LOGGER.log ( Level.INFO, "Клиент под ником " + nickName + " зарегистрировался" );
            } catch (SQLException e) {
                connection.rollback ( );
            }
            prepareStatement.close ( );
            connection.close ( );
        }
        return nickName;
    }

    @Override
    public String signIn(String login, String password) throws SQLException {

        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement ( "SELECT nickName  FROM bd where login = ?" +
                " and password = ?" ); // Проблема с двумя пользователями осталось открытой, надо решить.
        preparedStatement.setString ( 1, login );
        preparedStatement.setString ( 2, password );
        ResultSet resultSet = preparedStatement.executeQuery ( );
        if (resultSet.next ( )) {
            LOGGER.log ( Level.INFO, "Клиент под ником " + resultSet.getString ( "nickName" )
                    + " вошел в систему." );
            return resultSet.getString ( "nickName" );
        }
        try {
            preparedStatement.close ( );
            connection.close ( );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
        System.out.println ( "Неправильный логин или пароль" );
        return null;
    }

    @Override
    public String deletingAnAccount(String login, String password) throws SQLException {

        String nickName = signIn ( login, password );
        PreparedStatement preparedStatement;
        connection.setAutoCommit ( false );
        try {
            preparedStatement = connection.prepareStatement ( "DELETE FROM bd where login = ? and password = ?" );
            preparedStatement.setString ( 1, login );
            preparedStatement.setString ( 2, password );
            preparedStatement.executeUpdate ( );
            connection.commit ( );
            System.out.println ( "Аккаунт удален" );
            preparedStatement.close ( );
            connection.close ( );
        } catch (SQLException e) {
            LOGGER.log ( Level.WARNING, "Произошла ошибка, повторите команду позже" );
            connection.rollback ( );
        }
        return nickName;
    }
}
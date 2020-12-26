package ru.dibin.connectingDB;

import java.sql.SQLException;
import java.util.Scanner;

public class ConnectingToOutbox implements Connecting {

    DataBaseConnection dbc = new DataBaseConnection ( );
    ConnectingToOutbox cto = new ConnectingToOutbox ( );
    Scanner sc = new Scanner ( System.in );
    String nickName;
    String login;
    String password;

    @Override
    public String start() throws SQLException {
        System.out.printf ( "Добро пожаловать в PutBox, для авторизации введите цифру %d," +
                " для созданние аккаунта введите цифру %d," +
                " для удаление аакаунта введите цифру %d.", 1, 2, 3 );
        dbc.startBD ( );
        int number = sc.nextInt ( );
        if (number <= 3 && number >= 1) {
            switch (number) {
                case 1:
                    System.out.println ( "Введите логин и пароль." );
                    login = sc.next ( );
                    password = sc.next ( );
                    cto.closeScanner ( );
                    return dbc.signIn ( login, password );
                case 2:
                    System.out.println ( "Введите nickName, логин и пароль." );
                    nickName = sc.next ( );
                    login = sc.next ( );
                    password = sc.next ( );
                    cto.closeScanner ( );
                    return dbc.createAnAccount ( nickName, login, password );
                case 3:
                    System.out.println ( "Для подтверждения удаления аккаунта введите логин и пароль." );
                    login = sc.next ( );
                    password = sc.next ( );
                    dbc.deletingAnAccount ( login, password );
                    cto.closeScanner ( );
                    break;
            }
        } else {
            System.out.println ( "Введите цифру 1, 2 или 3" );
            new ConnectingToOutbox ( );
        }
        return null;
    }

    @Override
    public void repeatedAuthorization() {
        System.out.println ( "Введите логин и пароль" );
        login = sc.next ( );
        password = sc.next ( );
        try {
            dbc.signIn ( login, password );
        } catch (SQLException throwables) {
            throwables.printStackTrace ( );
        }
        cto.closeScanner ( );
    }

    @Override
    public void repeatedCreateAnAccount(String login, String password) throws SQLException {
        System.out.println ( "Введите nickName, логин и пароль." );
        nickName = sc.next ( );
        dbc.createAnAccount ( nickName, login, password );
        cto.closeScanner ( );
    }

    @Override
    public void closeScanner() {
        sc.close ( );
    }
}
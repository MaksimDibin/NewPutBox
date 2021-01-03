package ru.dibin.client;

import ru.dibin.connectingDB.DataBaseConnection;
import ru.dibin.serviceCommand.ServiceCommand;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        new Client ( ).start ( "localhost", 8180 );
    }

    private boolean active = true;
    private boolean success = false;
    private String nickName;

    public void start(String HOST, int PORT) {

        new DataBaseConnection ( ).startBD ( );
        try {
            Socket socket = new Socket ( HOST, PORT );
            DataOutputStream out = new DataOutputStream ( socket.getOutputStream ( ) );
            DataInputStream input = new DataInputStream ( socket.getInputStream ( ) );
            Scanner sc = new Scanner ( System.in );
            System.out.printf ( "Добро пожаловать в PutBox, для авторизации введите цифру %d," +
                    " для созданние аккаунта введите цифру %d," +
                    " для удаление аакаунта введите цифру %d.\n", 1, 2, 3 );
            int number = sc.nextInt ( );
            if (number <= 3 && number >= 1) {
                String login;
                String password;
                switch (number) {
                    case 1:
                        while (!success) {
                            System.out.println ( "Введите логин" );
                            login = sc.next ( );
                            System.out.println ( "Введите пароль" );
                            password = sc.next ( );
                            nickName = new DataBaseConnection ( ).signIn ( login, password );
                            if (nickName != null) success = true;
                        }
                        break;
                    case 2:
                        while (!success) {
                            System.out.println ( "Введите nickName" );
                            String nick = sc.next ( );
                            System.out.println ( "Введите логин" );
                            login = sc.next ( );
                            System.out.println ( "Введите пароль" );
                            password = sc.next ( );
                            nickName = new DataBaseConnection ( ).createAnAccount ( nick, login, password );
                            if (nickName != null) success = true;
                        }
                        break;
                    case 3:
                        while (!success) {
                            System.out.println ( "Для подтверждения удаления аккаунта введите логин" );
                            login = sc.next ( );
                            System.out.println ( "и пароль" );
                            password = sc.next ( );
                            nickName = new DataBaseConnection ( ).deletingAnAccount ( login, password );
                            new ServiceCommand ( nickName, input, out ).deleteWorkFolder ( );
                            if (nickName != null){
                                new ServiceCommand ( "quite", nickName, input, out ).command ( );
                                System.exit ( 0 );
                            }
                            System.out.println ( "Неправильный логин или пароль" );
                        }
                        break;
                }
            } else {
                System.out.println ( "Введите цифру 1, 2 или 3" );
                start ( HOST, PORT );
                return;
            }

            while (active) {
                System.out.println ( "Введите команду" );
                System.out.println ( "Если нужна помощь, введите \"help\"" );
                String command = sc.next ( );
                active = new ServiceCommand ( command, nickName, input, out ).command ( );
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace ( );
        }
    }
}
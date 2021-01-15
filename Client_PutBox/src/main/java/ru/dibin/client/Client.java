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
    private String nickName;
    private Socket socket;
    private boolean isSuccess = false;

    public void start(String HOST, int PORT) {

//        new DataBaseConnection ( ).startBD ( ); Не удалять!
        try {
            socket = new Socket ( HOST, PORT );
            DataOutputStream out = new DataOutputStream ( socket.getOutputStream ( ) );
            DataInputStream input = new DataInputStream ( socket.getInputStream ( ) );
            Scanner sc = new Scanner ( System.in );
            System.out.printf ( "Добро пожаловать в PutBox, для авторизации введите цифру %d," +
                    " для созданние аккаунта введите цифру %d," +
                    " для удаление аакаунта введите цифру %d," +
                    " Даниил, для вас введите цифру %d.\n", 1, 2, 3, 4 );
            int number = sc.nextInt ( );
            if (number <= 4 && number >= 1) {
                String login;
                String password;
                switch (number) {
                    case 1:
                        new Thread ( this::timiIsOut ).start ( );
                        while (!isSuccess) {
                            System.out.println ( "Введите логин" );
                            login = sc.next ( );
                            System.out.println ( "Введите пароль" );
                            password = sc.next ( );
                            nickName = new DataBaseConnection ( ).signIn ( login, password );
                            if (nickName != null) isSuccess = true;
                        }
                        break;
                    case 2:
                        while (!isSuccess) {
                            System.out.println ( "Введите nickName" );
                            String nick = sc.next ( );
                            System.out.println ( "Введите логин" );
                            login = sc.next ( );
                            System.out.println ( "Введите пароль" );
                            password = sc.next ( );
                            nickName = new DataBaseConnection ( ).createAnAccount ( nick, login, password );
                            if (nickName != null) isSuccess = true;
                        }
                        break;
                    case 3:
                        while (!isSuccess) {
                            System.out.println ( "Для подтверждения удаления аккаунта введите логин" );
                            login = sc.next ( );
                            System.out.println ( "и пароль" );
                            password = sc.next ( );
                            nickName = new DataBaseConnection ( ).deletingAnAccount ( login, password );
                            if (nickName != null) {
                                new ServiceCommand ( "deleteAll", nickName, input, out ).command ( );
                                new ServiceCommand ( "quite", nickName, input, out ).command ( );
                                System.exit ( 0 );
                            } else System.out.println ( "Неправильный логин или пароль" );
                        }
                        break;
                    case 4:
                        nickName = "Daniil";
                        System.out.println ( "Обратите внимание! В классе ProtoHandler, CopyCommand нужно изменить путь к папке" );
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
            System.exit ( 0 );
        } catch (IOException | SQLException e) {
            e.printStackTrace ( );
        }
    }

    public void timiIsOut() {
        try {
            Thread.sleep ( 120_000 );
            if (!isSuccess) {
                this.socket.close ( );
                System.exit ( 0 );
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace ( );
        }
    }
}
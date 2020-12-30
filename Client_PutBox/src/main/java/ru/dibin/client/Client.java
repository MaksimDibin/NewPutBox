package ru.dibin.client;

import ru.dibin.connectingDB.DataBaseConnection;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client {

    static Logger LOGGER;

    static {
        try (FileInputStream ins = new FileInputStream ( "C:\\PutBox\\log.config" )) {
            LogManager.getLogManager ( ).readConfiguration ( ins );
            LOGGER = Logger.getLogger ( Client.class.getName ( ) );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
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
                        System.out.println ( "Для подтверждения удаления аккаунта введите логин" );
                        login = sc.next ( );
                        System.out.println ( "и пароль" );
                        password = sc.next ( );
                        new DataBaseConnection ( ).deletingAnAccount ( login, password );
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
                if (command.equals ( "help" )) {
                    System.out.println ( "Для работы с приложением необходимо вводить служебные команды." +
                            "Для отправки файла или каталога нужно написать \"transfer_to\"" +
                            " Для просмотра списка каталогов и файлов введите \"transition\"" +
                            ". Для копирование файла или каталога введите \"copy\"" +
                            " Для выхода из \"PutBox\" введите \"quite\"\n" );
                }

                if (command.equals ( "transition" )) {


                }

                if (command.equals ( "quite" )) {
                    active = false;
                    LOGGER.log ( Level.INFO, nickName + " вышел из приложения" );
                    sc.close ( );
                }

                if (command.equals ( "transfer_to" )) {
                    System.out.println ( "Укажите путь к файлу, который хотите передать" );
                    String str = sc.next ( );
                    Path path = Paths.get ( str );
                    out.writeByte ( 13 );
                    int workFolderLength = nickName.length ( );
                    out.writeInt ( workFolderLength );
                    out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
                    String fileName = path.getFileName ( ).toString ( );
                    int fileLength = fileName.length ( );
                    out.writeInt ( fileLength );
                    out.write ( fileName.getBytes ( StandardCharsets.UTF_8 ) );
                    long fileSize = Files.size ( path );
                    out.writeLong ( fileSize );
                    byte[] bytes = new byte[ 256 ];
                    try (InputStream inputStream = new FileInputStream ( path.toFile ( ) )) {
                        int array;
                        while ((array = inputStream.read ( bytes )) != 1) {
                            out.write ( bytes, 0, array );
                        }
                    }
                }

                if (command.equals ( "copy" )) {
                    System.out.println ( "Введите название файла с расширением! Пример test.txt" );
                    String str = sc.next ();
                    out.write ( 14 );
                    int workFolderLength = nickName.length ( );
                    out.writeInt ( workFolderLength );
                    out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
                    int fileNameLength = str.length ( );
                    out.writeInt ( fileNameLength );
                    out.write ( str.getBytes ( ) );
                    byte inputByte = input.readByte ( );
                    if (inputByte == 14) {
                        System.out.println ( "Файл найден, идет загрузка..." );
                    } else {
                        System.out.println ( "Файл не найден, проверьте правильно ввели имя файла" );
                        return;
                    }
                    long fileSize = input.readLong ( );
                    Path path = Paths.get ( nickName, str );
                    try (BufferedOutputStream bufferedOutputStream
                                 = new BufferedOutputStream ( new FileOutputStream ( path.toFile ( ) ) )) {
                        for ( int i = 0 ; i < fileSize ; i++ ) {
                            bufferedOutputStream.write ( input.readByte ( ) );
                        }
                    }
                    System.out.println ( "Файл успешно загружен" );
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace ( );
        }
    }
}
package ru.dibin.client;

import ru.dibin.connectingDB.ConnectingToOutbox;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Scanner;

public class Client {

    public boolean active = true;

    public void start(String HOST, int PORT) {
        String nickName = null;

        try {
            Socket socket = new Socket ( HOST, PORT );
            DataOutputStream out = new DataOutputStream ( socket.getOutputStream ( ) );
            Scanner scanner = new Scanner ( System.in );
            try {
                nickName = new ConnectingToOutbox ( ).start ( );
            } catch (SQLException throwables) {
                throwables.printStackTrace ( );
            }

            while (active) {
                System.out.println ( "Введите команду" );
                System.out.println ( "Если нужна помощь, введите \"help\"" );
                String command = scanner.next ( ) + " " + nickName;
                String[] serviceCommands = command.split ( " " );
                if (serviceCommands[ 0 ].equals ( "help" )) {
                    System.out.println ( "Для работы с приложением необходимо вводить служебные команды." +
                            "Для отправки файла или каталога нужно написать \"transfer_to\"," +
                            " затем нажать \"Enter\" и написать путь, где лежит файл или каталог" +
                            ", после снова нажать \"Enter\". Для просмотра списка каталогов и файлов введите \"transition\"" +
                            " и нажмите \"Enter\". Для копирование файла или каталога" +
                            " введите \"copy\" + пробел + название каталога или файла. Например \"copy test1.txt\"." +
                            " Для выхода из \"PutBox\" введите \"quite\", затем \"Enter\"." );
                }
                if (serviceCommands[ 0 ].equals ( "quite" )) {
                    active = false;
                } if (serviceCommands[ 0 ].equals ("transfer_to")){


                    out.write ( command.getBytes ( StandardCharsets.UTF_8 ) );
                }
            }

        } catch (IOException e) {
            e.printStackTrace ( );
        }
    }
}

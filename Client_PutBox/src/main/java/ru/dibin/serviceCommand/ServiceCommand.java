package ru.dibin.serviceCommand;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ServiceCommand {

    static final byte send = 13;
    static final byte copy = 14;
    static final byte list = 15;
    static final byte deleteFile = 16;
    static final byte deleteWorkFolder = 17;
    static Logger LOGGER;
    private String command;
    private final String nickName;
    private final DataInputStream input;
    private final DataOutputStream out;
    private final Scanner sc = new Scanner ( System.in );

    static {
        try (FileInputStream ins = new FileInputStream ( "C:\\PutBox\\log.config" )) {
            LogManager.getLogManager ( ).readConfiguration ( ins );
            LOGGER = Logger.getLogger ( ServiceCommand.class.getName ( ) );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }

    public ServiceCommand( String nickName, DataInputStream input, DataOutputStream out) {
        this.nickName = nickName;
        this.input = input;
        this.out = out;
    }

    public ServiceCommand(String command, String nickName, DataInputStream input, DataOutputStream out) {
        this.command = command;
        this.nickName = nickName;
        this.input = input;
        this.out = out;
    }

    public boolean command() throws IOException {

        if (command.equals ( "help" )) {
            System.out.println ( "Для работы с приложением необходимо вводить служебные команды." +
                    "Для отправки файла или каталога нужно написать \"transfer_to\"." +
                    " Для просмотра списка каталогов и файлов введите \"transition\"." +
                    " Для копирование файла или каталога введите \"copy\"." +
                    " Для удаление файла или каталога введите \"delete\"." +
                    " Для выхода из \"PutBox\" введите \"quite\"\n" );
        }

        if (command.equals ( "transition" )) {
            out.writeByte ( list );
            int workFolderLength = nickName.length ( );
            out.writeInt ( workFolderLength );
            out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
            int fileLength = input.readInt ();
            byte[] nameFile = new byte[fileLength];
            for ( int i = 0 ; i < nameFile.length ; i++ ) {
                nameFile[i] = input.readByte ();
            }
            System.out.println ( new String ( nameFile ) );
        }

        if (command.equals ( "delete" )){
            System.out.println ( "Введите название файла, которого хотите удалить" );
            String nameFile = sc.nextLine ();
            out.writeByte ( deleteFile );
            int workFolderLength = nickName.length ( );
            out.writeInt ( workFolderLength );
            out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
            int fileNameLength = nameFile.length ( );
            out.writeInt ( fileNameLength );
            out.write ( nameFile.getBytes( StandardCharsets.UTF_8 ) );
            byte inputByte = input.readByte ( );
            if (inputByte == deleteFile) System.out.println ( "Файл успешно удален\n" );
            else System.out.println ( "Файл не найден\n" );
        }

        if (command.equals ( "quite" )) {
            LOGGER.log ( Level.INFO, nickName + " вышел из приложения" );
            sc.close ( );
            System.exit ( 0 );
        }

        if (command.equals ( "transfer_to" )) {
            System.out.println ( "Укажите путь к файлу, который хотите передать" );
            String str = sc.next ( );
            Path path = Paths.get ( str );
            out.writeByte ( send );
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
            String str = sc.next ( );
            out.write ( copy );
            int workFolderLength = nickName.length ( );
            out.writeInt ( workFolderLength );
            out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
            int fileNameLength = str.length ( );
            out.writeInt ( fileNameLength );
            out.write ( str.getBytes ( ) );
            byte inputByte = input.readByte ( );
            if (inputByte == copy) {
                System.out.println ( "Файл найден, идет загрузка..." );
                long fileSize = input.readLong ( );
                String namePath = "C:\\PutBox\\Тестовая_папка";
                Path path = Paths.get ( namePath, str );
                try (BufferedOutputStream bufferedOutputStream
                             = new BufferedOutputStream ( new FileOutputStream ( path.toFile ( ) ) )) {
                    for ( int i = 0 ; i < fileSize ; i++ ) {
                        bufferedOutputStream.write ( input.readByte ( ) );
                    }
                }
                System.out.println ( "Файл успешно загружен\n" );
            } else {
                System.out.println ( "Файл не найден, проверьте правильно ввели имя файла\n" );
            }
        } return true;
    }

    public void deleteWorkFolder() throws IOException {

        out.write ( deleteWorkFolder );
        int workFolderLength = nickName.length ( );
        out.writeInt ( workFolderLength );
        out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
    }
}
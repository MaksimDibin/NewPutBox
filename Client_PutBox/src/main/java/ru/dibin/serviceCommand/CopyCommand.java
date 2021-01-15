package ru.dibin.serviceCommand;

import ru.dibin.implementsClass.Command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static ru.dibin.serviceCommand.Signal.COPY;

public class CopyCommand implements Command {

    private final String nickName;
    private final DataInputStream input;
    private final DataOutputStream out;

    public CopyCommand(String nickName, DataInputStream input, DataOutputStream out) {
        this.nickName = nickName;
        this.input = input;
        this.out = out;
    }

    @Override
    public void start() throws IOException {
        System.out.println ( "Введите название файла с расширением! Пример test.txt" );
        Scanner sc = new Scanner ( System.in );
        String str = sc.next ( );
        out.write ( COPY.getI () );
        int workFolderLength = nickName.length ( );
        out.writeInt ( workFolderLength );
        out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
        int fileNameLength = str.length ( );
        out.writeInt ( fileNameLength );
        out.write ( str.getBytes ( ) );
        byte inputByte = input.readByte ( );
        if (inputByte == COPY.getI ()) {
            System.out.println ( "Файл найден, идет загрузка..." );
            long fileSize = input.readLong ( );
            String namePath = "C:" + File.separator + "PutBox" + File.separator + "Тестовая_папка"; //Заменить путь к файлу
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
    }
}
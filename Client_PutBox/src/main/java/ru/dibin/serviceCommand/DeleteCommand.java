package ru.dibin.serviceCommand;

import ru.dibin.implementsClass.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ru.dibin.serviceCommand.Signal.DELETE_FILE;

public class DeleteCommand implements Command {

    private final String nickName;
    private final DataInputStream input;
    private final DataOutputStream out;

    public DeleteCommand(String nickName, DataInputStream input, DataOutputStream out) {
        this.nickName = nickName;
        this.input = input;
        this.out = out;
    }

    @Override
    public void start() throws IOException {
        System.out.println ( "Введите название файла, которого хотите удалить" );
        Scanner sc = new Scanner ( System.in );
        String nameFile = sc.nextLine ( );
        out.writeByte ( DELETE_FILE.getI ( ) );
        int workFolderLength = nickName.length ( );
        out.writeInt ( workFolderLength );
        out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
        int fileNameLength = nameFile.length ( );
        out.writeInt ( fileNameLength );
        out.write ( nameFile.getBytes ( StandardCharsets.UTF_8 ) );
        byte inputByte = input.readByte ( );
        if (inputByte == DELETE_FILE.getI ( )) System.out.println ( "Файл успешно удален\n" );
        else System.out.println ( "Файл не найден\n" );
        sc.close ( );
    }
}

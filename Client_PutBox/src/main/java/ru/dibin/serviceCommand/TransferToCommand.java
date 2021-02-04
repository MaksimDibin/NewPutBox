package ru.dibin.serviceCommand;

import ru.dibin.implementsClass.Command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static ru.dibin.serviceCommand.Signal.SEND;

public class TransferToCommand implements Command {

    private final String nickName;
    private final DataOutputStream out;

    public TransferToCommand(String nickName, DataOutputStream out) {
        this.nickName = nickName;
        this.out = out;
    }

    @Override
    public void start() throws IOException {
        System.out.println ( "Укажите путь к файлу, который хотите передать" );
        Scanner sc = new Scanner ( System.in );
        String str = sc.next ( );
        Path path = Paths.get ( str );
        out.writeByte ( SEND.getI ( ) );
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
            while ((array = inputStream.read ( bytes )) != -1) {
                out.write ( bytes, 0, array );
            }
        }
    }
}
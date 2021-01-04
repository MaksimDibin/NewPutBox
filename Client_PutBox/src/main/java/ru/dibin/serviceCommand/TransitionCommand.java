package ru.dibin.serviceCommand;

import ru.dibin.implementsClass.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ru.dibin.serviceCommand.Signal.LIST;

public class TransitionCommand implements Command {

    private final String nickName;
    private final DataInputStream input;
    private final DataOutputStream out;

    public TransitionCommand(String nickName, DataInputStream input, DataOutputStream out) {
        this.nickName = nickName;
        this.input = input;
        this.out = out;
    }

    @Override
    public void start() throws IOException {
        out.writeByte ( LIST.getI ( ) );
        int workFolderLength = nickName.length ( );
        out.writeInt ( workFolderLength );
        out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
        int fileLength = input.readInt ( );
        byte[] nameFile = new byte[ fileLength ];
        for ( int i = 0 ; i < nameFile.length ; i++ ) {
            nameFile[ i ] = input.readByte ( );
        }
        System.out.println ( new String ( nameFile ) );
    }
}
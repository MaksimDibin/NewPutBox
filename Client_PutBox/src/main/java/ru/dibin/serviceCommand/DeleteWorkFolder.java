package ru.dibin.serviceCommand;

import ru.dibin.implementsClass.Command;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ru.dibin.serviceCommand.Signal.DELETE_WORK_FOLDER;

public class DeleteWorkFolder implements Command {

    private final String nickName;
    private final DataOutputStream out;

    public DeleteWorkFolder(String nickName, DataOutputStream out) {
        this.nickName = nickName;
        this.out = out;
    }

    @Override
    public void start() throws IOException {
        out.write ( DELETE_WORK_FOLDER.getI ( ) );
        int workFolderLength = nickName.length ( );
        out.writeInt ( workFolderLength );
        out.write ( nickName.getBytes ( StandardCharsets.UTF_8 ) );
    }
}

package ru.dibin.serviceCommand;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ServiceCommand {

    static Logger LOGGER;
    private final String command;
    private final String nickName;
    private final DataInputStream input;
    private final DataOutputStream out;

    static {
        try (FileInputStream ins = new FileInputStream ( "C:\\PutBox\\log.config" )) {
            LogManager.getLogManager ( ).readConfiguration ( ins );
            LOGGER = Logger.getLogger ( ServiceCommand.class.getName ( ) );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }

    public ServiceCommand(String command, String nickName, DataInputStream input, DataOutputStream out) {
        this.command = command;
        this.nickName = nickName;
        this.input = input;
        this.out = out;
    }

    public boolean command() throws IOException {

        switch (command) {
            case "help":
                new HelpCommand ( ).help ( );
                break;
            case "transition":
                new TransitionCommand ( nickName, input, out ).start ( );
                break;
            case "delete":
                new DeleteCommand ( nickName, input, out ).start ( );
                break;
            case "quite":
                LOGGER.log ( Level.INFO, nickName + " вышел из приложения" );
                return false;
            case "transfer_to":
                new TransferToCommand ( nickName, out ).start ( );
                break;
            case "copy":
                new CopyCommand ( nickName, input, out ).start ( );
                break;
            case "deleteAll":
                new DeleteWorkFolder ( nickName, out ).start ( );
                break;
        }
        return true;
    }
}
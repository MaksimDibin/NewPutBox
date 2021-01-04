package ru.dibin.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import org.apache.commons.io.FileUtils;
import ru.dibin.enumClass.State;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static ru.dibin.enumClass.Signal.*;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    private State currentState = State.IDLE;
    private int nextLength;
    private int lengthWorkFolder;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private byte read;
    private Path path;
    static Logger LOGGER;
    private File file;
    private byte[] fileName;
    private boolean result;

    static {
        try (FileInputStream ins = new FileInputStream ( "C:\\PutBox\\log.config" )) {
            LogManager.getLogManager ( ).readConfiguration ( ins );
            LOGGER = Logger.getLogger ( ProtoHandler.class.getName ( ) );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf buf = ((ByteBuf) msg);
        ByteBuf byteBuf;

        while (buf.readableBytes ( ) > 0) {
            if (currentState == State.IDLE) {
                read = buf.readByte ( );
                switch (read) {
                    case 13:
                        currentState = State.LENGTH_WORK_FOLDER;
                        receivedFileLength = 0L;
                        LOGGER.log ( Level.INFO, "Началась загрузка файла" );
                        break;
                    case 14:
                        currentState = State.LENGTH_WORK_FOLDER;
                        receivedFileLength = 0L;
                        LOGGER.log ( Level.INFO, "Началось скачивание файла" );
                        break;
                    case 15:
                        currentState = State.LENGTH_WORK_FOLDER;
                        LOGGER.log ( Level.INFO, "Происходит поиск файлов" );
                        break;
                    case 16:
                        currentState = State.LENGTH_WORK_FOLDER;
                        LOGGER.log ( Level.INFO, "Удаление файла" );
                        break;
                    case 17:
                        currentState = State.LENGTH_WORK_FOLDER;
                        LOGGER.log ( Level.INFO, "Удаление рабочей папки" );
                        break;
                    default:
                        LOGGER.log ( Level.WARNING, "Неизвестная команда: " + read );
                        break;
                }
                break;
            }
        }

        if (currentState == State.LENGTH_WORK_FOLDER) {
            if (buf.readableBytes ( ) >= 4) {
                LOGGER.log ( Level.INFO, "Получаем размер рабочей папки" );
                lengthWorkFolder = buf.readInt ( );
                currentState = State.NAME_WORK_FOLDER;
            }
        }

        if (currentState == State.NAME_WORK_FOLDER) {
            if (buf.readableBytes ( ) >= 4) {
                LOGGER.log ( Level.INFO, "Проверяется наличе рабочей папки" );
                byte[] fileName = new byte[ lengthWorkFolder ];
                buf.readBytes ( fileName );
                String nameFolder = new String ( fileName, StandardCharsets.UTF_8 );
                file = new File ( "C:\\PutBox\\WorkFolder\\" + nameFolder );
                if (file.mkdir ( )) LOGGER.log ( Level.INFO, "Создана рабочая папка " + nameFolder );
                if (read == SEND.getI ( ) || read == COPY.getI ( ) || read == DELETE_FILE.getI ( ))
                    currentState = State.NAME_LENGTH;
                if (read == LIST.getI ( )) currentState = State.LIST_OF_FILES;
                if (read == DELETE_WORK_FOLDER.getI ( )) currentState = State.DELETE_WORK_FOLDER;
            }
        }

        if (currentState == State.DELETE_WORK_FOLDER) {
            FileUtils.deleteDirectory ( new File ( String.valueOf ( file ) ) );
            currentState = State.IDLE;
        }

        if (currentState == State.LIST_OF_FILES) {
            if (file.isDirectory ( )) {
                File[] list = file.listFiles ( );
                if (list != null) {
                    for ( File name : list ) {
                        buf.writeLong ( name.getName ( ).length ( ) );
                        ctx.writeAndFlush ( buf );
                        ctx.writeAndFlush ( name.getName ( ) );
                    }
                }
                LOGGER.log ( Level.INFO, "Список файлов передался" );
                currentState = State.IDLE;
            }
        }

        if (currentState == State.NAME_LENGTH) {
            if (buf.readableBytes ( ) >= 4) {
                LOGGER.log ( Level.INFO, "Получаем размер названия файла" );
                nextLength = buf.readInt ( );
                currentState = State.NAME;
            }
        }

        if (currentState == State.NAME) {
            if (buf.readableBytes ( ) >= nextLength) {
                fileName = new byte[ nextLength ];
                buf.readBytes ( fileName );
                LOGGER.log ( Level.INFO, "Получено имя файла - " + new String ( fileName, StandardCharsets.UTF_8 ) );
                String namePath = String.valueOf ( file );
                path = Path.of ( namePath, new String ( fileName, StandardCharsets.UTF_8 ) );
                if (read == DELETE_FILE.getI ( )) {
                    currentState = State.DELETE_FILE;
                }
                if (read == SEND.getI ( )) {
                    try {
                        out = new BufferedOutputStream ( new FileOutputStream ( path.toFile ( ) ) );
                    } catch (FileNotFoundException e) {
                        e.printStackTrace ( );
                    }
                    currentState = State.FILE_LENGTH;
                }
                if (read == COPY.getI ( )) {
                    currentState = State.VERIFY_FAIL_PRESENCE;
                }
            }

            if (currentState == State.DELETE_FILE) {
                File[] list = file.listFiles ( );
                for ( File value : list )
                    if (value.getName ( ).equals ( new String ( fileName, StandardCharsets.UTF_8 ) ) && value.isFile ( )) {
                        if (value.delete ( ))
                            result = true;
                        break;
                    }
                if (result) {
                    byteBuf = ByteBufAllocator.DEFAULT.directBuffer ( 1 );
                    byteBuf.writeByte ( DELETE_FILE.getI ( ) );
                    ctx.writeAndFlush ( byteBuf );
                    LOGGER.log ( Level.INFO, "Файл удален" );
                    currentState = State.IDLE;
                } else {
                    byteBuf = ByteBufAllocator.DEFAULT.directBuffer ( 1 );
                    byteBuf.writeByte ( (byte) 21 );
                    ctx.writeAndFlush ( byteBuf );
                    LOGGER.log ( Level.INFO, "Файл не найден" );
                    currentState = State.IDLE;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes ( ) >= 8) {
                    fileLength = buf.readLong ( );
                    LOGGER.log ( Level.INFO, "Получаем размер файла" );
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes ( ) > 0) {
                    out.write ( buf.readByte ( ) );
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        LOGGER.log ( Level.INFO, "Файл записан" );
                        out.close ( );
                        break;
                    }
                }
            }

            if (currentState == State.VERIFY_FAIL_PRESENCE) {
                if (Files.exists ( path )) {
                    byteBuf = ByteBufAllocator.DEFAULT.directBuffer ( 1 );
                    byteBuf.writeByte ( COPY.getI ( ) );
                    ctx.writeAndFlush ( byteBuf );
                    LOGGER.log ( Level.INFO, "Файл найден" );
                    currentState = State.FILE_DISPATCH;
                } else {
                    byteBuf = ByteBufAllocator.DEFAULT.directBuffer ( 1 );
                    byteBuf.writeByte ( (byte) 19 );
                    ctx.writeAndFlush ( byteBuf );
                    LOGGER.log ( Level.INFO, "Файл не найден" );
                    currentState = State.IDLE;
                }
            }

            if (currentState == State.FILE_DISPATCH) {
                byteBuf = ByteBufAllocator.DEFAULT.directBuffer ( 8 );
                byteBuf.writeLong ( Files.size ( path ) );
                ctx.writeAndFlush ( byteBuf );
                FileRegion region = new DefaultFileRegion ( path.toFile ( ), 0, Files.size ( path ) );
                ctx.writeAndFlush ( region );
                currentState = State.IDLE;
                LOGGER.log ( Level.INFO, "Файл загружен" );
            }
        }

        if (buf.readableBytes ( ) == 0) {
            buf.release ( );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace ( );
        ctx.close ( );
    }
}
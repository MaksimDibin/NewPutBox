package ru.dibin.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    private State currentState = State.IDLE;
    private int nextLength;
    private int lengthWorkFolder;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private byte read;
    private Path path;
    private String nameFolder;
    static Logger LOGGER;

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
                    default:
                        LOGGER.log ( Level.WARNING, "Неизвестная команда: " + read );
                }
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
                nameFolder = new String ( fileName, StandardCharsets.UTF_8 );
                File file = new File ( "C:\\PutBox\\Server_PutBox\\src\\main\\java\\ru\\WorkFolder\\" + nameFolder );
                if (file.mkdir ( )) LOGGER.log ( Level.INFO, "Создана рабочая папка " + nameFolder );
                currentState = State.NAME_LENGTH;
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
                byte[] fileName = new byte[ nextLength ];
                buf.readBytes ( fileName );
                LOGGER.log ( Level.INFO, "Получено имя файла - " + new String ( fileName, StandardCharsets.UTF_8 ) );
                path = Path.of ( nameFolder, new String ( fileName ) );
                if (read == 13) {
                    try {
                        out = new BufferedOutputStream ( new FileOutputStream ( path.toFile ( ) ) );
                    } catch (FileNotFoundException e) {
                        e.printStackTrace ( );
                    }
                    currentState = State.FILE_LENGTH;
                }
                if (read == 14) {
                    currentState = State.VERIFY_FAIL_PRESENCE;
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
                    byteBuf.writeByte ( (byte) 13 );
                    ctx.writeAndFlush ( byteBuf );
                    LOGGER.log ( Level.INFO, "Проверка наличия файла" );
                    currentState = State.FILE_DISPATCH;
                } else {
                    byteBuf = ByteBufAllocator.DEFAULT.directBuffer ( 1 );
                    byteBuf.writeByte ( (byte) 14 );
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
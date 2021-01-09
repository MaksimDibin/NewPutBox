package ru.dibin.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Server {

    static Logger LOGGER;

    static {
        try (FileInputStream ins = new FileInputStream ( "C:\\PutBox\\log.config" )) {
            LogManager.getLogManager ( ).readConfiguration ( ins );
            LOGGER = Logger.getLogger ( Server.class.getName ( ) );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }

    public static void main(String[] args) {
        try {
            new Server ( ).start ( 8180 );
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }

    public void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup ( );
        EventLoopGroup workerGroup = new NioEventLoopGroup ( );
        try {
            ServerBootstrap sb = new ServerBootstrap ( );
            sb.group ( bossGroup, workerGroup )
                    .channel ( NioServerSocketChannel.class )
                    .childHandler ( new ChannelInitializer <SocketChannel> ( ) {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline ( );
                            pipeline.addLast ( new ProtoHandler ( ) );
                        }
                    } ).option ( ChannelOption.SO_BACKLOG, 50 ).childOption ( ChannelOption.SO_KEEPALIVE, true );
            ChannelFuture f = sb.bind ( port ).sync ( );

            LOGGER.log ( Level.INFO, "Сервер запущен" );

            f.channel ( ).closeFuture ( ).sync ( );
        } finally {
            workerGroup.shutdownGracefully ( );
            bossGroup.shutdownGracefully ( );
        }
    }
}

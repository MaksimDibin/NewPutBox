package ru.dibin.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws Exception {
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
                            pipeline.addLast (  ); //Скоро займусь сервером.
                        }
                    } ).option ( ChannelOption.SO_BACKLOG, 50 ).childOption ( ChannelOption.SO_KEEPALIVE, true );
            ChannelFuture f = sb.bind ( port ).sync ( );


            f.channel ( ).closeFuture ( ).sync ( );
        } finally {
            workerGroup.shutdownGracefully ( );
            bossGroup.shutdownGracefully ( );
        }
    }
}

package br.com.soeirosantos.grpc.server;

import com.google.common.util.concurrent.UncaughtExceptionHandlers;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcServer {

	public static void main(String[] args) throws IOException, InterruptedException {
//		System.setProperty("io.netty.allocator.useCacheForAllThreads", "false");
		nettyServer();
//		simpleServer();
	}

	private static void nettyServer() throws IOException, InterruptedException {
		final Server server = newNettyServer();
		server.start();
		System.out.println("Netty Config Server started");
		shutdown(server);
	}

	private static Server newNettyServer() {
		ThreadFactory tf = new DefaultThreadFactory("server-elg-", true);
		// On Linux it can, possibly, be improved by using
		// io.netty.channel.epoll.EpollEventLoopGroup
		// io.netty.channel.epoll.EpollServerSocketChannel
		final EventLoopGroup boss = new NioEventLoopGroup(1, tf);
		final EventLoopGroup worker = new NioEventLoopGroup(0, tf);
		final Class<? extends ServerChannel> channelType = NioServerSocketChannel.class;
		NettyServerBuilder builder = NettyServerBuilder
				.forPort(8080)
				.bossEventLoopGroup(boss)
				.workerEventLoopGroup(worker)
				.channelType(channelType)
				.addService(ServerInterceptors.intercept(new HelloServiceImpl(), new HeaderServerInterceptor()))
				.flowControlWindow(NettyChannelBuilder.DEFAULT_FLOW_CONTROL_WINDOW);
//		builder.directExecutor();
		builder.executor(getAsyncExecutor());
		return builder.build();
	}

	private static void simpleServer() throws IOException, InterruptedException {
		Server server = ServerBuilder
				.forPort(8080)
				.addService(ServerInterceptors.intercept(new HelloServiceImpl(), new HeaderServerInterceptor()))
				.build();
		server.start();
		System.out.println("Simple Config Server started");
		shutdown(server);
	}

	private static Executor getAsyncExecutor() {
		return new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
				new ForkJoinPool.ForkJoinWorkerThreadFactory() {
					final AtomicInteger num = new AtomicInteger();

					@Override
					public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
						ForkJoinWorkerThread thread =
								ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
						thread.setDaemon(true);
						thread.setName("grpc-server-app-" + "-" + num.getAndIncrement());
						return thread;
					}
				}, UncaughtExceptionHandlers.systemExit(), true);
	}


	private static void shutdown(Server server) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				System.out.println("Server shutting down");
				server.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		server.awaitTermination();
	}
}

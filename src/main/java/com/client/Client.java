package com.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import redis.clients.jedis.Jedis;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.log.ChatLog;
public class Client {

	public static void main(String[] args) throws InterruptedException {
		
		//服务类
		ClientBootstrap bootstrap = new  ClientBootstrap();
		
		//线程池
		ExecutorService boss = Executors.newCachedThreadPool();
		ExecutorService worker = Executors.newCachedThreadPool();
		
		//socket工厂
		bootstrap.setFactory(new NioClientSocketChannelFactory(boss, worker));
		
		//管道工厂
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new StringDecoder());
				pipeline.addLast("encoder", new StringEncoder());
				pipeline.addLast("hiHandler", new HiHandler());
				return pipeline;
			}
		});
		
		//连接服务端
		ChannelFuture connect = bootstrap.connect(new InetSocketAddress("127.0.0.1", 10101));
		Channel channel = connect.getChannel();
		//记录client聊天记录，设置聊天记录保存的最多条数
		Jedis jedis = new Jedis("localhost");
		ChatLog chatlog=new ChatLog(100);


		System.out.println("client start");


		Scanner scanner = new Scanner(System.in);
		while(true){
			Thread.sleep(200);
			System.out.println("请输入");
			String chat=scanner.next();
			if(chat.equals("ClientLog")){
				System.out.println(chatlog.queryData(jedis,"client"));
				continue;
			}
			else if(chat.equals("SeverLog")){
				System.out.println(chatlog.queryData(jedis,"server"));
				continue;
			}
			channel.write(chat);
			chatlog.saveData(jedis,"client",chat);
		}
	}
}

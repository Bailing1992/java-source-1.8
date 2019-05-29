package com.lichao.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * describe: 管道流主要可以进行两个线程之间的通信。多线程中通过管道通信的例子；
 *
 * @author lichao
 * @date 2019/01/01
 */
public class PipedStreamDemo {
    public static void main(String[] args) throws IOException {
        Send send = new Send();
        Recive recive = new Recive();
        try{
            //管道连接
            send.getOut().connect(recive.getInput());
        }catch (Exception e) {
            e.printStackTrace();
        }
        /**
         26              * Thread类的START方法：
         27              * 使该线程开始执行；Java 虚拟机调用该线程的 run 方法。
         28              * 结果是两个线程并发地运行；当前线程（从调用返回给 start 方法）和另一个线程（执行其 run 方法）。
         29              * 多次启动一个线程是非法的。特别是当线程已经结束执行后，不能再重新启动。
         30              */
        new Thread(send).start();
        new Thread(recive).start();
    }

}

/**
 * 消息发送类
 * */
class Send implements Runnable{
    // PipedOutputStream 管道输出流

    private PipedOutputStream out = null;
    public Send() {
        out = new PipedOutputStream();
    }
    public PipedOutputStream getOut(){
        return this.out;
    }
    public void run(){
        String message = "hello , Rollen";
        try{
            out.write(message.getBytes());
        }catch (Exception e) {
            e.printStackTrace();
        }
        try{
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * 接受消息类
 * */
class Recive implements Runnable{
    // PipedInputStream 管道输入流
    // 管道输入流对象。
    // 它和“管道输出流(PipedOutputStream)”对象绑定，
    // 从而可以接收“管道输出流”的数据，再让用户读取。
    private PipedInputStream input = null;
    public Recive(){
        this.input = new PipedInputStream();
    }
    public PipedInputStream getInput(){
        return this.input;
    }
    public void run(){
        // 虽然buf的大小是2048个字节，但最多只会从“管道输入流”中读取1024个字节。
        // 因为，“管道输入流”的缓冲区大小默认只有1024个字节。
        byte[] b = new byte[1000];
        int len = 0;
        try{
            len = this.input.read(b);
        }catch (Exception e) {
            e.printStackTrace();
        }
        try{
            input.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("接受的内容为 " + (new String(b,0, len)));
    }
}
package com.example;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
    public static void main(String... args) {
        new Thread(new UdpServer()).start();
    }


    public static class UdpServer implements Runnable {

        public static final String Ip = "192.168.1.181";
        public static final int Port = 9876;
        static long fileIndxe = 0l;
        private static DatagramSocket socket;
        private static InetAddress addr;
        byte[] data = new byte[10240];

        public static void writeToFile(byte[] data) {
            DataOutputStream dataOutputStream = null;
            try {
                dataOutputStream = new DataOutputStream(new FileOutputStream("2016-3-17", true));
//                FileOutputStream fileOutputStream = new FileOutputStream("");
//                fileOutputStream.write(data);
                dataOutputStream.write(data);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public void receiveData() {
            try {

                if (socket == null) {
                    socket = new DatagramSocket(Port);
//                    socket.setBroadcast(true);
//                    addr = InetAddress.getByName(Ip);
                }
//                byte[] buffer = "Hello World".getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length);
//                packet.setAddress(addr);
//                packet.setPort(Port);

                System.out.println("receive...  ");
                socket.receive(packet);
//                socket.
                writeToFile(data);
//                socket.send(packet);
                System.out.println("receive length " + data.length + "    --packet length  " + packet.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    receiveData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

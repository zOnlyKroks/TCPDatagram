package de.zonlykroks.tcpdatagram;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Tunnel {

    private final int port1, port2;

    private final boolean isServer;


    private Socket remote;

    private Forwarder in, out;

    private Socket local = new Socket();

    public Tunnel(boolean isServer, int port) {
        this.isServer = isServer;

        this.port1 = this.isServer ? port : port + 1;
        this.port2 = this.isServer ? port + 1 : port;
    }

    public void startWithDatagram() throws Exception {
        if(isServer) {
            DatagramSocket receiveDataGramSocket = new DatagramSocket(port1);
            byte[] msg = new byte["Init ACK!".getBytes(StandardCharsets.UTF_8).length];
            DatagramPacket receiveDataPacket = new DatagramPacket(msg, msg.length);
            receiveDataGramSocket.receive(receiveDataPacket);

            System.out.println(new String(msg));

            System.out.println("-----");

            DatagramSocket socket = new DatagramSocket();
            InetAddress host = InetAddress.getByName("127.0.0.1");
            DatagramPacket pk = new DatagramPacket("Server request open Socket!".getBytes(StandardCharsets.UTF_8),
                    "Server request open Socket!".getBytes(StandardCharsets.UTF_8).length,
                    host,
                    port2);
            socket.send(pk); //send packet to server to initiate udp communication
            System.out.println("Server request open Socket!");

            ServerSocket serverSocket = new ServerSocket(port1);
            this.remote = serverSocket.accept();
        }else {
            DatagramSocket socket = new DatagramSocket();
            InetAddress host = InetAddress.getByName("127.0.0.1");
            DatagramPacket pk = new DatagramPacket("Init ACK!".getBytes(StandardCharsets.UTF_8),
                    "Init ACK!".getBytes(StandardCharsets.UTF_8).length,
                    host,
                    port2);
            socket.send(pk); //send packet to server to initiate udp communication
            System.out.println("Init ACK!");

            DatagramSocket receiveDataGramSocket = new DatagramSocket(port1);
            byte[] msg = new byte["Server request open Socket!".getBytes(StandardCharsets.UTF_8).length];
            DatagramPacket receiveDataPacket = new DatagramPacket(msg, msg.length);
            receiveDataGramSocket.receive(receiveDataPacket);

            System.out.println(new String(msg));

            this.remote = new Socket("127.0.0.1", port2);
        }
    }

    public void setupLocalTunnel() throws Exception {
        if(isServer) {
            local.connect(new InetSocketAddress(25565), (int) TimeUnit.SECONDS.toMillis(20));
        }else {
            try {
                ServerSocket localServer = new ServerSocket();
                for (int i = 0; i < 20; i++) {
                    try {
                        localServer.bind(new InetSocketAddress(25565));
                    } catch (IOException ignored) {}
                    Thread.sleep(1000);
                }
                if (!localServer.isBound()) {
                    System.out.println("failed to find port for local proxy");
                    return;
                }

                this.local = localServer.accept();
                localServer.close();
            } catch (IOException e) {
                System.out.println("failed to setup local tunnel: " + e.getMessage());
            }
        }

        try {
            this.in = new Forwarder(this.local.getOutputStream(), this.remote.getInputStream());
            this.out = new Forwarder(this.remote.getOutputStream(), this.local.getInputStream());

            in.start();
            out.start();
        } catch (IOException e) {
            System.out.println("stream forwarding failed to start: " + e.getMessage());
        }
    }

}

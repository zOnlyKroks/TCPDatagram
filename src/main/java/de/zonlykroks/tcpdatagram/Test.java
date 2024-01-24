package de.zonlykroks.tcpdatagram;

public class Test {

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Tunnel tunnel = new Tunnel(true, 30000);
                tunnel.startWithDatagram();
                tunnel.setupLocalTunnel();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(4000);

                Tunnel tunnel = new Tunnel(false, 30000);
                tunnel.startWithDatagram();
                tunnel.setupLocalTunnel();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

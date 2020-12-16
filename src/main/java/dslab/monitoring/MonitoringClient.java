package dslab.monitoring;

import java.io.IOException;
import java.net.*;

public class MonitoringClient implements IMonitoringClient {

    private InetAddress host;
    private int port;

    public MonitoringClient(String host, int port) throws UnknownHostException {
        this.host = InetAddress.getByName(host);
        this.port = port;
    }

    @Override
    public void logMessage(UDPMessage udpMessage) {
        String serializedMessage = udpMessage.toString();
        // get a datagram socket
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // send request
        byte[] buf = serializedMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, host, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}

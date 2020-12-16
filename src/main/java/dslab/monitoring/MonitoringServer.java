package dslab.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import dslab.ComponentFactory;
import dslab.util.Config;

public class MonitoringServer implements IMonitoringServer, Runnable {

    protected DatagramSocket socket = null;
    private IMonitoringPersistence persistence = new MonitoringPersistence();

    private Shell shell;
    private Thread shellThread;

    private InputStream in;
    private PrintStream out;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MonitoringServer(String componentId, Config config, InputStream in, PrintStream out) throws SocketException {

        int port = config.getInt("udp.port");
        socket = new DatagramSocket(port);

        this.in = in;
        this.out = out;

        shell = new Shell(in, out)
                .register("addresses", ((input, context) -> this.addresses()))
                .register("servers", ((input, context) -> this.servers()))
                .register("shutdown", (((input, context) -> {
                    this.shutdown();
                    throw new StopShellException();
                })));
//        shell.setPrompt("monitoring-server" + "> ");
        shellThread = new Thread(shell);

        // test data
//        persistence.storeMessage(new UDPMessage("localhost", "1234", "rasha.djurdjevic@gmail.com"));
//        persistence.storeMessage(new UDPMessage("localhost", "1234", "rasha.djurdjevic@gmail.com"));
//        persistence.storeMessage(new UDPMessage("localhost", "1235", "rasha.djurdjevic@gmail.com"));
//        persistence.storeMessage(new UDPMessage("localhost", "1235", "r.djurdjevic@stellar.com"));
    }

    @Override
    public void run() {
        shellThread.start();
        byte[] buf = new byte[256];

        new Thread(() -> {
            while (true) {
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    persistence.storeMessage(new UDPMessage(received));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    @Override
    public void addresses() {
        this.out.println(persistence.generateAddressReport());
    }

    @Override
    public void servers() {
        this.out.println(persistence.generateServerReport());
    }

    @Override
    public void shutdown() {
        socket.close();
        shellThread.interrupt(); // this seems unnecessary (why?)
    }

    public static void main(String[] args) throws Exception {
        IMonitoringServer server = ComponentFactory.createMonitoringServer(args[0], System.in, System.out);
        server.run();
    }
}

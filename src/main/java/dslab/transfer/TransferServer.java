package dslab.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import dslab.ComponentFactory;
import dslab.dmtp.DMTPClientHandler;
import dslab.dmtp.EmailHandler;
import dslab.dmtp.storage.Storage;
import dslab.dmtp.storage.TransientStorage;
import dslab.monitoring.IMonitoringClient;
import dslab.monitoring.MonitoringClient;
import dslab.transfer.dns.DNSLookupService;
import dslab.transfer.dns.Domain;
import dslab.util.Config;

public class TransferServer implements ITransferServer, Runnable {

    private ServerSocket serverSocket;
    private EmailHandler emailHandler;
    private Storage storage;
    private DNSLookupService dnsLookupService;
    private IMonitoringClient monitoringClient;
    private ForwardingServiceHandler forwardingServiceHandler;
    private Shell shell;

    private ExecutorService executorService;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {
        int portNumber = config.getInt("tcp.port");
        String udpHost = config.getString("monitoring.host");
        int udpPort = config.getInt("monitoring.port");

        executorService = Executors.newCachedThreadPool();

        storage = new TransientStorage();
        emailHandler = new TransferServerEmailHandler(storage);
        dnsLookupService = new DNSLookupService();

        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            Domain transferServer = new Domain(localIp, portNumber);
            monitoringClient = new MonitoringClient(udpHost, udpPort);
            serverSocket = new ServerSocket(portNumber);
            forwardingServiceHandler = new ForwardingServiceHandler(storage, dnsLookupService, monitoringClient, transferServer);
        } catch (UnknownHostException e) {
            out.println(e.getMessage());
        } catch (IOException e) {
            out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            out.println(e.getMessage());
        }

        shell = new Shell(in, out)
                .register("shutdown", (((input, context) -> {
                    System.out.println("shutting down...");
                    this.shutdown();
                    throw new StopShellException();
                })));

    }

    @Override
    public void run() {
        executorService.execute(forwardingServiceHandler);
        executorService.execute(shell);
        while (true) {
            try {
                Socket client = serverSocket.accept();
                executorService.execute(new DMTPClientHandler(client, emailHandler));
            } catch (IOException e) {
                break;
            }
        }
    }

    @Override
    public void shutdown() {
        try {
            serverSocket.close();
            executorService.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }
}

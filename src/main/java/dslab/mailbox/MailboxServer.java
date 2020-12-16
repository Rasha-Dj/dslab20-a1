package dslab.mailbox;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import dslab.ComponentFactory;
import dslab.dmtp.DMTPClientHandler;
import dslab.dmtp.EmailHandler;
import dslab.dmtp.storage.PersistentStorage;
import dslab.dmtp.storage.Storage;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {

    private Map<String, String> users;

    private ServerSocket socketDMTP;
    private ServerSocket socketDMAP;

    private Shell shell;
    private Thread threadDMTP;
    private Thread threadDMAP;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {
        int portNumber = config.getInt("dmap.tcp.port");
        int portNumberDMTP = config.getInt("dmtp.tcp.port");

        String userFile = config.getString("users.config");
        System.out.println(userFile);
        try (InputStream inputStream = new FileInputStream("src/main/resources/" + userFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            users = (Map)properties;
            System.out.println(users);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // test data
//        EmailDTO emailDTO = new EmailDTO();
//        emailDTO.setId(UUID.randomUUID());
//        emailDTO.setFrom("rasha@gmail.com");
//        emailDTO.setTo(Arrays.asList("masha@gmail.com"));
//        emailDTO.setData("test");
//        emailDTO.setSubject("test");
//
//        EmailDTO emailDTO1 = new EmailDTO();
//        emailDTO1.setId(UUID.randomUUID());
//        emailDTO1.setFrom("rasha@gmail.com");
//        emailDTO1.setTo(Arrays.asList("gasha@gmail.com"));
//        emailDTO1.setData("test2");
//        emailDTO1.setSubject("test2");

        Storage storage = new PersistentStorage();
        EmailHandler emailHandler = new MailboxEmailHandler(storage);
        // initialize database for each user
        storage.init(users);
//        users.forEach((k, v) -> database.put(k, Arrays.asList(emailDTO, emailDTO1)));

        try {
            socketDMAP = new ServerSocket(portNumber);
            socketDMTP = new ServerSocket(portNumberDMTP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        threadDMAP = new Thread(() -> {
            while (true) {
                try {
                    executorService.execute(new DMAPClientHandler(socketDMAP.accept(), users, storage));
                } catch (IOException e) {
                    break;
                }
            }
        });
        threadDMTP = new Thread(() -> {
            while (true) {
                try {
                    executorService.execute(new DMTPClientHandler(socketDMTP.accept(), emailHandler));
                } catch (IOException e) {
                    break;
                }
            }
        });

        shell = new Shell(in, out)
                .register("shutdown", (((input, context) -> {
                    System.out.println("shutting down...");
                    this.shutdown();
                    throw new StopShellException();
                })));
    }

    @Override
    public void run() {
        executorService.execute(threadDMAP);
        executorService.execute(threadDMTP);
        executorService.execute(shell);
    }

    @Override
    public void shutdown() {
        try {
            socketDMTP.close();
            socketDMAP.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}

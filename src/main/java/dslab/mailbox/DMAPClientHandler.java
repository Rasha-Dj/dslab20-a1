package dslab.mailbox;

import dslab.dmtp.storage.Storage;
import dslab.util.email.EmailDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class DMAPClientHandler implements Runnable {

    private Socket socket;
    private Map<String, String> users;
    private Storage storage;

    public DMAPClientHandler(Socket socket, Map<String, String> users, Storage storage) {
        this.socket = socket;
        this.users = users;
        this.storage = storage;
    }

    @Override
    public void run() {
        try (
                PrintWriter outWriter =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader inWriter = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            System.out.println("ok DMAP");
            outWriter.println("ok DMAP");
            String inputLine;

            // 1. unauthorized: login, quit
            // 2. authorized: list, show, delete, logout, quit
            boolean authorized = false;
            String currentUser = null;
            while ((inputLine = inWriter.readLine()) != null) {
                System.out.println("Input: " + inputLine);
                if (inputLine.equals("quit")) {
                    // show's over
                    outWriter.println("ok bye");
                    socket.close();
                    break;
                }
                if (!authorized) {
                    // or if it doesn't start with "login"? :)
                    // should we check for syntax first? e.g exclude "lrst" and other stuff?
                    if (!inputLine.startsWith("login")) {
                        outWriter.println("error not logged in");
                    } else {
                        // user is attempting to log in

                        String[] parts = inputLine.split(" ");

                        if (parts.length != 3) {
                            outWriter.println("invalid login method");
                        } else {
                            // TODO expand on this
                            //  save logged in user
                            String command = parts[0]; // login
                            String user = parts[1];
                            String password = parts[2];

                            if (!users.containsKey(user)) {
                                outWriter.println("error unknown user");
                                continue;
                            }
                            if (!users.get(user).equals(password)) {
                                outWriter.println("error wrong password");
                                continue;
                            }
                            // user/password valid
                            authorized = true;
                            currentUser = user;
                            outWriter.println("ok");
                        }
                    }
                } else {
                    // user is authorized
                    if (inputLine.startsWith("list")) {
                        List<EmailDTO> emails = storage.getUserEmails(currentUser);
                        if (emails.isEmpty()) {
                            outWriter.println("(empty inbox)");
                        } else {
                            emails.forEach(x -> outWriter.println(x.getId() + " " + x.getFrom() + " " + x.getSubject()));
                        }
                    } else if (inputLine.startsWith("show")) {
                        // TODO handle invalid input
                        // FIXME printout should be better (use outWriter instead of email.toString)
                        String id = inputLine.split(" ")[1];
                        System.out.println(id);
                        List<EmailDTO> emails = storage.getUserEmails(currentUser);
                        System.out.println(emails);
                        Optional<EmailDTO> emailDTO = emails.stream().filter(email -> email.getId().equals(UUID.fromString(id))).findAny();
                        if (emailDTO.isEmpty()) {
                            outWriter.println("error unknown message id");
                        } else {
                            EmailDTO print = emailDTO.get();
                            outWriter.println("from " + print.getFrom());
                            outWriter.println("to " + print.getTo().stream().map(Object::toString).collect(Collectors.joining(",")));
                            outWriter.println("subject " + print.getSubject());
                            outWriter.println("data " + print.getData());
                        }
                    } else if (inputLine.startsWith("delete")) {
                        // TODO handle invalid input
                        // FIXME it didn't delete the right email
                        String id = inputLine.split(" ")[1];
                        List<EmailDTO> emails = storage.getUserEmails(currentUser);
                        Optional<EmailDTO> emailDTO = emails.stream().filter(email -> email.getId().equals(UUID.fromString(id))).findAny();
                        if (emailDTO.isEmpty()) {
                            outWriter.println("error unknown message id");
                        } else {
                            storage.remove(currentUser, emailDTO.get());
                            outWriter.println("ok");
                        }
                    } else if (inputLine.startsWith("logout")) {
                        outWriter.println("ok");
                        currentUser = null;
                        authorized = false;
                        continue;
                    } else {
                        // unknown command
                        outWriter.println("error unknown command");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

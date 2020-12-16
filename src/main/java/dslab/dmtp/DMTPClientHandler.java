package dslab.dmtp;

import dslab.util.email.EmailDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DMTPClientHandler implements Runnable {

    private Socket socket;
    private EmailHandler emailHandler;

    public DMTPClientHandler(Socket socket, EmailHandler emailHandler) {
        this.socket = socket;
        this.emailHandler = emailHandler;
    }

    @Override
    public void run() {
        try (
                PrintWriter outWriter =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader inWriter = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            outWriter.println("ok DMTP");
            String inputLine;
            EmailDTO emailDTO = new EmailDTO();
            emailDTO.setTo(new ArrayList<>());
            boolean begin = false;
            while ((inputLine = inWriter.readLine()) != null) {
                System.out.println("Input: " + inputLine);
                if (inputLine.equals("quit")) {
                    // show's over
                    outWriter.println("ok bye");
                    socket.close();
                    break;
                }

                if (!begin) {
                    if (!inputLine.startsWith("begin")) {
                        // you must start with "begin", otherwise connection termination
                        outWriter.println("error protocol error");
                        break;
                    } else if (inputLine.equals("begin")) {
                        begin = true;
                        outWriter.println("ok");
                    } else {
                        outWriter.println("unknown command");
                    }
                } else {
                    // begin == true
                    // to | from | subject | data
                    // send (only when valid), sends message and switches begin back to false
                    if (inputLine.startsWith("from")) {
                        emailDTO.setFrom(inputLine.split(" ")[1]);
                        outWriter.println("ok");
                    } else if (inputLine.startsWith("to")) {
                        boolean containsUnknownRecipient = false;
                        String unknownRecipient = "";
                        String toRaw = inputLine.split(" ")[1];
                        List<String> addresses = Arrays.asList(toRaw.split(","));
                        emailDTO.setTo(addresses);
                        for (String address : addresses) {
                            if (!emailHandler.isRecipientValid(address)) {
                                containsUnknownRecipient = true;
                                unknownRecipient = address.substring(0, address.indexOf('@'));
                            }
                        }
                        if (containsUnknownRecipient) {
                            emailDTO.setTo(new ArrayList<>()); // refresh the recipient list
                            outWriter.println("error unknown recipient " + unknownRecipient);
                        } else {
                            // all good
                            outWriter.println("ok " + addresses.size());
                        }
                    } else if (inputLine.startsWith("subject")) {
                        emailDTO.setSubject(inputLine.substring("subject".length() + 1));
                        outWriter.println("ok");
                    } else if (inputLine.startsWith("data")) {
                        emailDTO.setData(inputLine.substring("data".length() + 1));
                        outWriter.println("ok");
                    } else if (inputLine.startsWith("send")) {
                        // TODO extend validation
                        if (emailDTO.getTo().isEmpty()) {
                            outWriter.println("error no recipients");
                            continue;
                        }
                        // add newly made email to storage
                        System.out.println("Add message to storage: " + emailDTO);
                        try {
                            emailHandler.processEmail(emailDTO);
                        } catch (EmailProcessingException e) {
                            // in case user is not known to Mailbox Server
                            outWriter.println(e.getMessage());
                            continue;
                        }
                        begin = false;
                        outWriter.println("ok");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

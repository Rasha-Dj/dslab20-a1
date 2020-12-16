package dslab.dmtp;

import dslab.util.email.EmailDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class DMTPClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void sendEmail(EmailDTO emailDTO) throws IOException, UnknownUserException {
        System.out.println("Sending email: " + emailDTO);
        this.sendMessage("begin");
        this.sendMessage("from " + emailDTO.getFrom());
        String toResponse = this.sendMessage("to " + String.join(",", emailDTO.getTo()));
        System.out.println("to response: " + toResponse);
        if (toResponse.startsWith("error")) throw new UnknownUserException(toResponse);
        this.sendMessage("subject " + emailDTO.getSubject());
        this.sendMessage("data " + emailDTO.getData());
        String response = this.sendMessage("send");
        System.out.println(response);
        this.sendMessage("quit");
    }

    public void stopConnection() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (clientSocket != null) clientSocket.close();
    }

    public static void main(String[] args) {
        DMTPClient dmtpClient = new DMTPClient();
        dmtpClient.startConnection("127.0.0.1", 12042);
        try {
            EmailDTO emailDTO = new EmailDTO();
            emailDTO.setTo(Arrays.asList("arthur@earth.planet", "trilian@earth.planet"));
            emailDTO.setFrom("tester@gmail.com");
            emailDTO.setSubject("Hello");
            emailDTO.setData("World!");
            dmtpClient.sendEmail(emailDTO);
            dmtpClient.stopConnection();
        } catch (IOException | UnknownUserException e) {
            System.out.println(e.getMessage());
        }
    }
}

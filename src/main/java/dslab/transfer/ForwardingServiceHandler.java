package dslab.transfer;

import dslab.dmtp.DMTPClient;
import dslab.dmtp.UnknownUserException;
import dslab.dmtp.storage.Storage;
import dslab.monitoring.IMonitoringClient;
import dslab.monitoring.UDPMessage;
import dslab.transfer.dns.Domain;
import dslab.util.email.EmailDTO;
import dslab.transfer.dns.DNSLookupService;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForwardingServiceHandler implements Runnable {

    private Storage storage;
    private DNSLookupService dnsLookupService;
    private IMonitoringClient monitoringClient;
    private Domain transferServer;

    public ForwardingServiceHandler(Storage storage, DNSLookupService dnsLookupService, IMonitoringClient monitoringClient, Domain transferServer) {
        this.storage = storage;
        this.dnsLookupService = dnsLookupService;
        this.monitoringClient = monitoringClient;
        this.transferServer = transferServer;
    }

    @Override
    public void run() {
        System.out.println("ForwardingServiceHandler has been started!");
        while (!Thread.currentThread().isInterrupted()) {
            if (!storage.isEmpty()) {
                EmailDTO emailDTO = storage.pop();

                // send email to destination mailbox server
                System.out.println("Send email: " + emailDTO.getId() + "\n content: \n" + emailDTO);

                List<String> usedDomains = new ArrayList<>();
                emailDTO.getTo().forEach(recipient -> {
                    String domain = recipient.substring(recipient.indexOf('@') + 1);
                    if (!usedDomains.contains(domain)) {
                        usedDomains.add(domain); // this way we avoid sending same email multiple times to users from same domain
                        DMTPClient dmtpClient = new DMTPClient();
                        try {
                            Domain targetDomain = dnsLookupService.getDestinationDomain(domain);
                            dmtpClient.startConnection(targetDomain.getHost(), targetDomain.getPort());
                            dmtpClient.sendEmail(emailDTO);
                        } catch (UnknownUserException | UnknownHostException e) {
                            System.out.println(e.getMessage());
                            DMTPClient errorReporter = new DMTPClient();
                            String fromDomain = emailDTO.getFrom().substring(emailDTO.getFrom().indexOf('@') + 1);
                            try {
                                Domain errorReportTargetDomain = dnsLookupService.getDestinationDomain(fromDomain);
                                errorReporter.startConnection(errorReportTargetDomain.getHost(), errorReportTargetDomain.getPort());
                                EmailDTO errorReport = new EmailDTO();
                                errorReport.setFrom("mailer@" + transferServer.getHost());
                                errorReport.setTo(Arrays.asList(emailDTO.getFrom()));
                                errorReport.setSubject("Message Delivery Failure");
                                errorReport.setData("Your message with subject '" + emailDTO.getSubject() + "' was not delivered.");
                                errorReporter.sendEmail(errorReport);
                            } catch (IOException | UnknownUserException ioException) {
                                // give up
                                ioException.printStackTrace();
                            }
                            try {
                                errorReporter.stopConnection();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                dmtpClient.stopConnection();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        // log to monitoring
                        String transferServerPort = Integer.toString(transferServer.getPort());
                        UDPMessage message = new UDPMessage(transferServer.getHost(), transferServerPort, emailDTO.getFrom());
                        monitoringClient.logMessage(message);
                    }
                });
            }
        }
    }
}

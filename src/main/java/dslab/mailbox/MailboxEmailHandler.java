package dslab.mailbox;

import dslab.dmtp.EmailHandler;
import dslab.dmtp.EmailProcessingException;
import dslab.dmtp.storage.Storage;
import dslab.util.email.EmailDTO;

import java.util.List;
import java.util.UUID;

public class MailboxEmailHandler implements EmailHandler {

    private Storage storage;

    public MailboxEmailHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void processEmail(EmailDTO emailDTO) throws EmailProcessingException {
        // for each email
        // check to

        // otherwise save it with user as key, generate ID, check if user already has his list? nah
        List<String> recipients = emailDTO.getTo();

//        recipients.forEach(recipient -> {
//            if (recipient.startsWith("evil")) {
//                throw new EmailProcessingException("error unknown user");
//            }
//        });
        System.out.println(emailDTO.getTo());
        recipients.forEach(user -> {
            // if domain !correct then ignore (need to know about the domain)
            EmailDTO emailCopy = new EmailDTO();
            // generate UUID
            emailCopy.setData(emailDTO.getData());
            emailCopy.setSubject(emailDTO.getSubject());
            emailCopy.setTo(emailDTO.getTo());
            emailCopy.setFrom(emailDTO.getFrom());
            emailCopy.setId(UUID.randomUUID());
            storage.save(user, emailCopy);
        });
    }

    @Override
    public boolean isRecipientValid(String recipient) {
        return storage.containsUser(recipient);
    }
}

package dslab.dmtp;

import dslab.util.email.EmailDTO;

public interface EmailHandler {
    void processEmail(EmailDTO emailDTO) throws EmailProcessingException;
    boolean isRecipientValid(String recipient);
}

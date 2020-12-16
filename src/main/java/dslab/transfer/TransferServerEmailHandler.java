package dslab.transfer;

import dslab.dmtp.EmailHandler;
import dslab.dmtp.storage.Storage;
import dslab.util.email.EmailDTO;

public class TransferServerEmailHandler implements EmailHandler {

    private Storage storage;

    public TransferServerEmailHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void processEmail(EmailDTO emailDTO) {
        storage.save("draft", emailDTO);
    }

    @Override
    public boolean isRecipientValid(String recipient) {
        return true;
    }
}

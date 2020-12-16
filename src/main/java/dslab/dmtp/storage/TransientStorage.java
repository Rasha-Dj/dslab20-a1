package dslab.dmtp.storage;

import dslab.util.email.EmailDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

public class TransientStorage implements Storage {
    private LinkedBlockingDeque<EmailDTO> deque = new LinkedBlockingDeque<>();

    @Override
    public void init(Map<String, String> users) {

    }

    @Override
    public void save(String key, EmailDTO emailDTO) {
        deque.add(emailDTO);
    }

    @Override
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    @Override
    public EmailDTO pop() {
        return deque.pop();
    }

    @Override
    public void remove(EmailDTO emailDTO) {
        deque.remove(emailDTO);
    }

    @Override
    public void remove(String user, EmailDTO emailDTO) {

    }

    @Override
    public List<EmailDTO> getUserEmails(String user) {
        return null;
    }

    @Override
    public boolean containsUser(String user) {
        return true;
    }
}

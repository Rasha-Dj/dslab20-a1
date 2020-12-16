package dslab.dmtp.storage;

import dslab.util.email.EmailDTO;

import java.util.List;
import java.util.Map;

public interface Storage {
    void init(Map<String, String> users);
    void save(String key, EmailDTO emailDTO);
    boolean isEmpty();
    EmailDTO pop();
    void remove(EmailDTO emailDTO);
    void remove(String user, EmailDTO emailDTO);
    List<EmailDTO> getUserEmails(String user);
    boolean containsUser(String user);
}

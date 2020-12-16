package dslab.dmtp.storage;

import dslab.util.email.EmailDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PersistentStorage implements Storage {

    private ConcurrentHashMap<String, List<EmailDTO>> database = new ConcurrentHashMap<>();

    @Override
    public void init(Map<String, String> users) {
        users.forEach((k, v) -> database.put(k, new ArrayList<>()));
    }

    @Override
    public void save(String key, EmailDTO emailDTO) {
        String recipient = key.substring(0, key.indexOf("@"));
        if (database.containsKey(recipient)) {
            synchronized (this) {
                database.get(recipient).add(emailDTO);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public EmailDTO pop() {
        return null;
    }

    @Override
    public void remove(EmailDTO emailDTO) {

    }

    @Override
    public void remove(String user, EmailDTO emailDTO) {
        database.put(user, database.get(user).stream().filter(x -> !x.getId().equals(emailDTO.getId())).collect(Collectors.toList()));
    }

    @Override
    public List<EmailDTO> getUserEmails(String emailAddress) {
        return database.get(emailAddress);
    }

    @Override
    public boolean containsUser(String user) {
        return database.containsKey(user.substring(0, user.indexOf("@")));
    }
}

package dslab.util.email;

import java.util.List;
import java.util.UUID;

public class EmailDTO {
    private UUID id;
    private List<String> to;
    private String from;
    private String subject;
    private String data;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "EmailDTO{" +
                "id=" + id +
                ", to=" + to +
                ", from='" + from + '\'' +
                ", subject='" + subject + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}

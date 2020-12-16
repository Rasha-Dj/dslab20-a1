package dslab.monitoring;

public class UDPMessage {
    private String host;
    private String port;
    private String email;

    public UDPMessage(String host, String port, String email) {
        this.host = host;
        this.port = port;
        this.email = email;
    }

    public UDPMessage(String raw) {
        int indexOfColon = raw.indexOf(":");
        int indexOfWhiteSpace = raw.indexOf(" ");
        this.host = raw.substring(0, indexOfColon);
        this.port = raw.substring(indexOfColon + 1, indexOfWhiteSpace);
        this.email = raw.substring(indexOfWhiteSpace + 1);
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return host + ':' + port + ' ' + email;
    }
}

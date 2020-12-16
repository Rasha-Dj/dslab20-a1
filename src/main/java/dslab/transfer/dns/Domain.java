package dslab.transfer.dns;

public class Domain {
    private String host;
    private int port;

    public Domain(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Domain{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}

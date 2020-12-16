package dslab.transfer.dns;

import dslab.util.Config;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class DNSLookupService {

    private Config config;
    Map<String, Domain> domainMap = new HashMap<>();

    public DNSLookupService() {
        this.config = new Config("domains");
        this.config.listKeys().forEach(s -> {
            String server = this.config.getString(s);
            int splitAt = server.indexOf(':');
            domainMap.put(s, new Domain(server.substring(0, splitAt), Integer.parseInt(server.substring(splitAt + 1))));
        });
    }

    public Domain getDestinationDomain(String serverName) throws UnknownHostException {
        return new Domain(getDestinationHost(serverName), getDestinationPort(serverName));
    }

    public String getDestinationHost(String serverName) throws UnknownHostException {
        if (!domainMap.containsKey(serverName)) throw new UnknownHostException("Unknown server " + serverName);
        return domainMap.get(serverName).getHost();
    }

    public int getDestinationPort(String serverName) {
        return domainMap.get(serverName).getPort();
    }

}

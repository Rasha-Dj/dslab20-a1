package dslab.monitoring;

import java.util.HashMap;
import java.util.Map;

public class MonitoringPersistence implements IMonitoringPersistence {

    private Map<String, Integer> addresses = new HashMap<>();
    private Map<String, Integer> servers = new HashMap<>();

    @Override
    public void storeMessage(UDPMessage udpMessage) {
        String addressKey = udpMessage.getEmail();
        String serverKey = udpMessage.getHost() + ":" + udpMessage.getPort();

        addresses.computeIfPresent(addressKey, (k, v) -> v + 1);
        servers.computeIfPresent(serverKey, (k, v) -> v + 1);

        addresses.put(addressKey, addresses.getOrDefault(addressKey, 1));
        servers.put(serverKey, servers.getOrDefault(serverKey, 1));
    }

    @Override
    public String generateAddressReport() {
        StringBuilder stringBuilder = new StringBuilder();
        addresses.forEach((x, y) -> stringBuilder.append(x).append(" ").append(y).append("\n"));
        return stringBuilder.toString();
    }

    @Override
    public String generateServerReport() {
        StringBuilder stringBuilder = new StringBuilder();
        servers.forEach((x, y) -> stringBuilder.append(x).append(" ").append(y).append("\n"));
        return stringBuilder.toString();
    }
}

package dslab.monitoring;

public interface IMonitoringPersistence {
    void storeMessage(UDPMessage udpMessage);
    String generateAddressReport();
    String generateServerReport();
}

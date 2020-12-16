package dslab.util.email;

public class EmailValidator {
    public static boolean isValid(String emailAddress) {
        return emailAddress.contains("@") && emailAddress.contains(".");
    }

    public static boolean isMessageValid(EmailDTO emailDTO) {
        return emailDTO.getData() != null && emailDTO.getFrom() != null && emailDTO.getSubject() != null && emailDTO.getTo() != null;
    }

    public static String getHost(String emailAddress) {
        return emailAddress.substring(emailAddress.indexOf("@"));
    }
}

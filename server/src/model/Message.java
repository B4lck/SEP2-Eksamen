package model;

import utils.DataMap;

import java.util.List;

public interface Message {
    /**
     * Henter beskedens unikke id
     * @return unikke id
     */
    long getMessageId();

    /**
     * Henter id'et på senderen
     * @return id'et på senderen
     */
    long getSentBy();

    /**
     * Henter bodyen på beskeden
     * @return bodyen på beskeden
     */
    String getBody();

    /**
     * Henter tidspunktet beskeden blev sendt på
     * @return tidspunkt beskeden blev sendt på
     */
    long getDateTime();

    /**
     * Laver beskeden om til et map, som kan sendes til clienten
     * @return alle instans variabler som map.
     */
    DataMap getData();

    /**
     * Henter id'et på chatrummet, hvori beskeden er
     * @return Id'et på chatrummet
     */
    long getChatRoom();

    /**
     * Henter en kopi af attachments listen
     */
    List<String> getAttachments();

    /**
     * Redigere beskedens body, og giver den et mærkat som redigeret
     * @param messageBody Den nye body
     * @param byUserId Id'et på brugeren som forsøger at ændre beskedens body
     */
    void editBody(String messageBody, long byUserId);

    /**
     * Ændre beskedens body til "[BESKEDEN ER SLETTET]"
     * Fjerner også beskedens bilag, men sletter dog ikke selve filerne, det skal selv gøres efter
     * @param byUserId - Id'et på brugeren som forsøger at slette beskeden
     */
    void deleteContent(long byUserId);

    /**
     * Tilføjer et bilag til beskeden
     * @param fileName - Navnet på bilaget, bilaget skal være uploadet for at kunne hentes!
     */
    void addAttachment(String fileName);

    /**
     * Fjerner et bilag fra beskeden
     * Fjerner dog ikke selve filen, dette skal selv gøres efter
     * @param fileName - Navnet på bilaget
     */
    void removeAttachment(String fileName);
}

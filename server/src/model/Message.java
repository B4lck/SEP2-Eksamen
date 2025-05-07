package model;

import utils.DataMap;

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
     * Redigere beskedens body, og giver den et mærkat som redigeret
     * @param messageBody Den nye body
     * @param byUserId Id'et på brugeren som forsøger at ændre beskedens body
     */
    void editBody(String messageBody, long byUserId);

    /**
     * Ændre bodyen på beskeden til "[BESKEDEN ER SLETTET]"
     * @param byUserId Id'et på brugeren som forsøger at slette beskeden
     */
    void deleteContent(long byUserId);

    /**
     * Tilføjer et bilag til beskeden
     * @param fileName navnet på bilagets fil
     */
    void addAttachment(String fileName);
}

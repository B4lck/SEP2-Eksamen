package model;

import utils.DataMap;

import java.util.Map;

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

    long getChatRoom();

    void editBody(String messageBody, long byUserId);
}

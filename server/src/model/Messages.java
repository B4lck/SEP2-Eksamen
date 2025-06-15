package model;

import utils.PropertyChangeSubject;

import java.util.List;

public interface Messages extends ServerRequestHandler, PropertyChangeSubject {
    /**
     * Send en besked i et chatrum.
     *
     * @param roomId       Chatrummets id.
     * @param messageBody  Beskedens body.
     * @param senderUserId Id'et på brugeren som sender beskeden.
     * @return Message objekt for beskeden som er blevet sendt.
     * @throws IllegalStateException    Hvis rummet ikke findes.
     * @throws IllegalStateException    Hvis brugeren ikke findes.
     * @throws IllegalStateException    Hvis brugeren ikke har adgang til rummet.
     * @throws IllegalStateException    Hvis brugeren er muted.
     * @throws IllegalArgumentException Hvis beskedens body er null.
     * @throws IllegalArgumentException Hvis beskedens body er tom, og der ikke er vedhæftet bilag.
     * @throws IllegalArgumentException Hvis attachments er null eller indeholder null strenge.
     */
    Message sendMessage(long roomId, String messageBody, List<String> attachments, long senderUserId);

    /**
     * Henter de nyeste beskeder i et chatrum.
     *
     * @param roomId Chatrummets id.
     * @param amount Antal beskeder.
     * @return Returnerer Message-objekter for de nyeste beskeder.
     * @throws IllegalArgumentException Hvis amount er 0 eller negativ.
     * @throws IllegalStateException    Hvis chatrummet ikke findes.
     */
    List<Message> getMessages(long roomId, int amount);

    /**
     * Henter de nyeste beskeder i et chatrum.
     *
     * @param roomId Chatrummets id.
     * @param amount Antal beskeder.
     * @param userId Id'et på brugeren som henter beskederne.
     * @return Returnerer Message-objekter for de nyeste beskeder.
     * @throws IllegalArgumentException Hvis amount er 0 eller negativ.
     * @throws IllegalStateException    Hvis chatrummet ikke findes.
     * @throws IllegalStateException    Hvis brugeren ikke findes.
     * @throws IllegalStateException    Hvis brugeren ikke har adgang til chatrummet.
     */
    List<Message> getMessages(long roomId, int amount, long userId);

    /**
     * Henter beskeder umiddelbart før en bestemt besked.
     *
     * @param messageId ID'et på den besked, hvor ældre beskeder skal findes.
     * @param amount    Antal beskeder som skal hentes.
     * @param userId    Id'et på brugeren som henter beskederne.
     * @return Returnerer Message-objekter for beskeder før beskeden med messageId.
     * @throws IllegalArgumentException Hvis amount er 0 eller negativ.
     * @throws IllegalStateException    Hvis beskeden ikke findes.
     * @throws IllegalStateException    Hvis brugeren ikke findes.
     * @throws IllegalStateException    Hvis brugeren ikke har adgang til beskedens chatrum.
     */
    List<Message> getMessagesBefore(long messageId, int amount, long userId);

    /**
     * Henter en besked objekt, ud fra et id
     *
     * @param messageId id'et på beskeden
     * @param userId    id'et på brugeren som henter beskeden
     * @return beskeden som objekt
     * @throws IllegalStateException Hvis brugeren ikke findes
     * @throws IllegalStateException Hvis beskeden ikke findes
     * @throws IllegalStateException Hvis brugeren ikke har adgang til rummet som beskeden hører til
     * @throws RuntimeException      Hvis serveren støder på en SQL-fejl
     */
    Message getMessage(long messageId, long userId);

    /**
     * Sender en system besked til et chatrum.
     *
     * @param roomId  ID'et på chatrummet.
     * @param message Server-beskedens body.
     * @throws IllegalStateException Hvis chatrummet ikke findes.
     */
    void sendSystemMessage(long roomId, String message);

    /**
     * Rediger en besked.
     *
     * @param messageId ID'et på beskeden.
     * @param userId    ID'et på brugeren, som forsøger at redigere beskeden.
     * @throws IllegalStateException Hvis brugeren ikke findes.
     * @throws IllegalStateException Hvis beskeden ikke findes.
     * @throws IllegalStateException Hvis brugeren ikke har adgang til beskedens chatrum.
     * @throws IllegalStateException Hvis brugeren ikke har adgang til at redigere beskeden.
     */
    void editMessage(long messageId, String messageBody, long userId);

    /**
     * Sletter en besked.
     *
     * @param messageId ID'et på beskeden.
     * @param userId    ID'et på brugeren, som forsøger at slette beskeden.
     * @throws IllegalStateException Hvis brugeren ikke findes.
     * @throws IllegalStateException Hvis beskeden ikke findes.
     * @throws IllegalStateException Hvis brugeren ikke har adgang til beskedens chatrum.
     * @throws IllegalStateException Hvis brugeren ikke har adgang til at slette beskeden.
     * @implNote Bilag skal også slettes.
     */
    void deleteMessage(long messageId, long userId);

    /**
     * Tilføjer en reaktion til en besked.
     *
     * @param messageId ID'et på beskeden.
     * @param reaction  Streng med reaktions-emojien som skal tilføjes.
     * @param userId    ID'et på brugeren som reagere på beskeden.
     * @throws IllegalStateException Hvis brugeren ikke findes.
     * @throws IllegalStateException Hvis beskeden ikke findes.
     * @throws IllegalStateException Hvis brugeren ikke har adgang til beskedens chatrum.
     */
    void addReaction(long messageId, String reaction, long userId);

    /**
     * Fjerner en reaktion fra beskeden.
     *
     * @param messageId ID'et på beskeden.
     * @param reaction  Streng med reaktions-emojien som skal fjernes.
     * @param userId    ID'et på brugeren.
     * @throws IllegalStateException Hvis brugeren ikke findes.
     * @throws IllegalStateException Hvis beskeden ikke findes.
     * @throws IllegalStateException Hvis brugeren ikke har adgang til beskedens chatrum.
     */
    void removeReaction(long messageId, String reaction, long userId);

    /**
     * Setter brugerens nyeste læste besked.
     *
     * @param messageId ID'et på den nyeste besked som brugeren har læst.
     *                  Hvis brugeren tidligere har læst en besked i samme chatrum, og den besked er nyere, vil intet ske.
     * @param userId    ID'et på brugeren som har læst beskeden.
     * @throws IllegalStateException Hvis brugeren ikke findes.
     * @throws IllegalStateException Hvis beskeden ikke findes.
     * @throws IllegalStateException Hvis brugeren ikke har adgang til beskedens chatrum.
     */
    void setLatestReadMessage(long messageId, long userId);
}

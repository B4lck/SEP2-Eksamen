package model;

import utils.DataMap;

public interface Profile {
    /**
     * Henter profilens unikke ID
     * @return
     */
    long getUUID();

    /**
     * Henter brugernavnet på profilen
     * @return brugernavnet
     */
    String getUsername();

    /**
     * Sætter brugernavnet på profilen
     * @param username nye brugernavn
     */
    void setUsername(String username);

    /**
     * Checker om passwordet stemmet overens med argument
     * @param password Password som skal tjekkes for match
     * @return True hvis passwords matcher
     */
    boolean checkPassword(String password);

    /**
     * Sætter passworded på profilen
     * @param password nye password
     */
    void setPassword(String password);

    /**
     * Laver profilen om til et map, som kan sendes til clienten
     * @return alle instans variabler som map.
     */
    DataMap getData();
}

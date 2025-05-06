package model;

import mediator.ServerRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ChatModel implements Model, PropertyChangeListener {
    private Profiles profiles;
    private Messages messages;
    private Rooms rooms;
    private PropertyChangeSupport property;

    public ChatModel() {
        property = new PropertyChangeSupport(this);
        profiles = new ProfilesArrayListManager(this);
        messages = new MessagesArrayListManager(this);
        rooms = new RoomsArrayListManager(this);

        messages.addListener(this);

        // Dummy data

        var user1 = profiles.createProfile("Mazen Laursen", "1234");
        var user2 = profiles.createProfile("Malthe Balck", "1234");
        var user3 = profiles.createProfile("Nikolai Sharaf", "1234");
        var user4 = profiles.createProfile("1", "1");
        profiles.createProfile("Bruger5", "1234");
        profiles.createProfile("Bruger6", "1234");
        profiles.createProfile("Bruger7", "1234");
        profiles.createProfile("Bruger8", "1234");

        var rum = rooms.createRoom("Rum nr 1", user1.getUUID());
        rooms.addUser(rum.getRoomId(), user2.getUUID(), user1.getUUID());
        rooms.addUser(rum.getRoomId(), user3.getUUID(), user1.getUUID());
        rooms.addUser(rum.getRoomId(), user4.getUUID(), user1.getUUID());
        rooms.createRoom("Rum nr 2", user1.getUUID());

        long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) / 10L + 100000;

        messages.sendMessage(rum.getRoomId(), "Godmorgen! Er du også på vej på arbejde?", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ja, sidder i bussen nu. Trafikken er forfærdelig i dag 😫", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Åh nej, jeg tager metroen i dag heldigvis", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Smart! Jeg burde også gøre det", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Har du forberedt dig til præsentationen?", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ja, arbejdede på den til sent i går", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Du er bare for sej! Jeg glæder mig til at se den", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Håber bare teknikken virker denne gang 😅", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Det gør den! Jeg har dobbelttjekket projektor", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Du er en livredder! ❤️", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Skal vi mødes til en hurtig kaffe inden?", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ja, please! Jeg har brug for koffein", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Same! Jeg køber. Du havde sidst", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Tak! Du er bare for god", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Nå, nu holder bussen bare stille...", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Igen? Det er 3. gang denne uge 🙄", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ja, der er vist et uheld længere fremme", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Skal jeg vente med kaffen?", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Nej nej, start du bare. Jeg skynder mig", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ok! Jeg tager din sædvanlige med", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Endelig bevæger bussen sig igen", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Nice! Jeg har fundet os et bord", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ved vinduet? 😊", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Selvfølgelig! Kender jo din favorit plads", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "5 min væk nu!", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Perfect timing - kaffen er lige kommet", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Åh, jeg kan dufte den helt herud 😆", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Haha! Så er du tæt på", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Yes! Kan se dig nu", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Tak for kaffen! Den var tiltrængt", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Selv tak! God præsentation forresten", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Tak! Er lettet over den er overstået", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Du klarede det så godt! Chefen var imponeret", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Virkelig? Det er jeg glad for at høre", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Skal vi fejre det med frokost?", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "God ide! Hvor vil du hen?", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Der er åbnet en ny sushi restaurant", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Perfekt! Jeg elsker sushi 🍣", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Skal vi sige kl 12?", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ja! Jeg har et kort møde inden", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Ok, jeg reserverer bord", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Du er bare på toppen i dag! 🌟", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Bord er reserveret! De havde vinduesplads", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Yes! Du kender mig så godt 😊", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Mødet trækker ud... 😫", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "No stress, jeg venter", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Tak! 10 min mere maks", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Jeg kigger på menuen imens", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Endelig færdig! På vej nu", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "De har en vild antal rolls!", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Åh nej, nu bliver det svært at vælge 😅", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Vi kan dele nogle forskellige?", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "God ide! Jeg stoler på dine valg", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Er der om 2 min!", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Tak for en hyggelig frokost!", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "I lige måde! Vi må gøre det igen snart", user2.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Helt sikkert! God eftermiddag 😊", user1.getUUID(), (time++) * 10000);
        messages.sendMessage(rum.getRoomId(), "Dig også! Ses i morgen", user2.getUUID(), (time++) * 10000);
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public Messages getMessages() {
        return messages;
    }

    @Override
    public Rooms getRooms() {
        return rooms;
    }

    @Override
    public void passClientMessage(ServerRequest message) {
        UserFilesManager.getInstance().handleMessage(message);
        profiles.handleMessage(message);
        messages.handleMessage(message);
        rooms.handleMessage(message);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        property.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
}

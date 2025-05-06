package model;

import mediator.ClientMessage;
import mediator.ServerRequest;
import utils.DataMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class UserFilesManager {

    static UserFilesManager instance = new UserFilesManager();

    private UserFilesManager() {

    }

    public static UserFilesManager getInstance() {
        return instance;
    }

    public void handleMessage(ServerRequest message) {
        if (message.getType().equals("DOWNLOAD_FILE")) {
            String fileId = message.getData().getString("fileId");

            File file = new File("uploads/" + fileId);

            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            System.out.println("sender fil info");

            message.respond(new ClientMessage("FILE_INFO", new DataMap()
                    .with("name", fileId)));

            System.out.println("sender fil...");

            message.respond(in, fileId);
        }
    }
}

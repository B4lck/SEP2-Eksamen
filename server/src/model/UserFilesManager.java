package model;

import mediator.ServerRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class UserFilesManager implements ServerRequestHandler {

    public static final String UPLOADS_DIRECTORY = "uploads";

    private static UserFilesManager instance = new UserFilesManager();

    private UserFilesManager() {

    }

    public static UserFilesManager getInstance() {
        return instance;
    }

    @Override
    public void handleRequest(ServerRequest message) {
        if (message.getType().equals("DOWNLOAD_FILE")) {
            String fileId = message.getData().getString("fileId");

            File file = new File("uploads/" + fileId);

            FileInputStream in;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            message.respond(in, fileId);
        }
    }

    public void removeFiles(List<String> attachments) {
        for (String attachment : attachments) {
            File file = new File(UPLOADS_DIRECTORY + "/" + attachment);
            if (file.exists()) file.delete();
        }
    }
}

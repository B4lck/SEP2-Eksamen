package util;

import java.io.File;

public class Attachment {
    private String name;
    private File file;

    public Attachment(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }
}

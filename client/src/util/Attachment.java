package util;

import java.io.FileInputStream;

public class Attachment {
    private String name;
    private FileInputStream stream;

    public Attachment(String name, FileInputStream stream) {
        this.name = name;
        this.stream = stream;
    }

    public String getName() {
        return name;
    }

    public FileInputStream getStream() {
        return stream;
    }
}

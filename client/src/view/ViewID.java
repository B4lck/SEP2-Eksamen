package view;

public enum ViewID {
    ; // her mangler der noget
    private final String filename;
    private ViewID(String filename) {
        this.filename = filename;
    }
    public String getFilename() {
        return filename;
    }
}

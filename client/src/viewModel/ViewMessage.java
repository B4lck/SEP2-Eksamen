package viewModel;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class ViewMessage {
    public String sender;
    public String body;
    public LocalDateTime dateTime;
    public long messageId;
    public boolean isSystemMessage;
    public List<File> attachments;
}

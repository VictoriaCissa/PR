package lab6;


public interface Logger {
    void addEvent(String who, String event);
    void addError(String who, String event);
    void displayErrorMsg(String title, String text);
    void displayResultMsg(String title, String text);
    void stop();
}

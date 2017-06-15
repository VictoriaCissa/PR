package lab6;


interface VikaProtocol {
    String ERROR_TYPE = "error";
    String AUTH_TYPE = "auth";
    String EXIT_TYPE = "exit";
    String EXP_TYPE = "exp";
    String ADD_TYPE = "add";
    String FIND_TYPE = "find";
    Message read();
    void send(Message msg);
}

package lab6;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {
    private ServerSocket serverSocket;
    private ServerListener serverListener;
    private Database database;
    private Logger logger;
    private List<ServerWorker> workers;

    public Server(Logger logger) {
        this.logger = logger;
    }

    public void start(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            this.database = new Database();
            this.workers = Collections.synchronizedList(new ArrayList<>());
            logger.addEvent("SERVER", "STARTED");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        System.out.println("STOPPING THE SERVER");
        if (serverListener != null) {
            serverListener.isWorking = false;
            try {
                serverSocket.close();
                for (ServerWorker serverWorker : workers)
                    serverWorker.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            serverListener = null;
        }
    }

    public void listen() {
        if (serverListener != null)
            return;
        this.serverListener = new ServerListener(serverSocket);
        (new Thread(serverListener)).start();
        logger.addEvent("SERVER", "LISTENING");
    }


    private class ServerListener implements Runnable {
        private boolean isWorking;
        private ServerSocket serverSocket;

        public ServerListener(ServerSocket serverSocket) {
            this.isWorking = true;
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            while (isWorking) {
                try {
                    Socket socket = serverSocket.accept();
                    workers.add(new ServerWorker(socket));
                } catch (IOException i) {

                }
            }
        }
    }

    private class ServerWorker implements Runnable, VikaProtocol {
        private PrintWriter writer;
        private BufferedReader reader;
        private Socket socket;
        private boolean authenticated;
        private String login;
        private boolean isWorking;
        private String token;

        public ServerWorker(Socket socket) {
            this.socket = socket;
            try {
                logger.addEvent("SERVER", "NEW CONNECTION ESTABILISHED");
                this.writer = new PrintWriter(socket.getOutputStream(), false);
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.isWorking = true;
                (new Thread(this)).start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void stop() {
            isWorking = false;
            try {
                socket.close();
            } catch (IOException e) {

            }
        }

        @Override
        public Message read() {
            try {
                String content = reader.readLine();
                try {
                    JSONObject jsonObject = (JSONObject) new JSONParser().parse(content);
                    logger.addEvent("CLIENT", "FROM " + (authenticated ? login : "anonymus") + ": " + content);
                    Message message = new Message(jsonObject.toJSONString());
                    return message;
                } catch (ParseException p) {
                    logger.addError("CLIENT", "FROM " + (authenticated ? login : "anonymus") + " (NOT JSON): " + content);
                    return null;
                }
            } catch (Exception e) {
                logger.addError("SERVER", (authenticated ? login : "anonymus") + ": disconnecting...");
            }
            return null;
        }

        @Override
        public void send(Message message) {
            if (message.getType().equals("error"))
                logger.addError("SERVER", "SENT TO CLIENT " + message.toString());
            else
                logger.addEvent("SERVER", "SENT TO CLIENT " + message.toString());
            writer.println(message);
            writer.flush();
        }

        @Override
        public void run() {
            try {
                Message message = read();
                if (message == null) {
                    message = new Message.Builder(ERROR_TYPE)
                            .addParam("info", "bad request")
                            .addParam("code", "400")
                            .build();
                    socket.close();
                    send(message);
                    return;
                }
                login = message.getParam("login");
                String password = message.getParam("password");
                if (login.length() == 0 && password.length() == 0)
                    login = "guest";
                else {
                    synchronized (database) {
                        boolean result = database.checkCredentials(login, password);
                        if (!result) {
                            message = new Message.Builder(ERROR_TYPE)
                                    .addParam("info", "access denied")
                                    .addParam("code", "403")
                                    .build();
                            send(message);
                            writer.flush();
                            socket.close();
                            return;
                        }
                    }
                }
                logger.addEvent("SERVER", login + " AUTHENTICATED SUCCESSFULLY");
                authenticated = true;
                token = generateRandomString(32);
                message = new Message.Builder("success")
                        .addParam("info", "success")
                        .addParam("code", "200")
                        .addParam("token", token)
                        .build();
                send(message);
                while (isWorking && !socket.isClosed() && socket.isConnected()) {
                    try {
                        Message request = read();
                        if (request == null) {
                            socket.close();
                        } else if (request.getParam("token") == null || !request.getParam("token").equals(token)) {
                            message = new Message.Builder(ERROR_TYPE)
                                    .addParam("info", "access denied")
                                    .addParam("code", "403")
                                    .build();
                            send(message);
                            writer.flush();
                            socket.close();
                            return;
                        } else
                            switch (request.getType()) {
                                case EXP_TYPE:
                                    send(new Message.Builder(EXP_TYPE)
                                            .addParam("info", "success")
                                            .addParam("code", "200")
                                            .addParam("result", String.valueOf(Math.exp(Double.parseDouble(request.getParam("value")))))
                                            .build());
                                    break;
                                case FIND_TYPE:
                                    send(new Message.Builder(FIND_TYPE)
                                            .addParam("result", database.find(request.getParam("value")))
                                            .addParam("value", request.getParam("value"))
                                            .build());
                                    break;
                                case ADD_TYPE:
                                    if (login.equals("guest")) {
                                        message = new Message.Builder(ERROR_TYPE)
                                                .addParam("info", "access denied")
                                                .addParam("code", "403")
                                                .build();
                                        send(message);
                                        return;
                                    }
                                    boolean result = database.add(request.getParam("word"),
                                            request.getParam("definition"));
                                    if (result)
                                        send(new Message.Builder(ADD_TYPE)
                                                .addParam("result", "success!")
                                                .build());
                                    else
                                        send(new Message.Builder(ERROR_TYPE)
                                                .addParam("info", "definition already exists!")
                                                .build());
                                    break;
                                case EXIT_TYPE:
                                    socket.close();
                                    break;
                            }
                    } catch (Exception e) {
                        socket.close();
                    }
                }
                try {
                    socket.close();
                } catch (Exception e) {
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {

                }
            }
        }
    }

    private static String generateRandomString(int length) {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder str = new StringBuilder();
        Random rnd = new Random();
        while (str.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            str.append(SALTCHARS.charAt(index));
        }
        String saltStr = str.toString();
        return saltStr;
    }
}

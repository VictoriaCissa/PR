package lab6;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;


public class Client {
    private int port;
    private String login, password;
    private Logger logger;
    private Queue<Message> tasks;
    private ClientListener clientListener;

    private Client(Builder builder) {
        this.port = builder.port;
        this.login = builder.login;
        this.password = builder.password;
        this.logger = builder.logger;
        this.tasks = new ConcurrentLinkedDeque<>();
    }

    public void start() {
        try {
            clientListener = new ClientListener(new Socket("localhost", port));
            (new Thread(clientListener)).start();
            logger.addEvent("CLIENT", "STARTED");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calcExp(double val) {
        Message task = new Message.Builder(VikaProtocol.EXP_TYPE)
                .addParam("value", String.valueOf(val))
                .build();
        tasks.add(task);
    }

    public void find(String val) {
        Message task = new Message.Builder(VikaProtocol.FIND_TYPE)
                .addParam("value", val)
                .build();
        tasks.add(task);
    }

    public void add(String word, String definition) {
        Message task = new Message.Builder(VikaProtocol.ADD_TYPE)
                .addParam("word", word)
                .addParam("definition", definition)
                .build();
        tasks.add(task);
    }

    public void stop() {
        if (clientListener != null)
            clientListener.isWorking = false;
        logger.addEvent("CLIENT", "STOPPED");

    }

    private class ClientListener implements Runnable, VikaProtocol {
        private Socket socket;
        private boolean isWorking;
        private PrintWriter writer;
        private BufferedReader reader;
        private String token;

        public ClientListener(Socket socket) {
            this.socket = socket;
            logger.addEvent("CLIENT", "CREATED LISTENER");
            isWorking = true;
            try {
                writer = new PrintWriter(socket.getOutputStream(), false);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public Message read() {
            try {
                String content = reader.readLine();
                try {
                    JSONObject jsonObject = (JSONObject) new JSONParser().parse(content);
                    Message message = new Message(jsonObject.toJSONString());
                    return message;
                } catch (ParseException p) {
                    p.printStackTrace();
                    if (content != null)
                        logger.addEvent("SERVER", content);
                    logger.addError("SERVER", content);
                    return null;
                }
            } catch (Exception e) {
                logger.addError("SERVER", "no connection");
            }
            return null;
        }

        @Override
        public void send(Message message) {
            if (message.getType().equals("error"))
                logger.addError("CLIENT", "SENT TO SERVER: " + message.toString());
            else
                logger.addEvent("CLIENT", "SENT TO SERVER: " + message.toString());
            writer.println(message);
            writer.flush();
        }

        @Override
        public void run() {
            try {
                //Authentication
                Message authRequest = new Message.Builder(AUTH_TYPE)
                        .addParam("login", login)
                        .addParam("password", password)
                        .build();
                send(authRequest);
                logger.addEvent("CLIENT", "SENT TO SERVER: " + authRequest.toString());
                //Geting auth result
                Message response = read();
                if (response.getType().equals("error")) {
                    logger.stop();
                    logger.displayErrorMsg("Access Denied!", "Invalid login or password!");
                } else
                    token = response.getParam("token");
                //Loop of tasks
                while (isWorking) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    if (tasks.size() > 0) {
                        Message msg = tasks.poll();
                        msg.addParam("token", token);
                        send(msg);
                        response = read();
                        if (response == null) {
                            logger.stop();
                            logger.displayErrorMsg("No Connection!", "You were disconnected!");
                            return;
                        }
                        if (!response.getType().equals("error")) {
                            if (response.getType().equals("find"))
                                logger.displayResultMsg("RESULT", response.getParam("value") + " - " + response.getParam("result"));
                            else
                                logger.displayResultMsg("RESULT", response.getParam("result"));
                        } else {
                            logger.displayErrorMsg("Error!", response.getParam("info"));
                        }
                    }
                }
                Message message = new Message.Builder(EXIT_TYPE)
                        .addParam("cause", "manual stop")
                        .build();
                send(message);
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public static class Builder {
        private int port;
        private String login, password;
        private Logger logger;

        public Builder setLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setAuthentication(String login, String password) {
            this.login = login;
            this.password = password;
            return this;
        }

        public Client build() {
            return new Client(this);
        }
    }
}

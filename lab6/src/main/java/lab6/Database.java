package lab6;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


public class Database {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/lab6";
    private static final String USER = "lab6";
    private static final String PASS = "labpass";
    private Map<String, String> users;
    private Map<String, String> dict;

    public Database() throws Exception {
        Statement stmt = null;
        Class.forName(JDBC_DRIVER);
        users = new HashMap<>();
        users.put("admin", "password");
        users.put("vika", "password");
        dict = new HashMap<>();
        dict.put("tree", "a kind of plant");
        dict.put("sun", "our nearest star");
    }


    public boolean checkCredentials(String login, String password) {
        if (users.get(login) != null && password.equals(users.get(login)))
            return true;
        else
            return false;
    }


    public boolean add(String word, String definition) {
        if (dict.get(word) != null)
            return false;
        else
            dict.put(word, definition);
        return true;
    }

    public String find(String str) {
        if (dict.get(str) != null)
            return dict.get(str);
        if (dict.get(str.toLowerCase()) != null)
            return dict.get(str.toLowerCase());
        return null;
    }
}

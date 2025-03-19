package dataBaseAccessSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseAccessSQL {
    public static Connection connectionSMS;

    public static Connection getConnectionSMS() {
        try {
            if (connectionSMS == null || connectionSMS.isClosed()) {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                connectionSMS = DriverManager.getConnection(
                        "jdbc:jtds:sqlserver://10.195.105.247/LogDB;domain=CORP;useNTLMv2=true;trustServerCertificate=true",
                        "tamarmakharashvili",
                        "Testireba111");
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return connectionSMS;
    }
}

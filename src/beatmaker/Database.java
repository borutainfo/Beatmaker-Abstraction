/**
 * Sebastian Boruta
 * sebastian@boruta.info
 */
package beatmaker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private static Connection conn = null;

    public boolean connect() {
        Properties connectionProps = new Properties();
        connectionProps.put("user", ""); // TODO
        connectionProps.put("password", ""); // TODO
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@//", connectionProps); // TODO
            System.out.println("Połączono z bazą danych");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, "nie udało się połączyć z bazą danych", ex);
            return false;
            //System.exit(-1);
        }
    }

    public boolean isConnected() {
        try {
            if (conn != null) {
                return conn.isValid(0);
            } else {
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, "nie udało się sprawdzić statusu połączenia", ex);
            System.exit(-1);
            return false;
        }
    }

    public ResultSet query(String sql) {
        ResultSet rs = null;
        if (isConnected()) {
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
            } catch (SQLException ex) {
                Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rs;
    }

    public int update(String sql) {
        int changes = 0;
        if (isConnected()) {
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                changes = stmt.executeUpdate(sql);
            } catch (SQLException ex) {
                Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return changes;
    }

    public void disconnect() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Rozłączono z bazą danych");
    }
}

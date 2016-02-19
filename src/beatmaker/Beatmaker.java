/**
 * Sebastian Boruta
 * sebastian@boruta.info
 */
package beatmaker;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Beatmaker {

    // łączenie z bazą danych
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.out.println("UIManager Exception : " + ex);
        }
        
        GUI interfejs = new GUI();
        interfejs.setVisible(true);
    }

}

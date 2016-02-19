/**
 * Sebastian Boruta
 * sebastian@boruta.info
 */
package beatmaker;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.table.DefaultTableModel;

public class GUI extends javax.swing.JFrame {

    /**
     * Creates new form MainPage
     */
    static Database db = new Database();
    static Validator valid = new Validator();
    static int logged = 0;
    static String login = "";
    static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static Date date = new Date();
    static String play = "";
    static Hash hash;

    public GUI() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("ikona.png")));
        initComponents();
        checkConnection();
        checkLogin();
        beatsTable.removeColumn(beatsTable.getColumnModel().getColumn(5));
        forsaleTable.removeColumn(forsaleTable.getColumnModel().getColumn(5));
        //soldTable.removeColumn(soldTable.getColumnModel().getColumn(4));
        //boughtTable.removeColumn(boughtTable.getColumnModel().getColumn(4));
        historyTextPane.setContentType("text/html");
        //new java.util.Timer().schedule(new java.util.TimerTask() {@Override public void run() { checkConnection(); } }, 2500 );
    }

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        System.out.println("Program zakończony przez użytkownika.");
        System.exit(0);
    }

    private boolean checkConnection() {
        return checkConnection(0);
    }

    private boolean checkLogin() {
        if (logged <= 0) {
            loginStatusLabel.setText("Niezalogowany - ograniczony dostęp do niektórych funkcji");
            moneyStatusLabel.setText("Brak danych");
            logged = 0;
            login = "";
            addbeatItem.setEnabled(false);
            mysalesItem.setEnabled(false);
            myshoppingItem.setEnabled(false);
            logoutItem.setEnabled(false);
            walletItem.setEnabled(false);
            loginItem.setEnabled(true);
            beatBuyButton.setEnabled(false);
            refreshButton.setEnabled(false);
            return false;
        } else {
            try {
                ResultSet rs = db.query("SELECT login, money FROM users WHERE id = '" + logged + "'");
                if (rs.next()) {
                    loginStatusLabel.setText("Zalogowany jako: " + rs.getString(1));
                    moneyStatusLabel.setText("" + rs.getFloat(2) + " PLN");
                    addbeatItem.setEnabled(true);
                    mysalesItem.setEnabled(true);
                    myshoppingItem.setEnabled(true);
                    logoutItem.setEnabled(true);
                    walletItem.setEnabled(true);
                    loginItem.setEnabled(false);
                    beatBuyButton.setEnabled(true);
                    refreshButton.setEnabled(true);
                    return true;
                } else {
                    logged = 0;
                    return checkLogin();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
    }

    private boolean checkConnection(int milisec) {
        if (milisec > 0) {
            try {
                Thread.sleep(milisec);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (db.isConnected()) {
            connectionStatusLabel.setText("Połączenie z serwerem ustanowione.");
            connectionStatusLabel.setForeground(Color.black);
            ConnectionButton.setText("Rozłącz");
            beatsMenu.setEnabled(true);
            userMenu.setEnabled(true);
            disconnectItem.setEnabled(true);
            connectItem.setEnabled(false);
            return true;
        } else {
            logged = 0;
            login = "";
            checkLogin();
            connectionStatusLabel.setText("Brak połączenia z serwerem.");
            connectionStatusLabel.setForeground(Color.red);
            ConnectionButton.setText("Połącz");
            beatsMenu.setEnabled(false);
            userMenu.setEnabled(false);
            disconnectItem.setEnabled(false);
            connectItem.setEnabled(true);
            changeFrame(mainFrame);
            return false;
        }
    }

    private void connect() {
        System.out.println("Użytkownik próbuje połączyc się z serwerem.");
        db.connect();
        checkConnection();
    }

    private void disconnect() {
        System.out.println("Użytkownik próbuje zakończyć połączenie z serwerem.");
        db.disconnect();
        checkConnection();
    }

    private void changeFrame(Component comp) {
        if (comp == loginFrame) {
            loginTextField.setText("");
            passTextField.setText("");
            loginregTextField.setText("");
            passregTextField.setText("");
            mailregTextField.setText("");
            menRadioButton.setSelected(true);
            womanRadioButton.setSelected(false);
        }
        if (comp == walletFrame) {
            float money = money(logged);
            if (money <= 0) {
                cashoutdataTextArea.setEnabled(false);
                cashoutTextField.setEnabled(false);
                cashoutButton.setEnabled(false);
            } else {
                cashoutdataTextArea.setEnabled(true);
                cashoutTextField.setEnabled(true);
                cashoutButton.setEnabled(true);
            }
            cashoutdataTextArea.setText("");
            cashoutTextField.setText(money + "");
            historyTextPane.setText("");
            moneyAmountLabel.setText(money + " PLN");
            historyTextPane.setText(history(logged));

        }
        if (comp == beatFrame) {
            beatBuyButton.setEnabled(false);
        }
        if (comp == addbeatFrame) {
            addbeatTitleTextField.setText("");
            addbeatDescriptionTextArea.setText("");
            addbeatAddressTextField.setText("");
            addbeatPriceTextField.setText("50.00");
            addbeatYRadioButton.setSelected(true);
            addbeatNRadioButton.setSelected(false);
        }
        framesPanel.removeAll();
        framesPanel.repaint();
        framesPanel.revalidate();
        framesPanel.add(comp);
        framesPanel.repaint();
        framesPanel.revalidate();
    }

    private void beat(int id) {
        changeFrame(beatFrame);
        try {
            ResultSet rs = db.query("Select * FROM beatshow where id = '" + id + "'");
            rs.next();
            beatNumberLabel.setText(id + "");
            beatTitleLabel.setText(rs.getString(2));
            descriptionPane.setText(rs.getString(3));
            beatAuthorLabel.setText(rs.getString(4));
            beatDateLabel1.setText(rs.getString(6));
            beatPriceLabel1.setText(rs.getString(7) + " PLN");
            if (logged == 0 || rs.getFloat(7) > money(logged) || (rs.getString(4).toLowerCase() == null ? login.toLowerCase() == null : rs.getString(4).toLowerCase().equals(login.toLowerCase()))) {
                beatBuyButton.setEnabled(false);
            } else {
                beatBuyButton.setEnabled(true);
            }
            play = rs.getString(5);
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
            changeFrame(showbeatsFrame);
        }
    }

    private float money(int id) {
        float money = 0;
        ResultSet rs = null;
        try {
            rs = db.query("Select money FROM users WHERE id = " + id);
            rs.next();
            money = rs.getFloat(1);
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return money;
    }

    private String history(int id) {
        String history = "<html><div style=\"font-size:8px\">";
        ResultSet rs = null;
        try {
            rs = db.query("Select seller_id, buyer_id, price, title, transaction_date FROM beats WHERE (seller_id = " + id + " OR buyer_id = " + id + ") AND status = 2 ORDER BY transaction_date DESC");
            while (rs.next()) {
                if (rs.getInt(1) == id) {
                    history += "<font style=\"color: green\"><b>" + rs.getString(5) + "</b>: Sprzedałeś \"" + rs.getString(4) + "\" za " + rs.getFloat(3) + " PLN.<br/>";
                } else if (rs.getInt(2) == id) {
                    history += "<font style=\"color: blue\"><b>" + rs.getString(5) + "</b>: Kupiłeś \"" + rs.getString(4) + "\" za " + rs.getFloat(3) + " PLN.<br/>";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return history + "</div></html>";
    }

    private void mysales() {
        System.out.println("Użytkownik zmienia ramkę na \"Moja sprzedaż\".");
        changeFrame(mysalesFrame);
        DefaultTableModel forsale = (DefaultTableModel) forsaleTable.getModel();
        DefaultTableModel sold = (DefaultTableModel) soldTable.getModel();
        forsale.setRowCount(0);
        sold.setRowCount(0);
        try {
            ResultSet rs = db.query("SELECT id, title, price, upload_date, status FROM beats WHERE seller_id = " + logged + " AND status = 1 ORDER BY upload_date DESC");
            while (rs.next()) {
                Object[] row = {rs.getString(2), rs.getFloat(3), rs.getString(4), "Nowa cena", "Usuń", rs.getInt(1)};
                forsale.addRow(row);
            }
            rs = db.query("SELECT b.title, b.transaction_date, u.login, u.mail, b.product_key FROM users u, beats b WHERE b.buyer_id = u.id AND seller_id = " + logged + " AND status = 2 ORDER BY b.transaction_date DESC");
            while (rs.next()) {
                Object[] row = {rs.getString(1), rs.getString(3) + "<" + rs.getString(4) + ">", rs.getString(5)};
                sold.addRow(row);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Object[] options = {"Tak", "Nie"};
            Action changeprice = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int id = (int) forsaleTable.getModel().getValueAt(Integer.valueOf(e.getActionCommand()), 5);
                    Object price = alert.showInputDialog(null, "Wprowadź nową cenę:", forsaleTable.getModel().getValueAt(Integer.valueOf(e.getActionCommand()), 1));
                    if (price == null || !valid.money(price.toString())) {
                        alert.showMessageDialog(null, "Wpisana cena nie jest prawidłowa!", "Komunikat", alert.WARNING_MESSAGE);
                    } else {
                        forsaleTable.getModel().setValueAt(Float.parseFloat(price.toString()), Integer.valueOf(e.getActionCommand()), 1);
                        if (db.update("UPDATE beats SET price = " + price.toString() + " WHERE id = " + id) == 1) {
                            System.out.println("Użytkownik zmienił cenę produkcji (id " + id + ").");
                        }
                    }
                }
            };
            Action deletebeat = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int id = (int) forsaleTable.getModel().getValueAt(Integer.valueOf(e.getActionCommand()), 5);
                    int odp = alert.showOptionDialog(null, "Jesteś pewien, że chcesz usunąć tą produkcję?", "Usuwanie produkcji", alert.YES_NO_CANCEL_OPTION, alert.QUESTION_MESSAGE, null, options, options[1]);
                    if (odp == 0) {
                        ((DefaultTableModel) forsaleTable.getModel()).removeRow(Integer.valueOf(e.getActionCommand()));
                        if (db.update("DELETE FROM beats WHERE id = " + id) >= 1) {
                            System.out.println("Użytkownik skasował produkcję (id " + id + ").");
                        }
                    }
                }
            };
            ButtonColumn buttonColumn1 = new ButtonColumn(forsaleTable, changeprice, 3);
            ButtonColumn buttonColumn2 = new ButtonColumn(forsaleTable, deletebeat, 4);
            //buttonColumn1.setMnemonic(KeyEvent.VK_D);
            //buttonColumn2.setMnemonic(KeyEvent.VK_D);
        }
    }

    private void myshopping() {
        System.out.println("Użytkownik zmienia ramkę na \"Moje zakupy\".");
        changeFrame(myshoppingFrame);
        DefaultTableModel bought = (DefaultTableModel) boughtTable.getModel();
        bought.setRowCount(0);
        try {
            ResultSet rs = db.query("SELECT b.title, b.transaction_date, u.login, u.mail, b.product_key FROM users u, beats b WHERE b.seller_id = u.id AND buyer_id = " + logged + " AND status = 2 ORDER BY b.transaction_date DESC");
            while (rs.next()) {
                Object[] row = {rs.getString(1), rs.getString(3) + "<" + rs.getString(4) + ">", rs.getString(5)};
                bought.addRow(row);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        alert = new javax.swing.JOptionPane();
        framesPanel = new javax.swing.JPanel();
        mainFrame = new javax.swing.JPanel();
        ConnectionButton = new javax.swing.JButton();
        logoLabel = new javax.swing.JLabel();
        startframeLabel = new javax.swing.JLabel();
        showbeatsFrame = new javax.swing.JPanel();
        beatsScrollPanel = new javax.swing.JScrollPane();
        beatsTable = new javax.swing.JTable();
        beatsFrameTitleLabel = new javax.swing.JLabel();
        beatFrame = new javax.swing.JPanel();
        beatFrameTitleLabel = new javax.swing.JLabel();
        beatNumberLabel = new javax.swing.JLabel();
        beatTitleLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionPane = new javax.swing.JTextPane();
        playButton = new javax.swing.JButton();
        beatAuthorLabel = new javax.swing.JLabel();
        beatProducentLabel = new javax.swing.JLabel();
        beatDateLabel = new javax.swing.JLabel();
        beatDateLabel1 = new javax.swing.JLabel();
        beatPriceLabel = new javax.swing.JLabel();
        beatPriceLabel1 = new javax.swing.JLabel();
        beatBuyButton = new javax.swing.JButton();
        loginFrame = new javax.swing.JPanel();
        loginPanel = new javax.swing.JPanel();
        loginTextField = new javax.swing.JTextField();
        loginLabel = new javax.swing.JLabel();
        passLabel = new javax.swing.JLabel();
        loginButton = new javax.swing.JButton();
        passTextField = new javax.swing.JPasswordField();
        registerPanel = new javax.swing.JPanel();
        loginregTextField = new javax.swing.JTextField();
        mailregTextField = new javax.swing.JTextField();
        passregLabel = new javax.swing.JLabel();
        loginregLabel = new javax.swing.JLabel();
        mailregLabel = new javax.swing.JLabel();
        sexregLabel = new javax.swing.JLabel();
        menRadioButton = new javax.swing.JRadioButton();
        womanRadioButton = new javax.swing.JRadioButton();
        registerButton = new javax.swing.JButton();
        passregTextField = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        loginFrameTitleLabel = new javax.swing.JLabel();
        walletFrame = new javax.swing.JPanel();
        walletFrameTitleLabel = new javax.swing.JLabel();
        moneyAmountPanel = new javax.swing.JPanel();
        moneyAmountLabel = new javax.swing.JLabel();
        cashoutPanel = new javax.swing.JPanel();
        cashoutTextField = new javax.swing.JTextField();
        cashoutLabel = new javax.swing.JLabel();
        cashoutButton = new javax.swing.JButton();
        cashoutdataPane = new javax.swing.JScrollPane();
        cashoutdataTextArea = new javax.swing.JTextArea();
        cashoutLabel1 = new javax.swing.JLabel();
        cashinPanel = new javax.swing.JPanel();
        cashinLabel = new javax.swing.JLabel();
        historyPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        historyTextPane = new javax.swing.JTextPane();
        addbeatFrame = new javax.swing.JPanel();
        addbeatFrameTitleLabel = new javax.swing.JLabel();
        addbeatTitleLabel = new javax.swing.JLabel();
        addbeatTitleTextField = new javax.swing.JTextField();
        addbeatDescriptionLabel = new javax.swing.JLabel();
        addbeatDescriptionScrollPane = new javax.swing.JScrollPane();
        addbeatDescriptionTextArea = new javax.swing.JTextArea();
        addbeatAddressTextField = new javax.swing.JTextField();
        addbeatAddressLabel = new javax.swing.JLabel();
        addbeatPriceTextField = new javax.swing.JTextField();
        addbeatPriceLabel = new javax.swing.JLabel();
        addbeatNRadioButton = new javax.swing.JRadioButton();
        addbeatYRadioButton = new javax.swing.JRadioButton();
        addbeatUploadButton = new javax.swing.JButton();
        mysalesFrame = new javax.swing.JPanel();
        mysalesFrameTitleLabel = new javax.swing.JLabel();
        forsalePanel = new javax.swing.JPanel();
        forsaleScrollPane = new javax.swing.JScrollPane();
        forsaleTable = new javax.swing.JTable();
        soldPanel = new javax.swing.JPanel();
        soldScrollPane = new javax.swing.JScrollPane();
        soldTable = new javax.swing.JTable();
        myshoppingFrame = new javax.swing.JPanel();
        myshoppingFrameTitleLabel = new javax.swing.JLabel();
        boughtScrollPane = new javax.swing.JScrollPane();
        boughtTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        aboutFrame = new javax.swing.JPanel();
        aboutFrameTitleLabel = new javax.swing.JLabel();
        aboutLabel = new javax.swing.JLabel();
        connectionStatusLabel = new javax.swing.JLabel();
        loginStatusLabel = new javax.swing.JLabel();
        moneyStatusLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        mainMenu = new javax.swing.JMenuBar();
        programMenu = new javax.swing.JMenu();
        mainframeItem = new javax.swing.JMenuItem();
        Separator2 = new javax.swing.JPopupMenu.Separator();
        connectItem = new javax.swing.JMenuItem();
        disconnectItem = new javax.swing.JMenuItem();
        Separator1 = new javax.swing.JPopupMenu.Separator();
        exitItem = new javax.swing.JMenuItem();
        beatsMenu = new javax.swing.JMenu();
        showbeatsItem = new javax.swing.JMenuItem();
        myshoppingItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        addbeatItem = new javax.swing.JMenuItem();
        mysalesItem = new javax.swing.JMenuItem();
        userMenu = new javax.swing.JMenu();
        loginItem = new javax.swing.JMenuItem();
        logoutItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        walletItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutItem = new javax.swing.JMenuItem();

        setTitle("Beatmaker.pl");
        setMaximumSize(new java.awt.Dimension(640, 480));
        setMinimumSize(new java.awt.Dimension(640, 480));
        setResizable(false);

        framesPanel.setLayout(new java.awt.CardLayout());

        mainFrame.setPreferredSize(new java.awt.Dimension(300, 250));

        ConnectionButton.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        ConnectionButton.setText("Połącz");
        ConnectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectionButtonActionPerformed(evt);
            }
        });

        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/beatmaker/logo.png"))); // NOI18N

        startframeLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        startframeLabel.setText("<html>Witaj w aplikacji pośredniczącej w sprzedaży produkcji muzycznych.<br/><br/> \nJeśli jesteś wokalistą i szukasz podkładów na swoją płytę, świetnie trafiłeś - znajdziesz tutaj<br/> \nmnóstwo świetnych produkcji od najbardziej utalentowanych, polskich producentów.<br/>\nJeśli jesteś producentem i chciałbyś nawiązać współpracę z wokalistami jestes we właściwym<br/>\nmiejscu. Dodaj swoje produkcje do naszej bazy i już dziś zacznij kreować swój wizerunek<br/>\ni zarabiać na tym co naprawdę kochasz.<br/><br/>\nAby rozpocząć pracę z aplikacją musisz połączyć się z serwerem. Możesz tego dokonać<br/>\nprzy pomocy poniższego przycisku, lub wybierając odpowiednią opcję z głównego menu.</html>");
        startframeLabel.setToolTipText("");
        startframeLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout mainFrameLayout = new javax.swing.GroupLayout(mainFrame);
        mainFrame.setLayout(mainFrameLayout);
        mainFrameLayout.setHorizontalGroup(
            mainFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainFrameLayout.createSequentialGroup()
                .addGroup(mainFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainFrameLayout.createSequentialGroup()
                        .addGap(236, 236, 236)
                        .addComponent(ConnectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainFrameLayout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(logoLabel))
                    .addGroup(mainFrameLayout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(startframeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(63, Short.MAX_VALUE))
        );
        mainFrameLayout.setVerticalGroup(
            mainFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainFrameLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(logoLabel)
                .addGap(28, 28, 28)
                .addComponent(startframeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ConnectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        framesPanel.add(mainFrame, "card2");

        beatsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tytuł produkcji", "Producent", "Data dodania", "Cena", "", "id"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Float.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        beatsTable.setFocusable(false);
        beatsTable.setRowHeight(18);
        beatsScrollPanel.setViewportView(beatsTable);
        if (beatsTable.getColumnModel().getColumnCount() > 0) {
            beatsTable.getColumnModel().getColumn(0).setResizable(false);
            beatsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
            beatsTable.getColumnModel().getColumn(1).setResizable(false);
            beatsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            beatsTable.getColumnModel().getColumn(2).setResizable(false);
            beatsTable.getColumnModel().getColumn(3).setResizable(false);
            beatsTable.getColumnModel().getColumn(3).setPreferredWidth(30);
            beatsTable.getColumnModel().getColumn(4).setResizable(false);
            beatsTable.getColumnModel().getColumn(4).setPreferredWidth(40);
            beatsTable.getColumnModel().getColumn(5).setResizable(false);
            beatsTable.getColumnModel().getColumn(5).setPreferredWidth(0);
        }

        beatsFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        beatsFrameTitleLabel.setText("Produkcje na sprzedaż");

        javax.swing.GroupLayout showbeatsFrameLayout = new javax.swing.GroupLayout(showbeatsFrame);
        showbeatsFrame.setLayout(showbeatsFrameLayout);
        showbeatsFrameLayout.setHorizontalGroup(
            showbeatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(showbeatsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(showbeatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(beatsScrollPanel)
                    .addGroup(showbeatsFrameLayout.createSequentialGroup()
                        .addComponent(beatsFrameTitleLabel)
                        .addGap(0, 518, Short.MAX_VALUE)))
                .addContainerGap())
        );
        showbeatsFrameLayout.setVerticalGroup(
            showbeatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(showbeatsFrameLayout.createSequentialGroup()
                .addComponent(beatsFrameTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(beatsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .addContainerGap())
        );

        framesPanel.add(showbeatsFrame, "card3");

        beatFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        beatFrameTitleLabel.setText("Szczegóły produkcji nr");

        beatNumberLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        beatNumberLabel.setText("nr produkcji");

        beatTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        beatTitleLabel.setText("Beat title");

        descriptionPane.setEditable(false);
        descriptionPane.setText("Brak opisu.");
        descriptionPane.setFocusable(false);
        descriptionScrollPane.setViewportView(descriptionPane);

        playButton.setText("Odsłuchaj produkcję");
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });

        beatAuthorLabel.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        beatAuthorLabel.setText("Beat author");

        beatProducentLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        beatProducentLabel.setText("Producent:");

        beatDateLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        beatDateLabel.setText("Data dodania:");

        beatDateLabel1.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        beatDateLabel1.setText("Beat date");

        beatPriceLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        beatPriceLabel.setText("Cena produkcji:");

        beatPriceLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        beatPriceLabel1.setForeground(new java.awt.Color(0, 102, 0));
        beatPriceLabel1.setText("cena");

        beatBuyButton.setText("Kup teraz!");
        beatBuyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                beatBuyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout beatFrameLayout = new javax.swing.GroupLayout(beatFrame);
        beatFrame.setLayout(beatFrameLayout);
        beatFrameLayout.setHorizontalGroup(
            beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(beatFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(beatFrameLayout.createSequentialGroup()
                        .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(beatFrameLayout.createSequentialGroup()
                                .addComponent(beatPriceLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(beatPriceLabel1))
                            .addGroup(beatFrameLayout.createSequentialGroup()
                                .addComponent(beatFrameTitleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(beatNumberLabel))
                            .addGroup(beatFrameLayout.createSequentialGroup()
                                .addComponent(beatProducentLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(beatAuthorLabel)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(descriptionScrollPane)
                    .addGroup(beatFrameLayout.createSequentialGroup()
                        .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(beatTitleLabel)
                            .addGroup(beatFrameLayout.createSequentialGroup()
                                .addComponent(beatDateLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(beatDateLabel1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 275, Short.MAX_VALUE)
                        .addComponent(playButton, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(beatBuyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        beatFrameLayout.setVerticalGroup(
            beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(beatFrameLayout.createSequentialGroup()
                .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(beatFrameTitleLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(beatNumberLabel))
                .addGap(12, 12, 12)
                .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(beatFrameLayout.createSequentialGroup()
                        .addComponent(beatTitleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(beatAuthorLabel)
                            .addComponent(beatProducentLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(beatDateLabel)
                            .addComponent(beatDateLabel1)))
                    .addComponent(playButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(beatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(beatPriceLabel)
                    .addComponent(beatPriceLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(beatBuyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        framesPanel.add(beatFrame, "card4");

        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Logowanie"));

        loginTextField.setNextFocusableComponent(passTextField);
        loginTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginTextFieldActionPerformed(evt);
            }
        });

        loginLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        loginLabel.setText("Nazwa użytkownika:");

        passLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        passLabel.setText("Hasło:");

        loginButton.setText("Zaloguj!");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        passTextField.setNextFocusableComponent(loginButton);

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loginLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(loginTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                    .addComponent(passTextField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(loginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(loginPanelLayout.createSequentialGroup()
                        .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(loginLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(passLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(passTextField)))
                    .addComponent(loginButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        registerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Rejestracja"));

        loginregTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginregTextFieldActionPerformed(evt);
            }
        });

        mailregTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mailregTextFieldActionPerformed(evt);
            }
        });

        passregLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        passregLabel.setText("Hasło:");

        loginregLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        loginregLabel.setText("Nazwa użytkownika:");

        mailregLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        mailregLabel.setText("Adres email:");

        sexregLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        sexregLabel.setText("Płeć:");

        menRadioButton.setSelected(true);
        menRadioButton.setText("mężczyzna");
        menRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menRadioButtonActionPerformed(evt);
            }
        });

        womanRadioButton.setText("kobieta");
        womanRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                womanRadioButtonActionPerformed(evt);
            }
        });

        registerButton.setText("Zarejestruj!");
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerButtonActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("<html>\nNazwa użytkownika powinna składać się wyłącznie<br/>\nz liter, cyfr, myślnika oraz znaku podkreślenia i<br/>\nmieć minimalnie 3, a maksymalnie 15 znaków.<br/>\nHasło musi mieć co najmniej 8 znaków, w tym<br/>\nprzynajmniej 1 dużą literę, 1 małą, 1 cyfrę oraz<br/> \n1 znak specjalny (spośród @#$%^&+=!). </html>");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout registerPanelLayout = new javax.swing.GroupLayout(registerPanel);
        registerPanel.setLayout(registerPanelLayout);
        registerPanelLayout.setHorizontalGroup(
            registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(registerPanelLayout.createSequentialGroup()
                        .addComponent(mailregLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mailregTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(registerPanelLayout.createSequentialGroup()
                        .addComponent(sexregLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(menRadioButton)
                            .addComponent(womanRadioButton)))
                    .addGroup(registerPanelLayout.createSequentialGroup()
                        .addComponent(loginregLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(loginregTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(registerPanelLayout.createSequentialGroup()
                        .addComponent(passregLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(passregTextField)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(registerButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        registerPanelLayout.setVerticalGroup(
            registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(registerPanelLayout.createSequentialGroup()
                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loginregTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(loginregLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(passregLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(passregTextField)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(registerPanelLayout.createSequentialGroup()
                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mailregLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mailregTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(registerPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(womanRadioButton))
                            .addGroup(registerPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(menRadioButton)
                                    .addComponent(sexregLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 8, Short.MAX_VALUE))))
                    .addComponent(registerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        loginFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        loginFrameTitleLabel.setText("Dodaj produkcję");

        javax.swing.GroupLayout loginFrameLayout = new javax.swing.GroupLayout(loginFrame);
        loginFrame.setLayout(loginFrameLayout);
        loginFrameLayout.setHorizontalGroup(
            loginFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(loginFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(registerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loginPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(loginFrameLayout.createSequentialGroup()
                        .addComponent(loginFrameTitleLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        loginFrameLayout.setVerticalGroup(
            loginFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginFrameLayout.createSequentialGroup()
                .addComponent(loginFrameTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(registerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        framesPanel.add(loginFrame, "card5");

        walletFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        walletFrameTitleLabel.setText("Twój portfel");

        moneyAmountPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Stan konta"));

        moneyAmountLabel.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        moneyAmountLabel.setForeground(new java.awt.Color(0, 102, 0));
        moneyAmountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        moneyAmountLabel.setText("0.0 PLN");

        javax.swing.GroupLayout moneyAmountPanelLayout = new javax.swing.GroupLayout(moneyAmountPanel);
        moneyAmountPanel.setLayout(moneyAmountPanelLayout);
        moneyAmountPanelLayout.setHorizontalGroup(
            moneyAmountPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(moneyAmountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        moneyAmountPanelLayout.setVerticalGroup(
            moneyAmountPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(moneyAmountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        cashoutPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Wypłata"));

        cashoutTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cashoutTextFieldActionPerformed(evt);
            }
        });

        cashoutLabel.setText("Kwota wypłaty:");

        cashoutButton.setText("Zleć!");
        cashoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cashoutButtonActionPerformed(evt);
            }
        });

        cashoutdataTextArea.setColumns(20);
        cashoutdataTextArea.setRows(5);
        cashoutdataPane.setViewportView(cashoutdataTextArea);

        cashoutLabel1.setText("<html>Wpisz dane do<br/>wypłaty (numer konta<br/>imię i nazwisko itd.).</html>");
        cashoutLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout cashoutPanelLayout = new javax.swing.GroupLayout(cashoutPanel);
        cashoutPanel.setLayout(cashoutPanelLayout);
        cashoutPanelLayout.setHorizontalGroup(
            cashoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cashoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cashoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cashoutLabel)
                    .addComponent(cashoutLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cashoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cashoutPanelLayout.createSequentialGroup()
                        .addComponent(cashoutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cashoutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(cashoutdataPane))
                .addContainerGap())
        );
        cashoutPanelLayout.setVerticalGroup(
            cashoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cashoutPanelLayout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addGroup(cashoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cashoutdataPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(cashoutLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cashoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cashoutLabel)
                    .addComponent(cashoutButton)
                    .addComponent(cashoutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        cashinPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Wpłata"));

        cashinLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cashinLabel.setText("<html>Aby dokonać jakiegokolwiek zakupu na stronie musisz wpłacić pieniądze na konto w aplikacji Beatmaker.pl. Jest to niezbędny proces, który chroni zarówno kupujących, jak i sprzedającyc.<br/><br/>  Aby dokonać wpłaty dokonaj przelewu ze swojego banku na poniższe dane:<br/> nr konta: 0000 0000 0000 0000 0000 0000<br/> odbiorca: Jan Kowalski<br/> w tytule wpisz swoją nazwe użytkownika oraz email<br/>kwota dowolna <br/><br/> Jak tylko wpłata zostanie zaksięgowana, pieniądze automatycznie pojawią się na Twoim koncie.</html>");
        cashinLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout cashinPanelLayout = new javax.swing.GroupLayout(cashinPanel);
        cashinPanel.setLayout(cashinPanelLayout);
        cashinPanelLayout.setHorizontalGroup(
            cashinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cashinPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cashinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        cashinPanelLayout.setVerticalGroup(
            cashinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cashinPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cashinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        historyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Wydatki / zarobki"));

        historyTextPane.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        historyTextPane.setFocusable(false);
        historyScrollPane.setViewportView(historyTextPane);

        javax.swing.GroupLayout historyPanelLayout = new javax.swing.GroupLayout(historyPanel);
        historyPanel.setLayout(historyPanelLayout);
        historyPanelLayout.setHorizontalGroup(
            historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(historyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(historyScrollPane)
                .addContainerGap())
        );
        historyPanelLayout.setVerticalGroup(
            historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(historyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(historyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout walletFrameLayout = new javax.swing.GroupLayout(walletFrame);
        walletFrame.setLayout(walletFrameLayout);
        walletFrameLayout.setHorizontalGroup(
            walletFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(walletFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(walletFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(walletFrameLayout.createSequentialGroup()
                        .addComponent(walletFrameTitleLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(walletFrameLayout.createSequentialGroup()
                        .addGroup(walletFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(moneyAmountPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(historyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(walletFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cashoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cashinPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        walletFrameLayout.setVerticalGroup(
            walletFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(walletFrameLayout.createSequentialGroup()
                .addComponent(walletFrameTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(walletFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cashoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(moneyAmountPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(walletFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cashinPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(historyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        framesPanel.add(walletFrame, "card7");

        addbeatFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        addbeatFrameTitleLabel.setText("Dodaj produkcję");

        addbeatTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        addbeatTitleLabel.setText("Tytuł produkcji:");

        addbeatTitleTextField.setNextFocusableComponent(passTextField);
        addbeatTitleTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addbeatTitleTextFieldActionPerformed(evt);
            }
        });

        addbeatDescriptionLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        addbeatDescriptionLabel.setText("Opis:");

        addbeatDescriptionTextArea.setColumns(20);
        addbeatDescriptionTextArea.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        addbeatDescriptionTextArea.setRows(5);
        addbeatDescriptionScrollPane.setViewportView(addbeatDescriptionTextArea);

        addbeatAddressTextField.setNextFocusableComponent(passTextField);
        addbeatAddressTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addbeatAddressTextFieldActionPerformed(evt);
            }
        });

        addbeatAddressLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        addbeatAddressLabel.setText("Adres odsłuchu:");

        addbeatPriceTextField.setText("50.00");
        addbeatPriceTextField.setNextFocusableComponent(passTextField);
        addbeatPriceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addbeatPriceTextFieldActionPerformed(evt);
            }
        });

        addbeatPriceLabel.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        addbeatPriceLabel.setText("Cena (w PLN):");

        addbeatNRadioButton.setText("tylko dodaj do moich produkcji");
        addbeatNRadioButton.setEnabled(false);
        addbeatNRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addbeatNRadioButtonActionPerformed(evt);
            }
        });

        addbeatYRadioButton.setSelected(true);
        addbeatYRadioButton.setText("od razu wystaw na sprzedaż");
        addbeatYRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addbeatYRadioButtonActionPerformed(evt);
            }
        });

        addbeatUploadButton.setText("Dodaj");
        addbeatUploadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addbeatUploadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addbeatFrameLayout = new javax.swing.GroupLayout(addbeatFrame);
        addbeatFrame.setLayout(addbeatFrameLayout);
        addbeatFrameLayout.setHorizontalGroup(
            addbeatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addbeatFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addbeatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(addbeatUploadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addbeatFrameTitleLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addbeatFrameLayout.createSequentialGroup()
                        .addComponent(addbeatTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addbeatTitleTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addbeatFrameLayout.createSequentialGroup()
                        .addComponent(addbeatDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addbeatDescriptionScrollPane))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addbeatFrameLayout.createSequentialGroup()
                        .addComponent(addbeatAddressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addbeatAddressTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addbeatFrameLayout.createSequentialGroup()
                        .addComponent(addbeatPriceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addbeatPriceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(addbeatNRadioButton)
                        .addGap(18, 18, 18)
                        .addComponent(addbeatYRadioButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        addbeatFrameLayout.setVerticalGroup(
            addbeatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addbeatFrameLayout.createSequentialGroup()
                .addComponent(addbeatFrameTitleLabel)
                .addGap(23, 23, 23)
                .addGroup(addbeatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addbeatTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addbeatTitleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addbeatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addbeatDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addbeatDescriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addbeatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addbeatAddressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addbeatAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addbeatFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addbeatPriceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addbeatPriceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addbeatNRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(addbeatYRadioButton))
                .addGap(18, 18, 18)
                .addComponent(addbeatUploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        framesPanel.add(addbeatFrame, "card8");

        mysalesFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        mysalesFrameTitleLabel.setText("Moja sprzedaż");

        forsalePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("W sprzedaży"));

        forsaleTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tytuł", "Cena (PLN)", "Data sprzedaży", "", "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Float.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        forsaleTable.setRowHeight(18);
        forsaleScrollPane.setViewportView(forsaleTable);
        if (forsaleTable.getColumnModel().getColumnCount() > 0) {
            forsaleTable.getColumnModel().getColumn(0).setResizable(false);
            forsaleTable.getColumnModel().getColumn(0).setPreferredWidth(160);
            forsaleTable.getColumnModel().getColumn(0).setHeaderValue("Tytuł");
            forsaleTable.getColumnModel().getColumn(1).setResizable(false);
            forsaleTable.getColumnModel().getColumn(1).setPreferredWidth(40);
            forsaleTable.getColumnModel().getColumn(1).setHeaderValue("Cena (PLN)");
            forsaleTable.getColumnModel().getColumn(2).setResizable(false);
            forsaleTable.getColumnModel().getColumn(2).setHeaderValue("Data sprzedaży");
            forsaleTable.getColumnModel().getColumn(3).setResizable(false);
            forsaleTable.getColumnModel().getColumn(3).setPreferredWidth(50);
            forsaleTable.getColumnModel().getColumn(4).setResizable(false);
            forsaleTable.getColumnModel().getColumn(4).setPreferredWidth(40);
            forsaleTable.getColumnModel().getColumn(4).setHeaderValue("");
            forsaleTable.getColumnModel().getColumn(5).setResizable(false);
            forsaleTable.getColumnModel().getColumn(5).setPreferredWidth(5);
            forsaleTable.getColumnModel().getColumn(5).setHeaderValue("");
        }

        javax.swing.GroupLayout forsalePanelLayout = new javax.swing.GroupLayout(forsalePanel);
        forsalePanel.setLayout(forsalePanelLayout);
        forsalePanelLayout.setHorizontalGroup(
            forsalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(forsaleScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );
        forsalePanelLayout.setVerticalGroup(
            forsalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(forsaleScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
        );

        soldPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sprzedane"));

        soldTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tytuł", "Nabywca", "Klucz własności"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        soldTable.setRowHeight(18);
        soldScrollPane.setViewportView(soldTable);
        if (soldTable.getColumnModel().getColumnCount() > 0) {
            soldTable.getColumnModel().getColumn(0).setResizable(false);
            soldTable.getColumnModel().getColumn(0).setPreferredWidth(70);
            soldTable.getColumnModel().getColumn(1).setResizable(false);
            soldTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            soldTable.getColumnModel().getColumn(2).setResizable(false);
        }

        javax.swing.GroupLayout soldPanelLayout = new javax.swing.GroupLayout(soldPanel);
        soldPanel.setLayout(soldPanelLayout);
        soldPanelLayout.setHorizontalGroup(
            soldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(soldScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );
        soldPanelLayout.setVerticalGroup(
            soldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(soldScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mysalesFrameLayout = new javax.swing.GroupLayout(mysalesFrame);
        mysalesFrame.setLayout(mysalesFrameLayout);
        mysalesFrameLayout.setHorizontalGroup(
            mysalesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mysalesFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mysalesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(forsalePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mysalesFrameLayout.createSequentialGroup()
                        .addComponent(mysalesFrameTitleLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(soldPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mysalesFrameLayout.setVerticalGroup(
            mysalesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mysalesFrameLayout.createSequentialGroup()
                .addComponent(mysalesFrameTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(forsalePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(soldPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        framesPanel.add(mysalesFrame, "card9");

        myshoppingFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        myshoppingFrameTitleLabel.setText("Moje zakupy");

        boughtTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tytuł", "Producent", "Klucz własności"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        boughtTable.setRowHeight(18);
        boughtScrollPane.setViewportView(boughtTable);
        if (boughtTable.getColumnModel().getColumnCount() > 0) {
            boughtTable.getColumnModel().getColumn(0).setResizable(false);
            boughtTable.getColumnModel().getColumn(0).setPreferredWidth(70);
            boughtTable.getColumnModel().getColumn(1).setResizable(false);
            boughtTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            boughtTable.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel3.setText("<html>Poniżej znajdują się utwory, które zakupiłeś przy pomocy aplikacji. Po zakupie powinieneś skontaktować się z producentem, aby otrzymać pełną, niezabezpieczną wersję podkładu. Możesz również poprosić o dedykowaną aranżację utworu. W swojej wiadomości powinieneś posłużyć się \"klucz własności\", który producent będzie mógł zweryfikować aby potwierdzić, że to ty jesteś nabywcą jego utworu - jest to dodatkowe zabezpieczenie dla obu stron.</html>");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout myshoppingFrameLayout = new javax.swing.GroupLayout(myshoppingFrame);
        myshoppingFrame.setLayout(myshoppingFrameLayout);
        myshoppingFrameLayout.setHorizontalGroup(
            myshoppingFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, myshoppingFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(myshoppingFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(boughtScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, myshoppingFrameLayout.createSequentialGroup()
                        .addComponent(myshoppingFrameTitleLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        myshoppingFrameLayout.setVerticalGroup(
            myshoppingFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(myshoppingFrameLayout.createSequentialGroup()
                .addComponent(myshoppingFrameTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(boughtScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        framesPanel.add(myshoppingFrame, "card6");

        aboutFrameTitleLabel.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        aboutFrameTitleLabel.setText("O programie");

        aboutLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        aboutLabel.setText("<html>Program został stworzony na zaliczenie przedmiotu<br/> Systemy Baz Danych 2 na Politechnice Poznańskiej.<br/><br/>\nAutor: Sebastian Boruta<br/>\nNr indeksu: 106564<br/>\nKontakt: sebastian@boruta.info</html>");
        aboutLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        aboutLabel.setAutoscrolls(true);

        javax.swing.GroupLayout aboutFrameLayout = new javax.swing.GroupLayout(aboutFrame);
        aboutFrame.setLayout(aboutFrameLayout);
        aboutFrameLayout.setHorizontalGroup(
            aboutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aboutFrameTitleLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutFrameLayout.createSequentialGroup()
                .addContainerGap(70, Short.MAX_VALUE)
                .addComponent(aboutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 561, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54))
        );
        aboutFrameLayout.setVerticalGroup(
            aboutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutFrameLayout.createSequentialGroup()
                .addComponent(aboutFrameTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                .addComponent(aboutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(92, 92, 92))
        );

        framesPanel.add(aboutFrame, "card10");

        connectionStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        connectionStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        connectionStatusLabel.setIcon(new javax.swing.ImageIcon("D:\\Studia\\Semestr 5\\Systemy baz danych 2\\Laboratorium\\Beatmaker\\images\\icons-database-connection.png")); // NOI18N
        connectionStatusLabel.setText("Status połączenia");
        connectionStatusLabel.setToolTipText("");
        connectionStatusLabel.setAlignmentX(0.5F);
        connectionStatusLabel.setMaximumSize(new java.awt.Dimension(200, 16));
        connectionStatusLabel.setMinimumSize(new java.awt.Dimension(200, 16));
        connectionStatusLabel.setPreferredSize(new java.awt.Dimension(200, 16));

        loginStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        loginStatusLabel.setIcon(new javax.swing.ImageIcon("D:\\Studia\\Semestr 5\\Systemy baz danych 2\\Laboratorium\\Beatmaker\\images\\user_icon.png")); // NOI18N
        loginStatusLabel.setText("Status zalogowania");

        moneyStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        moneyStatusLabel.setIcon(new javax.swing.ImageIcon("D:\\Studia\\Semestr 5\\Systemy baz danych 2\\Laboratorium\\Beatmaker\\images\\new_order.png")); // NOI18N
        moneyStatusLabel.setText("Stan konta");

        jLabel1.setText("Copyright © 2016 by Boruta / Beatmaker.pl");

        refreshButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        refreshButton.setText("Odśwież");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        programMenu.setText("Aplikacja");

        mainframeItem.setText("Strona główna");
        mainframeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainframeItemActionPerformed(evt);
            }
        });
        programMenu.add(mainframeItem);
        programMenu.add(Separator2);

        connectItem.setText("Połącz");
        connectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectItemActionPerformed(evt);
            }
        });
        programMenu.add(connectItem);

        disconnectItem.setText("Rozłącz");
        disconnectItem.setEnabled(false);
        disconnectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectItemActionPerformed(evt);
            }
        });
        programMenu.add(disconnectItem);
        programMenu.add(Separator1);

        exitItem.setText("Wyjście");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        programMenu.add(exitItem);

        mainMenu.add(programMenu);

        beatsMenu.setText("Bity");

        showbeatsItem.setText("Bity do kupienia");
        showbeatsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showbeatsItemActionPerformed(evt);
            }
        });
        beatsMenu.add(showbeatsItem);

        myshoppingItem.setText("Moje zakupy");
        myshoppingItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myshoppingItemActionPerformed(evt);
            }
        });
        beatsMenu.add(myshoppingItem);
        beatsMenu.add(jSeparator3);

        addbeatItem.setText("Dodaj produkcję");
        addbeatItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addbeatItemActionPerformed(evt);
            }
        });
        beatsMenu.add(addbeatItem);

        mysalesItem.setText("Moja sprzedaż");
        mysalesItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mysalesItemActionPerformed(evt);
            }
        });
        beatsMenu.add(mysalesItem);

        mainMenu.add(beatsMenu);

        userMenu.setText("Użytkownik");

        loginItem.setText("Logowanie / rejestracja");
        loginItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginItemActionPerformed(evt);
            }
        });
        userMenu.add(loginItem);

        logoutItem.setText("Wyloguj się");
        logoutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutItemActionPerformed(evt);
            }
        });
        userMenu.add(logoutItem);
        userMenu.add(jSeparator2);

        walletItem.setText("Mój portfel");
        walletItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                walletItemActionPerformed(evt);
            }
        });
        userMenu.add(walletItem);

        mainMenu.add(userMenu);

        helpMenu.setText("Pomoc");

        aboutItem.setText("O programie");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutItem);

        mainMenu.add(helpMenu);

        setJMenuBar(mainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addComponent(framesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(211, 211, 211)
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(connectionStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loginStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(moneyStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(refreshButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectionStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loginStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moneyStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(framesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConnectionButtonActionPerformed
        if (!checkConnection()) {
            connect();
        } else {
            disconnect();
        }
    }//GEN-LAST:event_ConnectionButtonActionPerformed

    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        close();
    }//GEN-LAST:event_exitItemActionPerformed

    private void connectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectItemActionPerformed
        connect();
    }//GEN-LAST:event_connectItemActionPerformed

    private void disconnectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectItemActionPerformed
        disconnect();
    }//GEN-LAST:event_disconnectItemActionPerformed

    private void showbeatsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showbeatsItemActionPerformed
        System.out.println("Użytkownik zmienia ramkę na \"Pokaż bity\".");
        changeFrame(showbeatsFrame);
        DefaultTableModel beats = (DefaultTableModel) beatsTable.getModel();
        beats.setRowCount(0);
        try {
            ResultSet rs = db.query("SELECT * FROM beatsforsale");
            while (rs.next()) {
                Object[] row = {rs.getString(2), rs.getString(3), rs.getString(4), rs.getFloat(5), "Szczegóły", rs.getInt(1)};
                beats.addRow(row);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Action showbeat = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int id = (int) beatsTable.getModel().getValueAt(Integer.valueOf(e.getActionCommand()), 5);
                    System.out.println("Użytkownik wybrał produkcję (id " + id + ").");
                    beat(id);
                }
            };
            ButtonColumn buttonColumn = new ButtonColumn(beatsTable, showbeat, 4);
            //buttonColumn.setMnemonic(KeyEvent.VK_D);
        }
    }//GEN-LAST:event_showbeatsItemActionPerformed

    private void mainframeItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainframeItemActionPerformed
        System.out.println("Użytkownik zmienia ramkę na \"Strona główna\".");
        changeFrame(mainFrame);
    }//GEN-LAST:event_mainframeItemActionPerformed

    private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
        URL adres = null;
        try {
            adres = new URL(play);
        } catch (MalformedURLException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        openWebpage(adres);
    }//GEN-LAST:event_playButtonActionPerformed

    private void beatBuyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_beatBuyButtonActionPerformed
        Object[] options = {"Tak", "Nie"};
        try {
            ResultSet rs = db.query("Select price, seller_id, status FROM beats where id = " + beatNumberLabel.getText() + "");
            rs.next();
            if (rs.getFloat(1) > money(logged)) {
                alert.showMessageDialog(null, "Masz za mało środków na koncie, aby dokonać zakupu.", "Dokonaj wpłaty", alert.ERROR_MESSAGE);
                beatBuyButton.setEnabled(false);
            } else if (rs.getInt(3) != 1) {
                alert.showMessageDialog(null, "Produkcja nie znajduje się obecnie w sprzedaży.", "Błąd zakupu", alert.ERROR_MESSAGE);
                beatBuyButton.setEnabled(false);
            } else {
                int odp = alert.showOptionDialog(null, "Jesteś pewien, że chcesz kupić \"" + beatTitleLabel.getText() + "\" od użytkownika " + beatAuthorLabel.getText() + " za " + beatPriceLabel1.getText() + "?", "Zakup produkcji", alert.YES_NO_CANCEL_OPTION, alert.QUESTION_MESSAGE, null, options, options[1]);
                if (odp == 0) {
                    System.out.println("Użytkownik kupił bit o id " + beatNumberLabel.getText());
                    date = new Date();
                    String product_key = "brak";
                    try {
                        product_key = hash.generateMD5("product" + logged + beatTitleLabel.getText() + beatAuthorLabel.getText() + "key");
                    } catch (Hash.HashGenerationException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (db.update("UPDATE beats SET buyer_id = " + logged + ", status = 2, transaction_date='" + dateFormat.format(date) + "', product_key='" + product_key + "' WHERE id = " + beatNumberLabel.getText()) == 1
                            && db.update("UPDATE users SET money = money + " + rs.getFloat(1) + " WHERE id = " + rs.getInt(2)) == 1
                            && db.update("UPDATE users SET money = money - " + rs.getFloat(1) + " WHERE id = " + logged) == 1) {
                        System.out.println("Transakcja przebiegła pomyślnie.");
                        moneyStatusLabel.setText("" + money(logged) + " PLN");
                        myshopping();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_beatBuyButtonActionPerformed

    private void loginTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_loginTextFieldActionPerformed

    private void loginregTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginregTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_loginregTextFieldActionPerformed

    private void mailregTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mailregTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mailregTextFieldActionPerformed

    private void loginItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginItemActionPerformed
        System.out.println("Użytkownik zmienia ramkę na \"Logowanie / rejestracja\".");
        changeFrame(loginFrame);
    }//GEN-LAST:event_loginItemActionPerformed

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        System.out.println("Użytkownik próbuje się zalogować.");
        if (!valid.login(loginTextField.getText())) {
            alert.showMessageDialog(null, "Nieprawidłowy format nazwy użytkownika.", "Komunikat", alert.WARNING_MESSAGE);
        } else if (!valid.password(String.valueOf(passTextField.getPassword()))) {
            alert.showMessageDialog(null, "Niedozwolony format hasła.", "Komunikat", alert.WARNING_MESSAGE);
        } else {
            try {
                ResultSet rs = db.query("SELECT id, login FROM users WHERE LOWER(login) = '" + loginTextField.getText().toLowerCase() + "' AND password = '" + String.valueOf(passTextField.getPassword()) + "'");

                if (rs.next()) {
                    logged = rs.getInt(1);
                    System.out.println("Użytkownik zalogował sie jako " + rs.getString(2) + ".");
                    login = rs.getString(2);
                    checkLogin();
                    changeFrame(walletFrame);
                    //alert.showMessageDialog(null, "Witaj " + rs.getString(2) + ". Autoryzacja zakończona powodzeniem.", "Zalogowano", alert.PLAIN_MESSAGE);
                } else {
                    System.out.println("Użytkownik wprowadził niewłaściwe dane do logowania.");
                    alert.showMessageDialog(null, "Nieprawidłowy login lub hasło!", "Błąd logowania", alert.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_loginButtonActionPerformed

    private void logoutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutItemActionPerformed
        System.out.println("Użytkownik wylogowuje się.");
        logged = 0;
        login = "";
        checkLogin();
        changeFrame(loginFrame);
        //alert.showMessageDialog(null, "Wylogowano. Dziękujemy za skorzystanie z Beatmaker.pl.", "Wylogowano!", alert.PLAIN_MESSAGE);
    }//GEN-LAST:event_logoutItemActionPerformed

    private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerButtonActionPerformed
        System.out.println("Użytkownik próbuje się zarejestrować.");
        if (!valid.login(loginregTextField.getText())) {
            alert.showMessageDialog(null, "Wpisz prawidłową nazwę użytkownika.", "Komunikat", alert.WARNING_MESSAGE);
        } else if (!valid.password(String.valueOf(passregTextField.getPassword()))) {
            alert.showMessageDialog(null, "Niedozwolony format hasła.", "Komunikat", alert.WARNING_MESSAGE);
        } else if (!valid.mail(mailregTextField.getText())) {
            alert.showMessageDialog(null, "Wpisz prawidłowy adres email.", "Komunikat", alert.WARNING_MESSAGE);
        } else {
            try {
                ResultSet rs = db.query("SELECT login FROM users WHERE LOWER(login) = '" + loginregTextField.getText().toLowerCase() + "' OR LOWER(mail) = '" + mailregTextField.getText().toLowerCase() + "'");

                if (!rs.next()) {
                    int sex = 0;
                    if (womanRadioButton.isSelected()) {
                        sex = 1;
                    }
                    if (db.update("INSERT INTO users(login, password, mail, sex) VALUES('" + loginregTextField.getText() + "', '" + String.valueOf(passregTextField.getPassword()) + "', '" + mailregTextField.getText() + "', " + sex + ")") >= 1) {
                        rs = db.query("SELECT id, login FROM users WHERE LOWER(login) = '" + loginregTextField.getText().toLowerCase() + "' AND password = '" + String.valueOf(passregTextField.getPassword()) + "'");
                        if (rs.next()) {
                            logged = rs.getInt(1);
                            checkLogin();
                            changeFrame(walletFrame);
                            System.out.println("Użytkownik dokonał rejestracji jako " + rs.getString(2) + ".");
                            login = rs.getString(2);
                            alert.showMessageDialog(null, "Rejstracja przebiegła pomyslnie. Jesteś automatycznie zalogowany.", "Zarejestrowano", alert.PLAIN_MESSAGE);
                        }
                    } else {
                        System.out.println("Niespodziewany błąd podczas rejestracji.");
                        alert.showMessageDialog(null, "Wystąpił niespodziewany błąd podczas rejestracji, spróbuj ponownie za chwilę.", "Błąd rejestracji", alert.ERROR_MESSAGE);
                    }
                } else {
                    System.out.println("Wprowadzony podczas rejestracji login lub email już istnieje w bazie.");
                    alert.showMessageDialog(null, "Login lub email już istnieje w bazie.", "Błąd rejestracji", alert.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                Logger.getLogger(Beatmaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_registerButtonActionPerformed

    private void menRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menRadioButtonActionPerformed
        womanRadioButton.setSelected(false);
    }//GEN-LAST:event_menRadioButtonActionPerformed

    private void womanRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_womanRadioButtonActionPerformed
        menRadioButton.setSelected(false);
    }//GEN-LAST:event_womanRadioButtonActionPerformed

    private void cashoutTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cashoutTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cashoutTextFieldActionPerformed

    private void walletItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_walletItemActionPerformed
        System.out.println("Użytkownik zmienia ramkę na \"Mój portfel\".");
        changeFrame(walletFrame);
    }//GEN-LAST:event_walletItemActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        moneyStatusLabel.setText("" + money(logged) + " PLN");
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void addbeatTitleTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addbeatTitleTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addbeatTitleTextFieldActionPerformed

    private void addbeatAddressTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addbeatAddressTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addbeatAddressTextFieldActionPerformed

    private void addbeatPriceTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addbeatPriceTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addbeatPriceTextFieldActionPerformed

    private void addbeatItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addbeatItemActionPerformed
        changeFrame(addbeatFrame);
    }//GEN-LAST:event_addbeatItemActionPerformed

    private void addbeatYRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addbeatYRadioButtonActionPerformed
        addbeatNRadioButton.setSelected(false);
    }//GEN-LAST:event_addbeatYRadioButtonActionPerformed

    private void addbeatNRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addbeatNRadioButtonActionPerformed
        addbeatYRadioButton.setSelected(false);
    }//GEN-LAST:event_addbeatNRadioButtonActionPerformed

    private void addbeatUploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addbeatUploadButtonActionPerformed
        System.out.println("Użytkownik próbuje dodać produkcję.");
        float price = 0;
        if (!valid.title(addbeatTitleTextField.getText())) {
            alert.showMessageDialog(null, "Tytuł nieprawidłowy - wymagane 3 do 32 znaków. Można używac małych i dużych liter (z polskimi znakami), cyfr, spacji oraz: _-@#$%^&+=!*:?().", "Komunikat", alert.WARNING_MESSAGE);
        } else if (!valid.description(addbeatDescriptionTextArea.getText())) {
            alert.showMessageDialog(null, "Opis nieprawidłowy. Maksymalnie 256 znaków. Można używac małych i dużych liter (z polskimi znakami), cyfr, spacji oraz: _-@#$%^&+=!*:?().", "Komunikat", alert.WARNING_MESSAGE);
        } else if (!valid.address(addbeatAddressTextField.getText())) {
            alert.showMessageDialog(null, "Wprowadzony adres nie jest prawidłowy! Podaj adres do tego konkretnego utworu który sprzedajesz - np. na YouTube.", "Komunikat", alert.WARNING_MESSAGE);
        } else if (!valid.money(addbeatPriceTextField.getText())) {
            alert.showMessageDialog(null, "Wpisz poprawną cenę - nie używaj przecinka - zamiast tego użyć kropki.", "Komunikat", alert.WARNING_MESSAGE);
        } else {
            price = Float.parseFloat(addbeatPriceTextField.getText());
            if (price <= 0) {
                alert.showMessageDialog(null, "Cena produkcji nie może być równa 0.00!", "Komunikat", alert.WARNING_MESSAGE);
            } else {
                date = new Date();
                int status = 0;
                if (addbeatYRadioButton.isSelected()) {
                    status = 1;
                }
                if (db.update("INSERT INTO beats(seller_id, title, description, address, upload_date, price, status) VALUES('" + logged + "', '" + addbeatTitleTextField.getText() + "', '" + addbeatDescriptionTextArea.getText() + "', '" + addbeatAddressTextField.getText() + "', '" + dateFormat.format(date) + "', " + addbeatPriceTextField.getText() + ", " + status + ")") >= 1) {
                    mysales();
                    alert.showMessageDialog(null, "Twoja produkcja została dodana.", "Sukces", alert.PLAIN_MESSAGE);
                } else {
                    alert.showMessageDialog(null, "Wystąpił błąd! Najprawdopodobniej produkcja o identycznym adresie istnieje już w bazie.", "Komunikat", alert.ERROR_MESSAGE);
                }
            }
        }

    }//GEN-LAST:event_addbeatUploadButtonActionPerformed

    private void myshoppingItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myshoppingItemActionPerformed
        myshopping();
    }//GEN-LAST:event_myshoppingItemActionPerformed

    private void mysalesItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mysalesItemActionPerformed
        mysales();
    }//GEN-LAST:event_mysalesItemActionPerformed

    private void cashoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cashoutButtonActionPerformed
        System.out.println("Użytkownik postanowił dokonać wypłaty.");
        if (!valid.money(cashoutTextField.getText())) {
            alert.showMessageDialog(null, "Kwota nieprawidłowa, wpisz poprawną sumę!", "Komunikat", alert.WARNING_MESSAGE);
        } else if (!valid.bankdata(cashoutdataTextArea.getText())) {
            alert.showMessageDialog(null, "Wpisz poprawne dane tj. numer konta oraz imię i nazwisko odbiorcy przelewu.", "Komunikat", alert.WARNING_MESSAGE);
        } else {
            try {
                ResultSet rs = db.query("SELECT money FROM users WHERE id = " + logged);
                rs.next();
                float money = Float.parseFloat(cashoutTextField.getText());
                if (money > rs.getFloat(1)) {
                    alert.showMessageDialog(null, "Na na twoim koncie znajduje się " + rs.getFloat(1) + " PLN, zatem nie możesz wypłacić " + cashoutTextField.getText() + " PLN.", "Komunikat", alert.WARNING_MESSAGE);
                } else {
                    date = new Date();
                    if (db.update("UPDATE users SET money = money - " + cashoutTextField.getText() + " WHERE id = " + logged) == 1) {
                        if (db.update("INSERT INTO cashouts(cashout_date, user_id, user_data, money) VALUES('" + dateFormat.format(date) + "'," + logged + ", '" + cashoutdataTextArea.getText() + "', " + cashoutTextField.getText() + ")") >= 1) {
                            moneyStatusLabel.setText("" + (rs.getFloat(1) - money) + " PLN");
                            changeFrame(walletFrame);
                            alert.showMessageDialog(null, "Dokonałeś wypłaty " + money + " PLN. Pienidze pojawią się na twoim koncie maksymalnie w ciągu 5 dni roboczych.", "Komunikat", alert.PLAIN_MESSAGE);
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_cashoutButtonActionPerformed

    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
        changeFrame(aboutFrame);
    }//GEN-LAST:event_aboutItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ConnectionButton;
    private javax.swing.JPopupMenu.Separator Separator1;
    private javax.swing.JPopupMenu.Separator Separator2;
    private javax.swing.JPanel aboutFrame;
    private javax.swing.JLabel aboutFrameTitleLabel;
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JLabel aboutLabel;
    private javax.swing.JLabel addbeatAddressLabel;
    private javax.swing.JTextField addbeatAddressTextField;
    private javax.swing.JLabel addbeatDescriptionLabel;
    private javax.swing.JScrollPane addbeatDescriptionScrollPane;
    private javax.swing.JTextArea addbeatDescriptionTextArea;
    private javax.swing.JPanel addbeatFrame;
    private javax.swing.JLabel addbeatFrameTitleLabel;
    private javax.swing.JMenuItem addbeatItem;
    private javax.swing.JRadioButton addbeatNRadioButton;
    private javax.swing.JLabel addbeatPriceLabel;
    private javax.swing.JTextField addbeatPriceTextField;
    private javax.swing.JLabel addbeatTitleLabel;
    private javax.swing.JTextField addbeatTitleTextField;
    private javax.swing.JButton addbeatUploadButton;
    private javax.swing.JRadioButton addbeatYRadioButton;
    private javax.swing.JOptionPane alert;
    private javax.swing.JLabel beatAuthorLabel;
    private javax.swing.JButton beatBuyButton;
    private javax.swing.JLabel beatDateLabel;
    private javax.swing.JLabel beatDateLabel1;
    private javax.swing.JPanel beatFrame;
    private javax.swing.JLabel beatFrameTitleLabel;
    private javax.swing.JLabel beatNumberLabel;
    private javax.swing.JLabel beatPriceLabel;
    private javax.swing.JLabel beatPriceLabel1;
    private javax.swing.JLabel beatProducentLabel;
    private javax.swing.JLabel beatTitleLabel;
    private javax.swing.JLabel beatsFrameTitleLabel;
    private javax.swing.JMenu beatsMenu;
    private javax.swing.JScrollPane beatsScrollPanel;
    private javax.swing.JTable beatsTable;
    private javax.swing.JScrollPane boughtScrollPane;
    private javax.swing.JTable boughtTable;
    private javax.swing.JLabel cashinLabel;
    private javax.swing.JPanel cashinPanel;
    private javax.swing.JButton cashoutButton;
    private javax.swing.JLabel cashoutLabel;
    private javax.swing.JLabel cashoutLabel1;
    private javax.swing.JPanel cashoutPanel;
    private javax.swing.JTextField cashoutTextField;
    private javax.swing.JScrollPane cashoutdataPane;
    private javax.swing.JTextArea cashoutdataTextArea;
    private javax.swing.JMenuItem connectItem;
    private javax.swing.JLabel connectionStatusLabel;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JMenuItem disconnectItem;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JPanel forsalePanel;
    private javax.swing.JScrollPane forsaleScrollPane;
    private javax.swing.JTable forsaleTable;
    private javax.swing.JPanel framesPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JTextPane historyTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JButton loginButton;
    private javax.swing.JPanel loginFrame;
    private javax.swing.JLabel loginFrameTitleLabel;
    private javax.swing.JMenuItem loginItem;
    private javax.swing.JLabel loginLabel;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JLabel loginStatusLabel;
    private javax.swing.JTextField loginTextField;
    private javax.swing.JLabel loginregLabel;
    private javax.swing.JTextField loginregTextField;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JMenuItem logoutItem;
    private javax.swing.JLabel mailregLabel;
    private javax.swing.JTextField mailregTextField;
    private javax.swing.JPanel mainFrame;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JMenuItem mainframeItem;
    private javax.swing.JRadioButton menRadioButton;
    private javax.swing.JLabel moneyAmountLabel;
    private javax.swing.JPanel moneyAmountPanel;
    private javax.swing.JLabel moneyStatusLabel;
    private javax.swing.JPanel mysalesFrame;
    private javax.swing.JLabel mysalesFrameTitleLabel;
    private javax.swing.JMenuItem mysalesItem;
    private javax.swing.JPanel myshoppingFrame;
    private javax.swing.JLabel myshoppingFrameTitleLabel;
    private javax.swing.JMenuItem myshoppingItem;
    private javax.swing.JLabel passLabel;
    private javax.swing.JPasswordField passTextField;
    private javax.swing.JLabel passregLabel;
    private javax.swing.JPasswordField passregTextField;
    private javax.swing.JButton playButton;
    private javax.swing.JMenu programMenu;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton registerButton;
    private javax.swing.JPanel registerPanel;
    private javax.swing.JLabel sexregLabel;
    private javax.swing.JPanel showbeatsFrame;
    private javax.swing.JMenuItem showbeatsItem;
    private javax.swing.JPanel soldPanel;
    private javax.swing.JScrollPane soldScrollPane;
    private javax.swing.JTable soldTable;
    private javax.swing.JLabel startframeLabel;
    private javax.swing.JMenu userMenu;
    private javax.swing.JPanel walletFrame;
    private javax.swing.JLabel walletFrameTitleLabel;
    private javax.swing.JMenuItem walletItem;
    private javax.swing.JRadioButton womanRadioButton;
    // End of variables declaration//GEN-END:variables
}

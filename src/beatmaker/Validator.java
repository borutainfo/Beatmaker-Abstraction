package beatmaker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    private Pattern pattern;
    private Matcher matcher;

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,15}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,32}$";
    private static final String MAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String TITLE_PATTERN = "^[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ0-9 .,()@#$%^&+=!*:;/?_-]{3,32}$";
    private static final String DESCRIPTION_PATTERN = "^[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ0-9 \\r\\n.,()@#$%^&+=!*:;/?_-]{0,512}$";
    private static final String ADDRESS_PATTERN = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final String MONEY_PATTERN = "(^(0|([1-9][0-9]*))(\\.[0-9]{1,2})?$)|(^(0{0,1}|([1-9][0-9]*))(\\.[0-9]{1,2})?$)";
    private static final String BANKDATA_PATTERN = "^[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ0-9 \\r\\n.()@#$%^&+=!*:;?_-]{16,512}$";
    private boolean check(final String expression, final String regexp) {
        pattern = Pattern.compile(regexp);
        matcher = pattern.matcher(expression);
        return matcher.matches();
    }
    
    public boolean login(final String username) {
        return check(username, USERNAME_PATTERN);
    }
    
    public boolean password(final String password) {
        return check(password, PASSWORD_PATTERN);
    }
    
    public boolean mail(final String mail) {
        return check(mail, MAIL_PATTERN);
    }
    
    public boolean title(final String title) {
        return check(title, TITLE_PATTERN);
    }
    
    public boolean description(final String description) {
        return check(description, DESCRIPTION_PATTERN);
    }
    
    public boolean address(final String address) {
        return check(address, ADDRESS_PATTERN);
    }
    
    public boolean money(final String money) {
        return check(money, MONEY_PATTERN);
    }
    
    public boolean bankdata(final String bankdata) {
        return check(bankdata, BANKDATA_PATTERN);
    }
}

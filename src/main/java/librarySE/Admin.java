package librarySE;

public class Admin {
	private static Admin instance;
	
    private String username;
    private String password;
    private boolean loggedIn;

    private Admin(String username, String password) {
        this.username = username;
        this.password = password;
        this.loggedIn = false; 
    }

    public boolean login(String enteredUser, String enteredPass) {
        if (username.equals(enteredUser) && password.equals(enteredPass)) {
            return (loggedIn = true);
        } else {
            loggedIn = false;
            return (loggedIn = false);
        }
    }
    public static Admin getInstance(String username, String password) {
        if (instance == null) {
            instance = new Admin(username, password); 
        }
        return instance;
    }

    public void logout() {
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }


    public String getUsername() {
        return username;
    }

}


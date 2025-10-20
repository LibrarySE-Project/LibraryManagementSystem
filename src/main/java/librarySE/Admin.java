package librarySE;

public class Admin extends User{
	private static Admin instance;
    private boolean loggedIn;

    private Admin(String username, String password) {
    	super(username, Role.ADMIN,password);
        this.loggedIn = false; 
    }

    public boolean login(String enteredUser, String enteredPass) {
        if (getUsername().equals(enteredUser) && password.equals(enteredPass)) {
            loggedIn = true;
        } else {
            loggedIn = false;
        }
        return loggedIn;
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

}


package librarySE;


public class LoginManager {

        private Admin admin;

	    public LoginManager(Admin admin) {
	        this.admin = admin;
	    }

	    public boolean login(String user, String userPass) {
	        return admin.login(user, userPass);
	    }

	    public void logout() {
	        admin.logout();
	    }
	    public boolean isLoggedIn() {
	        return admin.isLoggedIn();
	    }

}

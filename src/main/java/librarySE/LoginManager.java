package librarySE;


public class LoginManager {

        private Admin admin;

	    public LoginManager(Admin admin) {
	        this.admin = admin;
	    }

	    public boolean login(String user, String pass) {
	        return admin.login(user, pass);
	    }

	    public void logout() {
	        admin.logout();
	    }
	

	public static void main(String[] args) {
		

	}

}

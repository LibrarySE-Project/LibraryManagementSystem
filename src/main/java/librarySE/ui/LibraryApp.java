package librarySE.ui;

import javax.swing.SwingUtilities;

import librarySE.managers.LoginManager;
import librarySE.repo.FileUserRepository;
import librarySE.repo.UserRepository;

/**
 * Entry point for the Swing-based Library Management application.
 * <p>
 * This class is responsible for bootstrapping the desktop application by:
 * <ul>
 *   <li>Loading all registered users from persistent storage via {@link UserRepository}</li>
 *   <li>Creating a {@link LoginManager} to handle authentication logic</li>
 *   <li>Launching the Swing-based {@link LoginFrame} on the Event Dispatch Thread (EDT)</li>
 * </ul>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Initialize infrastructure components needed for login (user repository + login manager)</li>
 *   <li>Ensure the UI is started in a thread-safe way using {@link SwingUtilities#invokeLater(Runnable)}</li>
 *   <li>Serve as the main entry point when running the JAR/application</li>
 * </ul>
 *
 * <h2>High-Level Flow:</h2>
 * <ol>
 *   <li>Construct a {@link FileUserRepository} to load users from <b>library_data/users.json</b>.</li>
 *   <li>Pass this repository into {@link LoginManager} so it can validate credentials.</li>
 *   <li>Create and display the {@link LoginFrame}, which provides:
 *       <ul>
 *           <li>Username and password fields</li>
 *           <li>Show/Hide password toggle</li>
 *           <li>Integration with {@link LoginManager} for real authentication</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // From the command line:
 * //   java -cp target/librarySE-1.0-SNAPSHOT.jar librarySE.ui.LibraryApp
 *
 * public class Launcher {
 *     public static void main(String[] args) {
 *         LibraryApp.main(args);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * This class is intentionally small and focused; all business logic is delegated
 * to managers and UI classes in other packages.
 * </p>
 *
 * @author Eman
 * @see LoginFrame
 * @see LoginManager
 * @see UserRepository
 * @see FileUserRepository
 */
public class LibraryApp {

    /**
     * Main entry point for the Library application.
     * <p>
     * The method performs the following steps:
     * <ol>
     *   <li>Creates a {@link FileUserRepository} to access persisted users.</li>
     *   <li>Constructs a {@link LoginManager} using that repository.</li>
     *   <li>Schedules creation and display of the {@link LoginFrame} on the EDT
     *       using {@link SwingUtilities#invokeLater(Runnable)}.</li>
     * </ol>
     * </p>
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {

        // Load all users from JSON-based repository
        UserRepository userRepo = new FileUserRepository();

        // Create LoginManager with the repository to handle authentication
        LoginManager loginManager = new LoginManager(userRepo);

        // Show Swing Login UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame(loginManager);
            frame.setVisible(true);
        });
    }
}

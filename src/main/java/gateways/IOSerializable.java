package gateways;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;
import java.util.logging.*;
import java.util.*;

import entities.Event;
import entities.User;

/**
 * This class will allow (de)serialization of files.
 * It reads from dropbox and saves the data in real time.
 * Debugging must be done by Sebin since all data is serialized in his personal repository that no one can access.
 * The entities being imported are not for violating clean architecture; rather they only exist for type casting.
 *
 * @author Sebin Im
 */
public class IOSerializable {
    // Logging
    private static final Logger logger = Logger.getLogger(IOSerializable.class.getPackage().getName());

    // Filepaths for each data being serialized
    private static final String EVENTS_FILEPATH = "events.ser";
    private static final String USERS_FILEPATH = "users.ser";

    private static final String RECURSIVE_EVENTS_FILEPATH = "recursive_events.ser";


    // A security token needed to access the Dropbox application
    private static final String ACCESS_TOKEN = "EfBUX9G7zxkAAAAAAAAAAaXr-kGtiOL1cwBhwIe7BcI0hvt-uH5LBsEh4FXJ31Ry";

    // A public Dropbox link where the serialized files are stored
    private static final String eventsURL = "https://www.dropbox.com/s/vkant0coohn5fil/events.ser?dl=1";
    private static final String usersURL = "https://www.dropbox.com/s/f6yan8l9e951b7x/users.ser?dl=1";

    private static final String recursiveEventsURL = ""; //TODO: sebin please add link.

    /**
     * Initialize an instance of IOSerializable.
     *
     * @param intro boolean value of the process being in introduction or conclusion
     */
    public IOSerializable(Boolean intro) {
        readFromDropbox(intro);
    }

    /**
     * Download from public Dropbox repository these two files, and save them in the directory temporarily.
     *
     * @param intro boolean value of the process being in introduction or conclusion
     */
    public void readFromDropbox(Boolean intro) {
        try {
            URL eventsDownload = new URL(eventsURL);
            URL usersDownload = new URL(usersURL);

            URL recursiveEventsDownload = new URL(recursiveEventsURL);
            ReadableByteChannel eventsReadableByteChannel = Channels.newChannel(eventsDownload.openStream());
            ReadableByteChannel usersReadableByteChannel = Channels.newChannel(usersDownload.openStream());

            ReadableByteChannel recursiveEventsReadableByteChannel = Channels.newChannel(recursiveEventsDownload.openStream());

            ArrayList<FileOutputStream> arrayList = introOrEnd(intro);
            FileOutputStream eventsFileOutputStream = arrayList.get(0);
            FileOutputStream usersFileOutputStream = arrayList.get(1);

            FileOutputStream recursiveEventsFileOutputStream = arrayList.get(2);

            eventsFileOutputStream.getChannel().transferFrom(eventsReadableByteChannel, 0, 1 << 24);
            usersFileOutputStream.getChannel().transferFrom(usersReadableByteChannel, 0, 1 << 24);

            recursiveEventsFileOutputStream.getChannel().transferFrom(recursiveEventsReadableByteChannel, 0, 1 << 24);

            eventsFileOutputStream.close();
            usersFileOutputStream.close();

            recursiveEventsFileOutputStream.close();

            eventsReadableByteChannel.close();
            usersReadableByteChannel.close();

            recursiveEventsReadableByteChannel.close();

        } catch (IOException eIO) {
            eIO.printStackTrace();
        }
    }

    /**
     * A helper method that checks if this file retrieval process is for the beginning or for the end of the program.
     *
     * @param intro boolean value of the process being in introduction or conclusion
     * @return A stream of FileOutput that corresponds to which filename it should have
     */
    public ArrayList<FileOutputStream> introOrEnd(Boolean intro) {
        try {
            if (intro) {
                return new ArrayList<>(Arrays.asList(new FileOutputStream(EVENTS_FILEPATH),
                        new FileOutputStream(USERS_FILEPATH), new FileOutputStream(RECURSIVE_EVENTS_FILEPATH)));
            } else {
                return new ArrayList<>(Arrays.asList(new FileOutputStream("events1.ser"),
                        new FileOutputStream("users1.ser"), new FileOutputStream("recursive_events1.ser")));
            }
        } catch (IOException eIO) {
            eIO.printStackTrace();
        }
        return null;
    }

    /**
     * A method that saves to the dropbox repository.
     * It first creates a client of Sebin Im, which uses the access token to verify itself.
     * Then creates an instance of builder that uploads the files to the repository.
     */
    public void saveToDropbox() {
        // Create Dropbox Client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/Sebin").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        // Check current account info is "Sebin Im"
        FullAccount account;
        try {
            account = client.users().getCurrentAccount();
            assert account.getName().getDisplayName().equalsIgnoreCase("Sebin Im");
        } catch (DbxException eDBX) {
            logger.log(Level.SEVERE, "Dropbox raised an exception.", eDBX);
        }

        // Delete files in the directory to avoid file duplication error and upload serializable files to Dropbox
        try {
//          Commenting out delete for now, since these lines are valuable.
//            DeleteResult delEvents1 = client.files().deleteV2("/" + EVENTS_FILEPATH);
//            DeleteResult delUsers1 = client.files().deleteV2("/" + USERS_FILEPATH);
            InputStream eventsInputStream = new FileInputStream(EVENTS_FILEPATH);
            InputStream usersInputStream = new FileInputStream(USERS_FILEPATH);

            InputStream recursiveEventsInputStream = new FileInputStream(RECURSIVE_EVENTS_FILEPATH);

            FileMetadata eventsMetadata = client.files().uploadBuilder("/" + EVENTS_FILEPATH).
                    withMode(WriteMode.OVERWRITE).uploadAndFinish(eventsInputStream);
            FileMetadata usersMetadata = client.files().uploadBuilder("/" + USERS_FILEPATH).
                    withMode(WriteMode.OVERWRITE).uploadAndFinish(usersInputStream);

            FileMetadata recursiveEventsMetadata = client.files().uploadBuilder("/" + RECURSIVE_EVENTS_FILEPATH).
                    withMode(WriteMode.OVERWRITE).uploadAndFinish(recursiveEventsInputStream);

            eventsInputStream.close();
            usersInputStream.close();

            recursiveEventsInputStream.close();

        } catch (FileNotFoundException eFNF) {
            logger.log(Level.SEVERE, "Cannot find file.", eFNF);
        } catch (IOException eIO) {
            logger.log(Level.SEVERE, "Cannot perform serialization.", eIO);
        } catch (DbxException eDBX) {
            logger.log(Level.SEVERE, "Dropbox raised an exception.", eDBX);
        }
    }

    /**
     * Checks if the user has save files for all supported data types.
     * Returns true if and only if all data types are saved.
     *
     * @return A boolean whether the user has save files
     */
    public boolean hasSavedData() {
        List<String> paths = List.of(EVENTS_FILEPATH, USERS_FILEPATH, RECURSIVE_EVENTS_FILEPATH);
        for (String path : paths) {
            if (!new File(path).exists()) return false;
        }
        return true;
    }

    /**
     * Read the file contents from the serialized files obtained from the dropbox repository.
     * Then type cast them into an ArrayList of Events.
     *
     * @return an ArrayList of all Events stored in the file
     */
    public Map<UUID, List<Event>> eventsReadFromSerializable(boolean recursive) {
        try {
            InputStream file;
            if (!recursive) {
                file = new FileInputStream(EVENTS_FILEPATH);
            } else {
                file = new FileInputStream(RECURSIVE_EVENTS_FILEPATH);
            }
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            //Please refer to specifications for explanation
            HashMap<UUID, List<Event>> recoveredEvents = (HashMap<UUID, List<Event>>) input.readObject();
            input.close();
            return recoveredEvents;
        } catch (IOException eIO) {
            logger.log(Level.SEVERE, "Cannot perform deserialization. Returning new blank Arraylist.", eIO);
            return new HashMap<>();
        } catch (ClassNotFoundException eCNF) {
            logger.log(Level.SEVERE, "Cannot find class. Returning new blank Arraylist.", eCNF);
            return new HashMap<>();
        }
    }

    /**
     * Write the serialized file to the filepath as specified.
     * The written object should be an ArrayList of Events.
     *
     * @param events an ArrayList of events to be serialized
     */
    public static void eventsWriteToSerializable(Map<UUID, List<Event>> events, boolean recursive) {
        try {
            OutputStream file;
            if(!recursive){
                file = new FileOutputStream(EVENTS_FILEPATH);
            }
            else{
                file = new FileOutputStream(RECURSIVE_EVENTS_FILEPATH);
            }
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(events);
            output.close();
        } catch (IOException eIO) {
            logger.log(Level.SEVERE, "Cannot perform serialization", eIO);
        }
    }

    /**
     * Read the file contents from the serialized files obtained from the dropbox repository.
     * Then type cast them into an ArrayList of Users.
     *
     * @return an ArrayList of all Users stored in the file
     */
    public List<User> usersReadFromSerializable() {
        try {
            InputStream file = new FileInputStream(USERS_FILEPATH);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            //Please refer to specifications for explanation
            List<User> recoveredUsers = (List<User>) input.readObject();
            input.close();
            return recoveredUsers;
        } catch (IOException eIO) {
            logger.log(Level.SEVERE, "Cannot perform deserialization. Returning new blank Arraylist.", eIO);
            return new ArrayList<>();
        } catch (ClassNotFoundException eCNF) {
            logger.log(Level.SEVERE, "Cannot find class. Returning new blank Arraylist.", eCNF);
            return new ArrayList<>();
        }
    }

    /**
     * Write the serialized file to the filepath as specified.
     * The written object should be an ArrayList of Users.
     *
     * @param users an ArrayList of users to be serialized
     */
    public static void usersWriteToSerializable(ArrayList<User> users) {
        try {
            OutputStream file = new FileOutputStream(USERS_FILEPATH);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(users);
            output.close();
        } catch (IOException eIO) {
            logger.log(Level.SEVERE, "Cannot perform serialization", eIO);
        }
    }




    /**
     * Delete files that are newly created to avoid data breach.
     */
    public void deleteNewFiles() {
        File events1Ser = new File("events1.ser");
        File users1Ser = new File("users1.ser");

        File recursiveEvents1Ser = new File("recursive_events1.ser");

        Boolean a = events1Ser.delete();
        Boolean b = users1Ser.delete();

        Boolean c = recursiveEvents1Ser.delete();
    }

    /**
     * Delete files that have been in the directory since the beginning to avoid data breach.
     */
    public void deleteOldFiles() {
        File eventsSer = new File("events.ser");
        File usersSer = new File("users.ser");

        File recursiveEventsSer = new File("recursive_events.ser");

        Boolean a = eventsSer.delete();
        Boolean b = usersSer.delete();

        Boolean c = recursiveEventsSer.delete();
    }
}

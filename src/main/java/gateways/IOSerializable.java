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
 */
public class IOSerializable {
    // Logging
    private static final Logger logger = Logger.getLogger(IOSerializable.class.getPackage().getName());

    // Filepaths for each data being serialized
    private static final String EVENTS_FILEPATH = "events.ser";
    private static final String USERS_FILEPATH = "users.ser";

    // A security token needed to access the Dropbox application
    private static final String ACCESS_TOKEN = "EfBUX9G7zxkAAAAAAAAAAaXr-kGtiOL1cwBhwIe7BcI0hvt-uH5LBsEh4FXJ31Ry";

    // A public Dropbox link where the serialized files are stored
    private static final String eventsURL = "https://www.dropbox.com/s/lzdvzsu1zwyeqkf/events.ser?dl=1";
    private static final String usersURL = "https://www.dropbox.com/s/pcbd8c2oyueq877/users.ser?dl=1";

    public IOSerializable(Boolean intro) {
        readFromDropbox(intro);
    }

    public void readFromDropbox(Boolean intro) {
        // Download from public Dropbox repository these two files, and save them in the directory temporarily.
        try {
            URL eventsDownload = new URL(eventsURL);
            URL usersDownload = new URL(usersURL);
            ReadableByteChannel eventsReadableByteChannel = Channels.newChannel(eventsDownload.openStream());
            ReadableByteChannel usersReadableByteChannel = Channels.newChannel(usersDownload.openStream());
            ArrayList<FileOutputStream> arrayList = introOrEnd(intro);
            FileOutputStream eventsFileOutputStream = arrayList.get(0);
            FileOutputStream usersFileOutputStream = arrayList.get(1);
            eventsFileOutputStream.getChannel().transferFrom(eventsReadableByteChannel, 0, 1 << 24);
            usersFileOutputStream.getChannel().transferFrom(usersReadableByteChannel, 0, 1 << 24);
            eventsFileOutputStream.close();
            usersFileOutputStream.close();
            eventsReadableByteChannel.close();
            usersReadableByteChannel.close();
        } catch (IOException eIO) {
            eIO.printStackTrace();
        }
    }

    public ArrayList<FileOutputStream> introOrEnd(Boolean intro) {
        try {
            if (intro) {
                return new ArrayList<>(Arrays.asList(new FileOutputStream(EVENTS_FILEPATH),
                        new FileOutputStream(USERS_FILEPATH)));
            } else {
                return new ArrayList<>(Arrays.asList(new FileOutputStream("events1.ser"),
                        new FileOutputStream("users1.ser")));
            }
        } catch (IOException eIO) {
            eIO.printStackTrace();
        }
        return null;
    }

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
// Commenting out delete to check if overwrite is viable.
//            DeleteResult delEvents1 = client.files().deleteV2("/" + EVENTS_FILEPATH);
//            DeleteResult delUsers1 = client.files().deleteV2("/" + USERS_FILEPATH);
            InputStream eventsInputStream = new FileInputStream(EVENTS_FILEPATH);
            InputStream usersInputStream = new FileInputStream(USERS_FILEPATH);
            FileMetadata eventsMetadata = client.files().uploadBuilder("/" + EVENTS_FILEPATH).
                    withMode(WriteMode.OVERWRITE).uploadAndFinish(eventsInputStream);
            FileMetadata usersMetadata = client.files().uploadBuilder("/" + USERS_FILEPATH).
                    withMode(WriteMode.OVERWRITE).uploadAndFinish(usersInputStream);
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
     * @return A boolean whether the user has save files.
     */
    public boolean hasSavedData() {
        List<String> paths = List.of(EVENTS_FILEPATH, USERS_FILEPATH);
        for (String path : paths) {
            if (!new File(path).exists()) return false;
        }
        return true;
    }

    public ArrayList<Event> eventsReadFromSerializable() {
        try {
            InputStream file = new FileInputStream(EVENTS_FILEPATH);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            //Please refer to specifications for explanation
            ArrayList<Event> recoveredEvents = (ArrayList<Event>) input.readObject();
            input.close();
            return recoveredEvents;
        } catch (IOException eIO) {
            logger.log(Level.SEVERE, "Cannot perform deserialization. Returning new blank Arraylist.", eIO);
            return new ArrayList<>();
        } catch (ClassNotFoundException eCNF) {
            logger.log(Level.SEVERE, "Cannot find class. Returning new blank Arraylist.", eCNF);
            return new ArrayList<>();
        }
    }

    public void eventsWriteToSerializable(ArrayList<Event> events) {
        try {
            OutputStream file = new FileOutputStream(EVENTS_FILEPATH);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(events);
            output.close();
        } catch (IOException eIO) {
            logger.log(Level.SEVERE, "Cannot perform serialization", eIO);
        }
    }

    public ArrayList<User> usersReadFromSerializable() {
        try {
            InputStream file = new FileInputStream(USERS_FILEPATH);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            //Please refer to specifications for explanation
            ArrayList<User> recoveredUsers = (ArrayList<User>) input.readObject();
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

    public void usersWriteToSerializable(ArrayList<User> users) {
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

// Create new events.ser and users.ser in case they are deleted. In that case, write methods must be static.
//    public static void main(String[] args) {
//        Event event1 = new Event(1, "Example Event 1", 2021, 11, 10, 0, 1, 0, 0);
//        Event event2 = new Event(2, "Example Event 2", 2021, 11, 10, 1, 2, 0, 0);
//        Event event3 = new Event(3, "Example Event 3", 2021, 11, 10, 2, 3, 0, 0);
//        Event event4 = new Event(4, "Example Event 4", 2021, 11, 10, 3, 4, 0, 0);
//        Event event5 = new Event(5, "Example Event 5", 2021, 11, 10, 4, 5, 0, 0);
//        ArrayList<Event> events = new ArrayList<>(Arrays.asList(event1, event2, event3, event4, event5));
//        User user1 = new User(UUID.randomUUID(), "Example User 1", "username1", "password1");
//        User user2 = new User(UUID.randomUUID(), "Example User 2", "username2", "password2");
//        User user3 = new User(UUID.randomUUID(), "Example User 3", "username3", "password3");
//        User user4 = new User(UUID.randomUUID(), "Example User 4", "username4", "password4");
//        User user5 = new User(UUID.randomUUID(), "Example User 5", "username5", "password5");
//        ArrayList<User> users = new ArrayList<>(Arrays.asList(user1, user2, user3, user4, user5));
//        eventsWriteToSerializable(events);
//        usersWriteToSerializable(users);
//    }
}

package presenters.MenuStrategies;

import interfaces.MenuContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic menu contents to be displayed at MainController
 * @author Seo Won Yi
 */
public class BasicMenuContent implements MenuContent {

    @Override
    public List<String> getContent() {
        return new ArrayList<>(){{
            add("1. Profile Setting");
            add("2. View/Export Calendar by Type and Date");
            add("3. Add a New Event");
            add("4. View/Modify an Existing Event through Calendar");
            add("5. Create Repetition of the Existing Events");
            add("6. Export Entire Calendar to iCal File");
            add("7. Log Out");
            add("8. Exit");
        }};
    }

}

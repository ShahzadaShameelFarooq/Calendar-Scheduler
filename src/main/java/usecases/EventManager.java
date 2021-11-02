package usecases;

import java.time.LocalTime;
import java.util.*;
import java.time.LocalDate;
import entities.Event;
import entities.RecursiveEvent;
import interfaces.EventListObserver;


public class EventManager{

    private final Map<Integer, Event> eventMap;
    private RepeatedEventManager repeatedEventManager;
    private EventListObserver[] toUpdate;


    /**
     * constructor for event manager
     * @param events a list of the current users events
     */
    public EventManager(List<Event> events, RepeatedEventManager repeatedEventManager){
        this.eventMap = new HashMap<>();
        this.repeatedEventManager = repeatedEventManager;

        for (Event event: events){
            this.eventMap.put(event.getID(), event);
        }
    }

    /**
     * empty EventManager
     */
    public EventManager(){
        this.eventMap = new HashMap<>();
    }

    public static Integer getID(Event event) {
        return event.getID();
    }

    /**
     * getDay returns a map of the events in a day
     * @param day the day that is being searched for
     * @return all events in this day
     */
    public Map<Integer, Event> getDay(LocalDate day){
        Map<Integer, Event> dayMap = new HashMap<>();
        for (Event event: eventMap.values()){
            if (event.getDay().isEqual(day)) {
                dayMap.put(event.getID(), event);
            }
        }
        return dayMap;
    }

    /**
     *
     * @param ID the name of an existing event
     * @return the event of this name
     */
    public Event get(Integer ID){
        return eventMap.get(ID);
    }

    /**
     * removes an event from the set
     * @param ID the name to be removed
     * @return the event just removed
     */
    public Event remove(Integer ID){
        return eventMap.remove(ID);
    }

    /**
     *
     * @param year year of event
     * @param month month of event
     * @param day date of event
     * @param endHour time event ends (form HHMM)
     * @param endMin end minute
     */
    public void addEvent(Integer ID, String name, String type, int year, int month, int day, int endHour, int endMin,
                         String categoryName, String otherInformation){
        Event event = new Event(ID, name, type, year, month, day, endHour, endMin, categoryName, otherInformation);
        this.eventMap.put(event.getID(), event);
    }

    public void addEvent(Event event){
        this.eventMap.put(event.getID(), event);
    }

    public void addEventsInRecursion(RecursiveEvent recursiveEvent){
        for(ArrayList<Event> events : repeatedEventManager.getEventsFromRecursion(recursiveEvent.getId()).values()){
            for(Event event : events){
                this.addEvent(event);
            }
        }
    }


    public void addEventsInRecursion(){
        for(RecursiveEvent recursiveEvent : this.repeatedEventManager.getRecursiveEventMap().values()){
            this.addEventsInRecursion(recursiveEvent);
        }
    }



    public String getName(Event event){
        return event.getName();
    }
    public String getStart(Event event) {return event.getStartString();}
    public String getStartTime(Event event){
        String[] date = event.getStartString().split("-");
        return date[2].substring(3, 8);
    }
    public String getEndTime(Event event){
        String[] date = event.getEndString().split("-");
        return date[2].substring(3, 8);
    }

    public String getEnd(Event event) {return event.getEndString();}

    public void update(String addRemoveChange, Map<Integer, Event> changed){
        for (EventListObserver obs: this.toUpdate){
            obs.update(addRemoveChange, changed);
        }
    }
    public void addObserver(EventListObserver obs){
        ArrayList<EventListObserver> inter = new ArrayList<>(List.of(this.toUpdate));
        inter.add(obs);
        this.toUpdate = inter.toArray(new EventListObserver[0]);
    }

    public String getAllNames(){
        StringBuilder list = new StringBuilder();
        for (Event event: eventMap.values()){
            list.append(event.getName());
        }
        return list.toString();
    }

    public float totalHours(List<Event> events){
        float hours = 0;
        for(Event event: events) {hours += event.getLength();}
        return hours;
    }

    public Event earliest(List<Event> events){
        Event earliest = events.get(0);
        for (Event event: events){
            if (event.getStartTime().isBefore(earliest.getStartTime())){
                earliest = event;
            }
        }
        return earliest;
    }
    public List<Event> timeOrder(List<Event> events){
        List<Event> sorted = new ArrayList<>();
        while (!events.isEmpty()){
            sorted.add(earliest(events));
            events.remove(earliest(events));
        }
        return sorted;
    }

    public List<Event> getAllEvents() {
        List<Event> result = new ArrayList<>();
        result.addAll(this.eventMap.values());
        return result;
    }
}

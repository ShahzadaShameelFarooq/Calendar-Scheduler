package entities.recursions;

import entities.Event;
import interfaces.DateGetter;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Malik Lahlou
 */

public class RecursiveEvent {


    /**
     * + eventsInOneCycle ;for example if a lecture is tuesdays and thurdays of each week, the cycle will consist
     *  of [(tuesday, date+time of lecture), (thursday, date+time of lecture), (tuesday, date+time of lecture after the
     *  thursday one)]. If an event repeats 3 times each 2 week, the list would have 3 elements with the days and
     *  times of the three occurrences of the event.
     *  methodToGetDate: indicates which method the user want to handle repetitions. For now, the user can either input
     *  the number of times a cycle repeats, or the two dates in between the cycle repeats.
     */


    private final Integer id;
    private ArrayList<Event> eventsInOneCycle;
    private DateGetter methodToGetDate;

    public RecursiveEvent(Integer id, ArrayList<Event> events, DateGetter methodToGetDate){
        this.id = id;
        this.eventsInOneCycle = new ArrayList<>();
        for(Event event : events){
            this.eventsInOneCycle.add(event);
            event.setRecursiveId(id);
        }
        this.methodToGetDate = methodToGetDate;
    }

    public RecursiveEvent(Integer id){
        this.id = id;
    }
    /**
     * Getter methods.
     */

    public Integer getId() {return id;}
    public ArrayList<Event> getEventsInOneCycle() {return eventsInOneCycle;}
    public int getCycleLength(){
        return eventsInOneCycle.size() -1;
    }

    /**
     * Setter methods.
     */
    public void setEventsInOneCycle(ArrayList<Event> eventsInOneCycle) {this.eventsInOneCycle = eventsInOneCycle;}
    public void setMethodToGetDate(DateGetter methodToGetDate) {this.methodToGetDate = methodToGetDate;}

    /**
     * If user were to Remove/add/change an event from/to a recursion, these methods return cycle in which they will be.
     */

    // TODO (for phase 2): give the user the choice to modify cycles by include cycleAfterAdditionOrChange and
    //  cycleAfterRemoval in event manager and controller
    // TODO (for phase 2): test these methods after including them.
    // TODO (for phase 2): make cycleAfterAdditionOrChange shorter by creating private helper methods

    public void addOrChange(String addChange, ArrayList<Event> objects, Event object, int index){
        if (!Objects.equals(addChange, "add")) {
            objects.remove(index);
        }
        objects.add(index, object);
    }

    public ArrayList<Event> cycleAfterRemoval(Event event){
        int cycleLength = this.getCycleLength();
        ArrayList<Event> eventsInCycles = this.listOfEventsInCycles(this.eventsInOneCycle);
        int eventIndex = eventsInCycles.indexOf(event);
        int rest = eventIndex % cycleLength;
        int quotient = eventIndex - rest;
        ArrayList<Event> newCycle = new ArrayList<>();
        for (int i = 0 ; i < quotient ; i++){
            newCycle.add(eventsInCycles.get(quotient + i));
        }
        newCycle.remove(rest);
        return newCycle;
    }

    public ArrayList<Event> cycleAfterAdditionOrChange(Event event, String addChange){
        event.setRecursiveId(this.id);
        int cycleLength = this.getCycleLength();
        Event firstEvent1 = this.eventsInOneCycle.get(0);
        ArrayList<Event> tempCycle = this.createEventInCycles(firstEvent1);
        int i = 0;
        Event currentEvent = tempCycle.get(i);
        while(event.getStartTime().isAfter(currentEvent.getStartTime())){
            i++;
            currentEvent = tempCycle.get(i);
        }
        if(i==0){
            ArrayList<Event> newCycle = this.eventsInOneCycle;
            int j = 1;
            Event thisEvent = newCycle.get(j);
            while (event.getStartTime().isAfter(thisEvent.getStartTime())){
                j++;
                thisEvent = newCycle.get(j);
            }
            this.addOrChange(addChange, newCycle, event, j);
            return newCycle;
        }
        else{
            ArrayList<Event> aCycle = this.listOfEventsInCycles(this.eventsInOneCycle);
            ArrayList<Event> newCycle = new ArrayList<>();
            for (int k = 0 ; k < cycleLength ; k++){
                newCycle.add(aCycle.get(i*cycleLength + k));
            }
            int j = 1;
            Event thisEvent = newCycle.get(j);
            while (event.getStartTime().isAfter(thisEvent.getStartTime())){
                j++;
                thisEvent = newCycle.get(j);
            }
            this.addOrChange(addChange, newCycle, event, j);
            return newCycle;
        }
    }

    /**
     *
     * Uses the classes that implement the date getter interface to return the dates of all the events in the
     * period of repetition.
     * @param events The events in one repetition cycle.
     * @return list of repetitions of event in events
     */

    public ArrayList<Event> listOfEventsInCycles(ArrayList<Event> events){
        return methodToGetDate.listOfDatesInCycles(events);
    }

    /**
     *
     * @param event The specific event.
     * @return Given a specific event in a cycle, this method returns an arrayList of all the events
     * (which are repetitions of this specific event) in the period of repetition (don't include original event
     * in this.eventsInOneCycle).
     */

    public ArrayList<Event> createEventInCycles(Event event){
        ArrayList<Event> result = new ArrayList<>();
        int indexOfEvent = this.eventsInOneCycle.indexOf(event);
        ArrayList<Event> listOfDatesInCycles = this.listOfEventsInCycles(this.eventsInOneCycle);
        int cyclesLength = listOfDatesInCycles.size();
        int i = 0;
        while(indexOfEvent + this.getCycleLength()*i < cyclesLength){
            listOfDatesInCycles.get(indexOfEvent + this.getCycleLength()*i).setRecursiveId(this.id);
            result.add(listOfDatesInCycles.get(indexOfEvent + this.getCycleLength()*i));
            i += 1;
        }
        return result;
    }


}

package GUISwing;

import controllers.EventController;
import controllers.MainController;
import helpers.Constants;
import helpers.GUIInfoProvider;
import interfaces.EventInfoGetter;
import interfaces.MeltParentWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

/**
 * Event editing page
 * @author Seo Won Yi
 * @author Taite Cullen
 * @see EventController
 * @see EventInfoGetter
 */

public class EditEventWindow implements ActionListener, MeltParentWindow {
    private final MainController mc;
    private final EventInfoGetter eventInfoGetter;
    private final UUID eventID;
    private final MeltParentWindow parent;
    private final GUIInfoProvider helper;
    private final JFrame frame;
    private final EventController ec;
    private JTextField eventName;
    private JTextArea eventDescription;
    private final JPanel timeInfoPanel;
    private JButton setUpStart;
    private JButton setUpEnd;
    private JButton workSessionButton;
    private JButton recursionButton;
    private JButton saveButton;
    private JButton deleteButton;

    /**
     * Construct the EditEventWindow
     * @param mc MainController with access to every controller
     * @param eventInfoGetter Interface used for obtaining event information
     * @param eventID ID of the event to edit on
     * @param parent parent window (prev window)
     */
    public EditEventWindow(MainController mc, EventInfoGetter eventInfoGetter, UUID eventID, MeltParentWindow parent,
                           String option) {
        this.helper = new GUIInfoProvider();
        this.eventID = eventID;
        this.parent = parent;
        this.eventInfoGetter = eventInfoGetter;
        this.frame = new PopUpWindowFrame();
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.mc = mc;
        this.ec = mc.getEventController();
        this.timeInfoPanel = new JPanel();
        JPanel textInfoPanel = new JPanel();
        textInfoPanelSetUp(textInfoPanel);
        setUpTextInfo(textInfoPanel);
        timeInfoPanelSetUp();
        setUpTimeInfo(eventID, helper);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(null);
        buttonPanel.setBackground(Constants.WINDOW_COLOR);
        buttonPanel.setBounds(0, 80, Constants.POPUP_WIDTH, Constants.POPUP_HEIGHT * 2 / 3);
        setUpButtons();
        addButtons(buttonPanel);
        addActionLister();
        this.frame.add(textInfoPanel);
        this.frame.add(timeInfoPanel);
        this.frame.add(buttonPanel);
        this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (option.equalsIgnoreCase("Add")) {
                    removeEvent(eventID);
                }
                else {
                    JOptionPane.showMessageDialog(frame, "Any changes made for Start and End Time have been saved");
                }
                parent.refresh();
                parent.enableFrame();
                exitFrame();
            }
        });

        this.frame.setVisible(true);
    }

    /**
     * Set up the text info panel that will have text information such as name and description
     * @param textInfoPanel panel to have text information on
     */
    private void textInfoPanelSetUp(JPanel textInfoPanel) {
        textInfoPanel.setBounds(0, 0, 230, Constants.POPUP_HEIGHT / 3);
        textInfoPanel.setLayout(null);
        textInfoPanel.setBackground(Constants.WINDOW_COLOR);
    }

    /**
     * Set up the time information and put them on the panel
     * @param eventID ID of an event to be considered
     * @param helper GUIInfoProvider object that will help to provide with time information of the event
     */
    private void setUpTimeInfo(UUID eventID, GUIInfoProvider helper) {
        JLabel viewStart = new JLabel("Current Event Start Time:");
        viewStart.setBounds(40, 13, 270, 20);
        JLabel startTime = new JLabel(helper.getEventStartInfo(eventID, eventInfoGetter));
        startTime.setBounds(40, 33, 270, 20);
        JLabel viewEnd = new JLabel("Current Event End Time:");
        viewEnd.setBounds(40, 53, 270, 20);
        JLabel endTime = new JLabel(helper.getEventEndInfo(eventID, eventInfoGetter));
        endTime.setBounds(40, 73, 270, 20);
        timeInfoPanel.add(viewStart);
        timeInfoPanel.add(viewEnd);
        timeInfoPanel.add(startTime);
        timeInfoPanel.add(endTime);
    }

    /**
     * set up the text information (name and description of event) and put them on the panel
     * @param panel panel to contain the text information
     */
    private void setUpTextInfo(JPanel panel) {
        eventName = new JTextField(eventInfoGetter.getName(this.eventID));
        eventName.setBounds(30, 17, 200, 20);
        eventDescription = new JTextArea(eventInfoGetter.getDescription(this.eventID));
        //eventDescription.setBounds(30, 47, 200, 50);
        eventDescription.setWrapStyleWord(true);
        eventDescription.setLineWrap(true);
        JScrollPane descriptionPane = new JScrollPane(eventDescription, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionPane.setBounds(30, 47, 200, 50);
        panel.add(eventName);
        this.frame.add(descriptionPane);
    }

    /**
     * Set up the buttons to be used
     */
    private void setUpButtons() {
        setUpStart = new JButton("Modify Start Time");
        setUpStart.setBounds(30, 50, 200, 20);
        setUpEnd = new JButton("Modify End Time");
        setUpEnd.setBounds(270, 50, 200, 20);
        workSessionButton = new JButton("Add / Modify Work Sessions");
        workSessionButton.setBounds(30, 90, 200, 20);
        recursionButton = new JButton("Add to a recursion");
        recursionButton.setBounds(270, 90, 200, 20);
        saveButton = new JButton("Save");
        saveButton.setBounds(30, 130, 200, 20);
        deleteButton = new JButton("Delete");
        deleteButton.setBounds(270, 130, 200, 20);
    }

    /**
     * Enable actions with buttons
     */
    private void addActionLister() {
        setUpStart.addActionListener(this);
        setUpEnd.addActionListener(this);
        workSessionButton.addActionListener(this);
        recursionButton.addActionListener(this);
        saveButton.addActionListener(this);
        deleteButton.addActionListener(this);
    }

    /**
     * Add buttons to the panel
     * @param buttonPanel panel that contains all the buttons
     */
    private void addButtons(JPanel buttonPanel) {
        buttonPanel.add(setUpStart);
        buttonPanel.add(setUpEnd);
        buttonPanel.add(workSessionButton);
        buttonPanel.add(recursionButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
    }

    /**
     * Set up the time information panel
     */
    private void timeInfoPanelSetUp() {
        this.timeInfoPanel.setBounds(230, 0, Constants.POPUP_WIDTH - 230, Constants.POPUP_HEIGHT / 3);
        this.timeInfoPanel.setLayout(null);
        this.timeInfoPanel.setBackground(Constants.WINDOW_COLOR);
    }

    /**
     * remove the event
     * @param eventID eventID to be removed
     */
    private void removeEvent(UUID eventID) {
        this.ec.delete(eventID);
    }

    /**
     * configuration of button actions
     * @param e each action to be considered from
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setUpStart) {
            this.frame.setEnabled(false);
            new TimeSetUp(this.mc, this.eventInfoGetter, this.eventID,this, "Start");
        }

        if (e.getSource() == setUpEnd) {
            this.frame.setEnabled(false);
            new TimeSetUp(this.mc, this.eventInfoGetter, this.eventID,this, "End");
        }

        if (e.getSource() == workSessionButton) {
            this.frame.setEnabled(false);
            new WorkSessionEdit(this.ec, this.eventInfoGetter, this.ec.getWorkSessionController()
                    .getWorkSessionManager(this.ec.getEventManager()), this, eventID);
        }

        if (e.getSource() == recursionButton) {
            this.parent.enableFrame();
            exitFrame();
        }

        if (e.getSource() == saveButton) {
            this.ec.changeName(this.eventID, eventName.getText());
            this.ec.changeDescription(this.eventID, eventDescription.getText());
            this.parent.enableFrame();
            this.parent.refresh();
            this.exitFrame();
        }

        if (e.getSource() == deleteButton) {
            if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to remove this event?",
                    "Remove Event", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                this.ec.delete(eventID);
                this.parent.enableFrame();
                this.parent.refresh();
                this.exitFrame();
            }
        }
    }

    /**
     * Refresh the page by reloading specific panels
     */
    @Override
    public void refresh() {
        this.timeInfoPanel.removeAll();
        timeInfoPanelSetUp();
        setUpTimeInfo(this.eventID, this.helper);
        this.frame.revalidate();
        this.frame.repaint();
    }

    /**
     * get parent object (prev window)
     * @return the parent object (prev window)
     */
    @Override
    public MeltParentWindow getParent() {
        return this.parent;
    }

    /**
     * enable the frame
     */
    @Override
    public void enableFrame() {
        this.frame.setEnabled(true);
    }

    /**
     * exit the frame
     */
    @Override
    public void exitFrame() {
        this.frame.dispose();
    }
}

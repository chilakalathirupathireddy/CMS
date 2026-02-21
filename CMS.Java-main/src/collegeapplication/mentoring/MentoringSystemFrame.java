package collegeapplication.mentoring;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MentoringSystemFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private MentoringDataAccess dataAccess = new MentoringDataAccess();
    private User currentUser;

    private JPanel cardPanel = new JPanel(new CardLayout());

    private JTextField usernameField = new JTextField();
    private JTextField passwordField = new JTextField();
    private JComboBox<String> roleBox = new JComboBox<String>(new String[] { "student", "mentor", "admin" });

    private JLabel welcomeLabel = new JLabel();
    private JPanel dashboardPanel = new JPanel(new BorderLayout());

    public MentoringSystemFrame() {
        setTitle("Online Student Mentoring System");
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardPanel.add(createLoginPanel(), "login");
        cardPanel.add(dashboardPanel, "dashboard");
        add(cardPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.add(new JLabel("Role"));
        panel.add(roleBox);
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        JPanel holder = new JPanel(new BorderLayout());
        holder.add(panel, BorderLayout.CENTER);
        holder.add(loginButton, BorderLayout.SOUTH);
        return holder;
    }

    private void login() {
        String role = (String) roleBox.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        User user = dataAccess.login(role, username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid login");
            return;
        }
        currentUser = user;
        renderDashboard();
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, "dashboard");
    }

    private void renderDashboard() {
        dashboardPanel.removeAll();
        welcomeLabel.setText("Logged in as " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        dashboardPanel.add(welcomeLabel, BorderLayout.NORTH);

        if ("student".equals(currentUser.getRole())) {
            dashboardPanel.add(createStudentPanel(), BorderLayout.CENTER);
        } else if ("mentor".equals(currentUser.getRole())) {
            dashboardPanel.add(createMentorPanel(), BorderLayout.CENTER);
        } else {
            dashboardPanel.add(createAdminPanel(), BorderLayout.CENTER);
        }

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) cardPanel.getLayout()).show(cardPanel, "login");
            }
        });
        dashboardPanel.add(logoutButton, BorderLayout.SOUTH);
        dashboardPanel.revalidate();
        dashboardPanel.repaint();
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 12, 12));

        DefaultTableModel model = new DefaultTableModel(new String[] { "Subject", "Marks", "Attendance" }, 0);
        List<String[]> performance = dataAccess.getStudentPerformance(currentUser.getId());
        for (int i = 0; i < performance.size(); i++) {
            model.addRow(performance.get(i));
        }
        panel.add(new JScrollPane(new JTable(model)));

        JList<String> noticeList = new JList<String>(dataAccess.getNotifications("student").toArray(new String[0]));
        panel.add(new JScrollPane(noticeList));

        JPanel messagePanel = new JPanel(new BorderLayout());
        JTextArea messageArea = new JTextArea();
        JButton sendButton = new JButton("Send Query to Mentor");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int mentorId = dataAccess.getAssignedMentorId(currentUser.getId());
                if (mentorId < 1 || messageArea.getText().trim().isEmpty()) {
                    return;
                }
                dataAccess.sendMessage(currentUser.getId(), mentorId, messageArea.getText().trim());
                JOptionPane.showMessageDialog(MentoringSystemFrame.this, "Message sent");
                renderDashboard();
            }
        });
        messagePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.SOUTH);
        panel.add(messagePanel);

        JList<String> inboxList = new JList<String>(dataAccess.getStudentMessages(currentUser.getId()).toArray(new String[0]));
        panel.add(new JScrollPane(inboxList));

        return panel;
    }

    private JPanel createMentorPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 12, 12));

        DefaultTableModel model = new DefaultTableModel(new String[] { "Student", "Attendance", "Avg Marks", "Student ID" }, 0);
        List<String[]> studentRows = dataAccess.getMentorStudents(currentUser.getId());
        for (int i = 0; i < studentRows.size(); i++) {
            model.addRow(studentRows.get(i));
        }
        panel.add(new JScrollPane(new JTable(model)));

        JList<String> inboxList = new JList<String>(dataAccess.getMentorInbox(currentUser.getId()).toArray(new String[0]));
        panel.add(new JScrollPane(inboxList));

        JPanel announcePanel = new JPanel(new BorderLayout());
        JTextArea announceText = new JTextArea();
        JButton postButton = new JButton("Post Guidance");
        postButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (announceText.getText().trim().isEmpty()) {
                    return;
                }
                dataAccess.postAnnouncement(currentUser.getId(), "student", announceText.getText().trim());
                JOptionPane.showMessageDialog(MentoringSystemFrame.this, "Announcement posted");
                renderDashboard();
            }
        });
        announcePanel.add(new JScrollPane(announceText), BorderLayout.CENTER);
        announcePanel.add(postButton, BorderLayout.SOUTH);
        panel.add(announcePanel);

        JList<String> sessions = new JList<String>(dataAccess.getMentorSessions(currentUser.getId()).toArray(new String[0]));
        panel.add(new JScrollPane(sessions));

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 12, 12));

        List<String[]> students = dataAccess.getUsersByRole("student");
        List<String[]> mentors = dataAccess.getUsersByRole("mentor");

        JComboBox<String> studentBox = new JComboBox<String>();
        for (int i = 0; i < students.size(); i++) {
            studentBox.addItem(students.get(i)[0] + " - " + students.get(i)[1]);
        }
        JComboBox<String> mentorBox = new JComboBox<String>();
        for (int i = 0; i < mentors.size(); i++) {
            mentorBox.addItem(mentors.get(i)[0] + " - " + mentors.get(i)[1]);
        }

        JPanel assignPanel = new JPanel(new GridLayout(5, 1));
        assignPanel.add(new JLabel("Assign Mentor to Student"));
        assignPanel.add(studentBox);
        assignPanel.add(mentorBox);
        JButton assignButton = new JButton("Assign");
        assignButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (studentBox.getSelectedItem() == null || mentorBox.getSelectedItem() == null) {
                    return;
                }
                int studentId = Integer.parseInt(studentBox.getSelectedItem().toString().split(" - ")[0]);
                int mentorId = Integer.parseInt(mentorBox.getSelectedItem().toString().split(" - ")[0]);
                dataAccess.assignMentor(mentorId, studentId);
                JOptionPane.showMessageDialog(MentoringSystemFrame.this, "Assignment updated");
            }
        });
        assignPanel.add(assignButton);
        panel.add(assignPanel);

        JTextArea adminAnnouncement = new JTextArea();
        JButton post = new JButton("Broadcast Notification");
        post.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (adminAnnouncement.getText().trim().isEmpty()) {
                    return;
                }
                dataAccess.postAnnouncement(currentUser.getId(), "all", adminAnnouncement.getText().trim());
                JOptionPane.showMessageDialog(MentoringSystemFrame.this, "Broadcasted");
                renderDashboard();
            }
        });

        JPanel postPanel = new JPanel(new BorderLayout());
        postPanel.add(new JScrollPane(adminAnnouncement), BorderLayout.CENTER);
        postPanel.add(post, BorderLayout.SOUTH);
        panel.add(postPanel);

        JList<String> allUsers = new JList<String>(buildUserList(students, mentors));
        panel.add(new JScrollPane(allUsers));

        JList<String> notices = new JList<String>(dataAccess.getNotifications("admin").toArray(new String[0]));
        panel.add(new JScrollPane(notices));

        return panel;
    }

    private String[] buildUserList(List<String[]> students, List<String[]> mentors) {
        List<String[]> admins = dataAccess.getUsersByRole("admin");
        String[] list = new String[students.size() + mentors.size() + admins.size()];
        int index = 0;

        for (int i = 0; i < students.size(); i++) {
            list[index++] = "Student: " + students.get(i)[1] + " (" + students.get(i)[2] + ")";
        }
        for (int i = 0; i < mentors.size(); i++) {
            list[index++] = "Mentor: " + mentors.get(i)[1] + " (" + mentors.get(i)[2] + ")";
        }
        for (int i = 0; i < admins.size(); i++) {
            list[index++] = "Admin: " + admins.get(i)[1] + " (" + admins.get(i)[2] + ")";
        }

        return list;
    }

    public static void main(String[] args) {
        MentoringSystemFrame frame = new MentoringSystemFrame();
        frame.setVisible(true);
    }
}

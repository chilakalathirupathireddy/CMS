package collegeapplication.mentoring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MentoringDataAccess {

    private static final String URL = "jdbc:mysql://localhost:3306/mentoring_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public User login(String role, String username, String password) {
        String sql = "select id,name,role from users where role=? and username=? and password=?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, username);
            ps.setString(3, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"), rs.getString("role"), username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String[]> getStudentPerformance(int studentUserId) {
        List<String[]> rows = new ArrayList<String[]>();
        String sql = "select m.subject,m.mark,a.attendance_percent from marks m "
                + "join attendance a on a.student_user_id=m.student_user_id "
                + "where m.student_user_id=?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[] { rs.getString("subject"), String.valueOf(rs.getInt("mark")),
                        String.valueOf(rs.getInt("attendance_percent")) + "%" });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<String> getNotifications(String role) {
        List<String> items = new ArrayList<String>();
        String sql = "select message from announcements where target_role in (?, 'all') order by created_at desc";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(rs.getString("message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<String> getMentorInbox(int mentorUserId) {
        List<String> items = new ArrayList<String>();
        String sql = "select u.name as from_name,m.message_text from messages m "
                + "join users u on u.id=m.from_user_id where m.to_user_id=? order by m.created_at desc";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, mentorUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(rs.getString("from_name") + ": " + rs.getString("message_text"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<String> getStudentMessages(int studentUserId) {
        List<String> items = new ArrayList<String>();
        String sql = "select u.name as from_name,m.message_text from messages m "
                + "join users u on u.id=m.from_user_id "
                + "where m.from_user_id=? or m.to_user_id=? order by m.created_at desc";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentUserId);
            ps.setInt(2, studentUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(rs.getString("from_name") + ": " + rs.getString("message_text"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public int getAssignedMentorId(int studentUserId) {
        String sql = "select mentor_user_id from mentor_student_assignments where student_user_id=?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("mentor_user_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void sendMessage(int fromUserId, int toUserId, String text) {
        String sql = "insert into messages(from_user_id,to_user_id,message_text) values(?,?,?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            ps.setString(3, text);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getMentorStudents(int mentorUserId) {
        List<String[]> rows = new ArrayList<String[]>();
        String sql = "select u.id as user_id,u.name,a.attendance_percent,round(avg(m.mark)) as avg_marks "
                + "from mentor_student_assignments msa "
                + "join users u on u.id=msa.student_user_id "
                + "left join attendance a on a.student_user_id=u.id "
                + "left join marks m on m.student_user_id=u.id "
                + "where msa.mentor_user_id=? group by u.id,u.name,a.attendance_percent";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, mentorUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[] { rs.getString("name"), rs.getString("attendance_percent") + "%",
                        String.valueOf(rs.getInt("avg_marks")), rs.getString("user_id") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<String> getMentorSessions(int mentorUserId) {
        List<String> rows = new ArrayList<String>();
        String sql = "select u.name,s.session_date,s.session_time from mentoring_sessions s "
                + "join users u on u.id=s.student_user_id where s.mentor_user_id=? order by s.session_date";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, mentorUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(rs.getDate("session_date") + " " + rs.getString("session_time") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public void postAnnouncement(int authorUserId, String role, String message) {
        String sql = "insert into announcements(author_user_id,target_role,message) values(?,?,?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorUserId);
            ps.setString(2, role);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getUsersByRole(String role) {
        List<String[]> rows = new ArrayList<String[]>();
        String sql = "select id,name,username from users where role=?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[] { String.valueOf(rs.getInt("id")), rs.getString("name"), rs.getString("username") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public void assignMentor(int mentorUserId, int studentUserId) {
        String deleteSql = "delete from mentor_student_assignments where student_user_id=?";
        String insertSql = "insert into mentor_student_assignments(mentor_user_id,student_user_id) values(?,?)";
        try (Connection con = getConnection(); PreparedStatement dps = con.prepareStatement(deleteSql);
                PreparedStatement ips = con.prepareStatement(insertSql)) {
            dps.setInt(1, studentUserId);
            dps.executeUpdate();
            ips.setInt(1, mentorUserId);
            ips.setInt(2, studentUserId);
            ips.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

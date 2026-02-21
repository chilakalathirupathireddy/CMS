# CMS

## Online Student Mentoring System (Java + MySQL)

This implementation provides a Java Swing based Online Student Mentoring System backed by a MySQL database.

### Core Features
- Student, Mentor, and Admin login with role-based access
- Attendance and marks tracking for students
- Mentor-student communication via stored messages
- Announcements and mentoring session alerts
- Admin tools for mentor assignment and data oversight

### Setup
1. Import SQL schema:
   - Run `CMS.Java-main/mentoring_system.sql` in MySQL.
2. Ensure MySQL credentials in:
   - `CMS.Java-main/src/collegeapplication/mentoring/MentoringDataAccess.java`
3. Add MySQL JDBC driver (already available in `CMS.Java-main/jar files/mysql-connector-java-5.1.38.jar`).
4. Run:
   - `collegeapplication.mentoring.MentoringSystemFrame`

### Demo credentials
- Student: `stu01 / student123`
- Mentor: `men01 / mentor123`
- Admin: `adm01 / admin123`

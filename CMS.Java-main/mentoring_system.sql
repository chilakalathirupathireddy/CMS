CREATE DATABASE IF NOT EXISTS mentoring_system;
USE mentoring_system;

CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL,
  role ENUM('student','mentor','admin') NOT NULL
);

CREATE TABLE mentor_student_assignments (
  id INT PRIMARY KEY AUTO_INCREMENT,
  mentor_user_id INT NOT NULL,
  student_user_id INT NOT NULL UNIQUE,
  FOREIGN KEY (mentor_user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (student_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE attendance (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_user_id INT NOT NULL,
  attendance_percent INT NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (student_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE marks (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_user_id INT NOT NULL,
  subject VARCHAR(60) NOT NULL,
  mark INT NOT NULL,
  FOREIGN KEY (student_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE messages (
  id INT PRIMARY KEY AUTO_INCREMENT,
  from_user_id INT NOT NULL,
  to_user_id INT NOT NULL,
  message_text TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE announcements (
  id INT PRIMARY KEY AUTO_INCREMENT,
  author_user_id INT NOT NULL,
  target_role ENUM('student','mentor','admin','all') NOT NULL DEFAULT 'all',
  message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE mentoring_sessions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  mentor_user_id INT NOT NULL,
  student_user_id INT NOT NULL,
  session_date DATE NOT NULL,
  session_time VARCHAR(15) NOT NULL,
  FOREIGN KEY (mentor_user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (student_user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO users(name,username,password,role) VALUES
('Asha Rao','stu01','student123','student'),
('Rohan Das','stu02','student123','student'),
('Dr. Neha Sharma','men01','mentor123','mentor'),
('System Administrator','adm01','admin123','admin');

INSERT INTO mentor_student_assignments(mentor_user_id,student_user_id) VALUES (3,1),(3,2);

INSERT INTO attendance(student_user_id,attendance_percent) VALUES (1,92),(2,84);

INSERT INTO marks(student_user_id,subject,mark) VALUES
(1,'Math',86),(1,'Science',90),(1,'English',82),
(2,'Math',74),(2,'Science',79),(2,'English',88);

INSERT INTO messages(from_user_id,to_user_id,message_text) VALUES
(1,3,'Need help with exam preparation strategy.'),
(3,2,'Please revise chapter 3 before Friday session.');

INSERT INTO announcements(author_user_id,target_role,message) VALUES
(3,'student','Weekly mentoring session on Friday at 4:00 PM.'),
(4,'all','Profile update window closes this Sunday.');

INSERT INTO mentoring_sessions(mentor_user_id,student_user_id,session_date,session_time) VALUES
(3,1,'2026-03-02','16:00'),
(3,2,'2026-03-03','15:30');

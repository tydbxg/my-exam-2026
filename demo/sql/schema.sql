DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;

CREATE TABLE courses (
    course_id VARCHAR(20) PRIMARY KEY COMMENT '课程ID',
    course_name VARCHAR(50) NOT NULL COMMENT '课程名称',
    course_type VARCHAR(20) NOT NULL COMMENT '课程类型',
    capacity INT NOT NULL COMMENT '课程容量'
) COMMENT='课程表';

CREATE TABLE enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '选课记录ID',
    student_id VARCHAR(20) NOT NULL COMMENT '学生ID',
    course_id VARCHAR(20) NOT NULL COMMENT '课程ID',
    enroll_time DATETIME NOT NULL COMMENT '选课时间',
    CONSTRAINT fk_enrollments_courses
        FOREIGN KEY (course_id) REFERENCES courses (course_id)
) COMMENT='选课记录表';

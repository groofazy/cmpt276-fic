package com.group14.fic_attendance_tracker.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="courses")
public class Course {
    public enum CourseSubject {
        ACMA, ALS, APMA, ARAB, ARCH, ASC, BISC, BOT, BPK, BUEC, BUS, CA, CENV, 
        CHEM, CHIN, CMNS, CMPT, COGS, CRIM, DATA, DDP, DEVS, DIAL, DMED, EAS, 
        EASC, EBP, ECO, ECON, EDPR, EDUC, ENGL, ENSC, ENV, EVSC, EXCH, EXPL, 
        FAL, FAN, FASS, FEP, FNLG, FNST, FPA, FREN, GA, GDST, GEOG, GERM, GERO, 
        GLP, GRAD, GRK, GS, GSWS, HIST, HS, HSCI, HUM, IAT, INDG, INLG, INS, 
        IS, ISPO, ITAL, JAPN, KIN, LANG, LAS, LBRL, LBST, LING, LS, MACM, MASC, 
        MATH, MBB, MIL, MPP, MSE, MSSC, NEUR, NUSC, ONC, PERS, PHIL, PHYS, 
        PLAN, PLCY, POL, PORT, PSYC, PUB, PUNJ, REM, RISK, SA, SAR, SCD, SCI, 
        SD, SDA, SEE, SPAN, STAT, TECH, TEKX, TRSS, UGRAD, URB, WDA, WKTM, WL, WS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="course_id")
    private int courseId;

    @Column(name="admin_id")
    private int adminId;

    @Column(name="subject")
    @Enumerated(EnumType.STRING)
    private CourseSubject subject;

    @Column(name="course_num")
    private String courseNum;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "course_times", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "time")
    private List<String> courseTimes;

    // Constructor for Course object
    public Course() {
    }

    public Course(int adminId, CourseSubject subject, String courseNum, List<String> courseTimes){
        this.adminId = adminId;
        this.subject = subject;
        this.courseNum = courseNum;
        if (courseTimes != null) {
            this.courseTimes = courseTimes;
        }
    }

    public int getCourseId(){
        return courseId;
    }
    public void setCourseId(int courseId){
        this.courseId = courseId;
    }

    public int getAdminId(){
        return adminId;
    }
    public void setAdminId(int adminId){
        this.adminId = adminId;
    }

    public CourseSubject getCourseSubject() {
        return subject;
    }
    public void setCourseSubject(CourseSubject subject) {
        this.subject = subject;
    }

    public String getCourseNum(){
        return courseNum;
    }
    public void setCourseNum(String courseNum){
        this.courseNum = courseNum;
    }

    public List<String> getCourseTimes() {
        return courseTimes;
    }
    public void setCourseTimes(List<String> courseTimes) {
        this.courseTimes = courseTimes;
    }
}

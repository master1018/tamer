public class StudentGroupStudent {
    public Long getId() {
        return id;
    }
    protected void setStudentGroup(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }
    public StudentGroup getStudentGroup() {
        return studentGroup;
    }
    public void setStudent(Student student) {
        this.student = student;
    }
    public Student getStudent() {
        return student;
    }
    @SuppressWarnings("unused")
    private void setVersion(Long version) {
        this.version = version;
    }
    public Long getVersion() {
        return version;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "StudentGroupStudent")
    @TableGenerator(name = "StudentGroupStudent", allocationSize = 1, table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "sequence_next_hi_value")
    @DocumentId
    private Long id;
    @OneToOne
    @JoinColumn(name = "studentGroup")
    private StudentGroup studentGroup;
    @ManyToOne
    @JoinColumn(name = "student")
    private Student student;
    @Version
    @Column(nullable = false)
    private Long version;
}

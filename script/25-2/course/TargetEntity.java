@Entity
@Table(name = "target")
class TargetEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(nullable = false)
        val departmentId: Long,

        @Column(nullable = false)
        val courseId: Long,

        @Column(nullable = false)
        val grade: Int,
        )

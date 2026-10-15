package com.example.delominds.data.repository

import com.example.delominds.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeloMindsRepository {

  private val _currentUser = MutableStateFlow(
    UserAccount(
      id = "USR-004",
      name = "Mutupira",
      email = "mutupira@delominds.creatvate.org",
      phone = "+263 77 123 4567",
      role = UserRole.TEACHER,
      assignedSchoolIds = listOf("SCH-001"),
      assignedGradeGroups = listOf("part_d")
    )
  )
  val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

  val availableAccounts = listOf(
    UserAccount(
      id = "USR-001",
      name = "Super Admin (Head Office)",
      email = "superadmin@delominds.creatvate.org",
      phone = "+263 77 000 0001",
      role = UserRole.SUPER_ADMIN
    ),
    UserAccount(
      id = "USR-002",
      name = "Programme Admin",
      email = "admin@delominds.creatvate.org",
      phone = "+263 77 000 0002",
      role = UserRole.ADMIN
    ),
    UserAccount(
      id = "USR-003",
      name = "Tariro Chimuka (Riverside)",
      email = "tariro@riverside.ac.zw",
      phone = "+263 77 000 0003",
      role = UserRole.SCHOOL_ADMIN,
      assignedSchoolIds = listOf("SCH-001")
    ),
    UserAccount(
      id = "USR-004",
      name = "Mutupira (Robotics Lead)",
      email = "mutupira@delominds.creatvate.org",
      phone = "+263 77 123 4567",
      role = UserRole.TEACHER,
      assignedSchoolIds = listOf("SCH-001"),
      assignedGradeGroups = listOf("part_d")
    )
  )

  private val _schools = MutableStateFlow<List<School>>(emptyList())
  val schools: StateFlow<List<School>> = _schools.asStateFlow()

  private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
  val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

  private val _assignments = MutableStateFlow<List<TeacherAssignment>>(emptyList())
  val assignments: StateFlow<List<TeacherAssignment>> = _assignments.asStateFlow()

  private val _curriculumParts = MutableStateFlow<List<CurriculumPart>>(emptyList())
  val curriculumParts: StateFlow<List<CurriculumPart>> = _curriculumParts.asStateFlow()

  private val _curriculumWeeks = MutableStateFlow<List<CurriculumWeek>>(emptyList())
  val curriculumWeeks: StateFlow<List<CurriculumWeek>> = _curriculumWeeks.asStateFlow()

  private val _followUpSubmissions = MutableStateFlow<List<FollowUpSubmission>>(emptyList())
  val followUpSubmissions: StateFlow<List<FollowUpSubmission>> = _followUpSubmissions.asStateFlow()

  private val _students = MutableStateFlow<List<Student>>(emptyList())
  val students: StateFlow<List<Student>> = _students.asStateFlow()

  private val _payments = MutableStateFlow<List<Payment>>(emptyList())
  val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

  private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
  val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

  private val _expoCriteria = MutableStateFlow<List<ExpoRubricCriterion>>(emptyList())
  val expoCriteria: StateFlow<List<ExpoRubricCriterion>> = _expoCriteria.asStateFlow()

  private val _expoEvaluations = MutableStateFlow<List<ExpoEvaluation>>(emptyList())
  val expoEvaluations: StateFlow<List<ExpoEvaluation>> = _expoEvaluations.asStateFlow()

  private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
  val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

  private val _systemSettings = MutableStateFlow(SystemSettings())
  val systemSettings: StateFlow<SystemSettings> = _systemSettings.asStateFlow()

  private val _scheduledLessons = MutableStateFlow<List<ScheduledLesson>>(emptyList())
  val scheduledLessons: StateFlow<List<ScheduledLesson>> = _scheduledLessons.asStateFlow()

  init {
    seedInitialData()
  }

  fun switchUser(account: UserAccount) {
    _currentUser.value = account
    addAuditEntry(
      user = account.name,
      action = "Sign-in",
      record = account.email,
      details = "Logged in as ${account.role.displayName}"
    )
  }

  /**
   * Replaces the seeded sample data with what the server actually knows, read from the cached
   * snapshot (data/sync/OfflineStore). Call this once at launch with whatever is cached (even
   * offline), and again after every successful sync.
   *
   * Server-confirmed records always win. A school/teacher/student/payment the admin added while
   * offline keeps showing up (via SnapshotMapper.mergeKeepingUnsynced) until the next sync
   * confirms it, so adding something offline never makes it disappear from the list.
   *
   * A section the snapshot does not include at all (null from the mapper) is left as-is, so a
   * narrower teacher snapshot does not blank out sections only the admin snapshot carries.
   */
  fun loadFromServerSnapshot(json: JSONObject) {
    SnapshotMapper.account(json)?.let { _currentUser.value = it }

    SnapshotMapper.schools(json)?.let { server ->
      _schools.value = SnapshotMapper.mergeKeepingUnsynced(server, _schools.value) { it.schoolCode }
    }
    SnapshotMapper.teachers(json)?.let { server ->
      _teachers.value = SnapshotMapper.mergeKeepingUnsynced(server, _teachers.value) { it.email }
    }
    SnapshotMapper.assignments(json)?.let { server ->
      _assignments.value = SnapshotMapper.mergeKeepingUnsynced(server, _assignments.value) { "${it.teacherId}|${it.schoolId}|${it.grade}" }
    }
    SnapshotMapper.curriculumParts(json)?.let { _curriculumParts.value = it }
    SnapshotMapper.curriculumWeeks(json)?.let { _curriculumWeeks.value = it }
    SnapshotMapper.students(json)?.let { server ->
      _students.value = SnapshotMapper.mergeKeepingUnsynced(server, _students.value) { it.studentId }
    }
    SnapshotMapper.payments(json)?.let { server ->
      _payments.value = SnapshotMapper.mergeKeepingUnsynced(server, _payments.value) { it.receiptNumber }
    }
    SnapshotMapper.followUpSubmissions(json)?.let { server ->
      _followUpSubmissions.value = SnapshotMapper.mergeKeepingUnsynced(server, _followUpSubmissions.value) { "${it.teacherId}|${it.schoolId}|${it.grade}|${it.weekNumber}" }
    }
    SnapshotMapper.notifications(json)?.let { _notifications.value = it }
    SnapshotMapper.auditLogs(json)?.let { _auditLogs.value = it }
    SnapshotMapper.settings(json)?.let { _systemSettings.value = it }
  }

private fun seedInitialData() {
    _schools.value = listOf(
      School(
        id = "SCH-001",
        schoolCode = "RIV-01",
        name = "Riverside Primary School",
        location = "Harare North",
        address = "12 Riverside Way, Harare",
        phone = "+263 242 789012",
        email = "admin@riverside.ac.zw",
        principal = "Mrs. M. Chikwanha",
        contactPerson = "Mr. D. Marange",
        contactPhone = "+263 77 456 7890",
        contactEmail = "dmarange@riverside.ac.zw",
        gradesOffered = listOf("Grade 1", "Grade 2", "Grade 6", "Grade 7"),
        studentCount = 142,
        teacherCount = 3,
        coveragePercent = 86,
        pendingFollowUps = 2,
        programmeStartDate = "15 Jan 2026",
        notes = "Full Robotics lab with 20 Delo Minds Starter Kits."
      ),
      School(
        id = "SCH-002",
        schoolCode = "HLD-02",
        name = "Highlands Academy",
        location = "Highlands, Harare",
        address = "45 Enterprise Road, Highlands",
        phone = "+263 242 498123",
        email = "info@highlandsacademy.co.zw",
        principal = "Dr. K. Nyoni",
        contactPerson = "Ms. S. Moyo",
        contactPhone = "+263 71 234 5678",
        contactEmail = "smoyo@highlandsacademy.co.zw",
        gradesOffered = listOf("Grade 1", "Grade 2", "Grade 3"),
        studentCount = 98,
        teacherCount = 2,
        coveragePercent = 78,
        pendingFollowUps = 3,
        programmeStartDate = "20 Jan 2026",
        notes = "Expanding to Grades 4-5 next term."
      ),
      School(
        id = "SCH-003",
        schoolCode = "STP-03",
        name = "St. Peter's College",
        location = "Gweru Central",
        address = "8 College Avenue, Gweru",
        phone = "+263 54 223456",
        email = "office@stpeters.ac.zw",
        principal = "Rev. T. Gumbo",
        contactPerson = "Mr. P. Sibanda",
        contactPhone = "+263 73 345 6789",
        contactEmail = "psibanda@stpeters.ac.zw",
        gradesOffered = listOf("Grade 4", "Grade 5", "Grade 6"),
        studentCount = 115,
        teacherCount = 2,
        coveragePercent = 94,
        pendingFollowUps = 2,
        programmeStartDate = "12 Jan 2026",
        notes = "National Innovation Expo finalist school."
      )
    )

    _teachers.value = listOf(
      Teacher(
        id = "TCH-001",
        name = "Mutupira",
        email = "mutupira@delominds.creatvate.org",
        phone = "+263 77 123 4567",
        address = "Harare, Zimbabwe",
        status = "Active",
        pendingFollowUps = 2,
        completedFollowUps = 14,
        coveragePercent = 83
      ),
      Teacher(
        id = "TCH-002",
        name = "Sibanda",
        email = "sibanda@delominds.creatvate.org",
        phone = "+263 77 234 5678",
        address = "Bulawayo, Zimbabwe",
        status = "Active",
        pendingFollowUps = 3,
        completedFollowUps = 12,
        coveragePercent = 78
      ),
      Teacher(
        id = "TCH-003",
        name = "Ndlovu",
        email = "ndlovu@delominds.creatvate.org",
        phone = "+263 77 345 6789",
        address = "Gweru, Zimbabwe",
        status = "Active",
        pendingFollowUps = 2,
        completedFollowUps = 16,
        coveragePercent = 92
      )
    )

    _assignments.value = listOf(
      TeacherAssignment(
        id = "ASN-001",
        teacherId = "TCH-001",
        teacherName = "Mutupira",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        gradeGroupId = "part_d",
        grade = "Grade 6",
        className = "Grade 6 Red",
        coveragePercent = 83,
        weeklyCoverage = mapOf(1 to 100, 2 to 83, 3 to 100, 4 to 100, 5 to 67, 6 to 100, 7 to 83, 8 to null)
      ),
      TeacherAssignment(
        id = "ASN-002",
        teacherId = "TCH-001",
        teacherName = "Mutupira",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        gradeGroupId = "part_d",
        grade = "Grade 7",
        className = "Grade 7 Blue",
        coveragePercent = 85,
        weeklyCoverage = mapOf(1 to 100, 2 to 100, 3 to 83, 4 to 100, 5 to 83, 6 to 100, 7 to null, 8 to null)
      ),
      TeacherAssignment(
        id = "ASN-003",
        teacherId = "TCH-002",
        teacherName = "Sibanda",
        schoolId = "SCH-002",
        schoolName = "Highlands Academy",
        gradeGroupId = "part_b",
        grade = "Grade 1",
        className = "Grade 1 Eagles",
        coveragePercent = 80,
        weeklyCoverage = mapOf(1 to 100, 2 to 67, 3 to 100, 4 to 100, 5 to 67, 6 to 100, 7 to null, 8 to null)
      ),
      TeacherAssignment(
        id = "ASN-004",
        teacherId = "TCH-003",
        teacherName = "Ndlovu",
        schoolId = "SCH-003",
        schoolName = "St. Peter's College",
        gradeGroupId = "part_c",
        grade = "Grade 4",
        className = "Grade 4 Alpha",
        coveragePercent = 94,
        weeklyCoverage = mapOf(1 to 100, 2 to 100, 3 to 100, 4 to 83, 5 to 100, 6 to 100, 7 to 100, 8 to null)
      )
    )

    _curriculumParts.value = listOf(
      CurriculumPart(
        id = "part_a",
        code = "Part A",
        title = "Early Childhood Development (ECD-A & ECD-B)",
        gradeRange = "ECD-A & ECD-B",
        colorHex = 0xFFFF9800, // Orange
        totalWeeks = 36,
        coveragePercent = 71
      ),
      CurriculumPart(
        id = "part_b",
        code = "Part B",
        title = "Junior Primary (Grades 1–3)",
        gradeRange = "Grades 1–3",
        colorHex = 0xFF4CAF50, // Green
        totalWeeks = 36,
        coveragePercent = 82
      ),
      CurriculumPart(
        id = "part_c",
        code = "Part C",
        title = "Middle Primary (Grades 4–5)",
        gradeRange = "Grades 4–5",
        colorHex = 0xFF2196F3, // Blue
        totalWeeks = 36,
        coveragePercent = 94
      ),
      CurriculumPart(
        id = "part_d",
        code = "Part D",
        title = "Senior Primary (Grades 6–7)",
        gradeRange = "Grades 6–7",
        colorHex = 0xFFAB47BC, // Purple
        totalWeeks = 36,
        coveragePercent = 88
      )
    )

    // Seed textbook weeks
    _curriculumWeeks.value = listOf(
      CurriculumWeek(
        partId = "part_d",
        weekNumber = 8,
        term = 1,
        topic = "Autonomous Obstacle Avoidance & Sensor Integration",
        learningObjectives = listOf(
          "Explain ultrasonic pulse-echo distance calculations (time to distance).",
          "Assemble an ultrasonic sensor (HC-SR04) on a mini servo sweep mount.",
          "Design Education 5.0 industrial Automated Guided Vehicle (AGV) collision prevention logic."
        ),
        bigIdea = "Machines sense their surroundings by emitting waves and calculating response delays, forming the foundation of autonomous navigation.",
        keyWords = listOf("Ultrasonic", "Pulse Width", "Echo", "Servo Sweep", "AGV", "Threshold"),
        day1CoreConcept = "Ultrasonic sensors operate using piezoelectric sound transceivers. The Trig pin sends a 10µs ultrasonic burst at 40kHz. The Echo pin stays HIGH until sound bounces back. Distance = (Echo duration * 0.0343 cm/µs) / 2. Microcontroller state machines compare this distance to safety thresholds.",
        day2HandsOn = "Mount the HC-SR04 ultrasonic bracket onto the front servo gear. Wire Trig to D9, Echo to D10, VCC to 5V, GND to GND. Program the servo to sweep 45° (left), 90° (center), and 135° (right) and verify serial monitor readings when barriers approach within 15cm.",
        day3Application = "Connect distance data to motor brake routines. Prototyping an industrial mine-cart collision avoidance system (Education 5.0 local mining safety application).",
        workedExamples = "Example: If echo pulse is 588µs, Distance = (588 * 0.0343)/2 = 10.08 cm. Safe threshold is 15cm, so trigger vehicle reverse.",
        troubleshooting = "If Echo always reports 0cm: Check loose breadboard ground wires and ensure 5V rail is shared with microcontroller.",
        gradeDifferentiation = "Grade 6: Focus on single forward-facing reading threshold stop. Grade 7: Implement 3-way servo sweep and path choosing algorithm.",
        selfCheck = "Can the student calculate distance from a given microsecond pulse duration? Did the vehicle stop before hitting an obstacle?",
        sketchNotes = "Wiring diagram of Microcontroller + HC-SR04 + Servo Shield + L298N Motor Driver.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      CurriculumWeek(
        partId = "part_d",
        weekNumber = 7,
        term = 1,
        topic = "H-Bridge DC Motor Controllers & Pulse Width Modulation",
        learningObjectives = listOf(
          "Understand how an H-bridge inverts polarity to reverse DC motor rotation.",
          "Use PWM (Pulse Width Modulation) duty cycle to control motor RPM.",
          "Connect external battery pack to motor driver with shared ground."
        ),
        bigIdea = "Microcontrollers cannot directly power heavy motors; dedicated driver circuits translate low-current control signals into high-current motor power.",
        keyWords = listOf("H-Bridge", "PWM", "Duty Cycle", "L298N", "Torque", "Polarity"),
        day1CoreConcept = "The H-Bridge circuit uses 4 switching transistors to control current direction across a DC motor coil. By pulsing the enable pin using PWM (0-255), we vary effective voltage to change speed.",
        day2HandsOn = "Wire L298N motor driver to 2 geared DC motors. Connect IN1/IN2 to control Left motor and IN3/IN4 for Right motor. Test differential steering (forward, turn left, turn right, spin, reverse).",
        day3Application = "Simulate an agricultural irrigation crawler moving along crop furrows at controlled variable speed.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      CurriculumWeek(
        partId = "part_b",
        weekNumber = 8,
        term = 1,
        topic = "Simple Mechanical Linkages & Robotic Grippers",
        learningObjectives = listOf(
          "Identify mechanical levers, fulcrums, and parallel linkages.",
          "Build a working two-finger mechanical claw from laser-cut plywood and fasteners.",
          "Test lifting everyday classroom objects of different shapes and weights."
        ),
        bigIdea = "Levers and linkages allow small input motions to exert precise grip force on distant objects.",
        keyWords = listOf("Linkage", "Fulcrum", "Gripper", "Effort", "Pivot", "Tension"),
        day1CoreConcept = "A four-bar linkage translates linear pull force into opening and closing gripper jaws. The pivot point determines mechanical advantage.",
        day2HandsOn = "Assemble the Delo Minds mechanical claw. Attach rubber grip bands to claw tips to increase friction. Connect pull string or syringe plunger.",
        day3Application = "Robotic arm harvest assistance: grasping fruits without bruising them.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      CurriculumWeek(
        partId = "part_c",
        weekNumber = 8,
        term = 1,
        topic = "Microcontroller Light Dependent Resistors (LDR) & Smart Lighting",
        learningObjectives = listOf(
          "Measure ambient illumination using an analog photoresistor (LDR).",
          "Program an analog read threshold to toggle LED illuminators automatically.",
          "Design a solar-powered street lighting controller for rural school pathways."
        ),
        bigIdea = "Sensors transform physical phenomena like light into electrical resistance, enabling automated environmental response.",
        keyWords = listOf("LDR", "AnalogRead", "Threshold", "Streetlight", "Voltage Divider", "Automation"),
        day1CoreConcept = "Photoresistor resistance drops as ambient light increases. In a voltage divider circuit with a 10kΩ resistor, the analog pin reads between 0 (pitch dark) and 1023 (bright daylight).",
        day2HandsOn = "Construct the LDR voltage divider on a mini breadboard. Code an IF condition: IF analogRead(A0) < 300 THEN turn ON High-Power White LED.",
        day3Application = "Automated school energy conservation: pathway lights turn on only when dusk falls, saving solar battery storage.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      CurriculumWeek(
        partId = "part_a",
        weekNumber = 8,
        term = 1,
        topic = "Colour Recognition & Directional Coding Tiles",
        learningObjectives = listOf(
          "Match colored coding blocks with robot movement directions.",
          "Direct a floor robot through a mat maze using sequential picture cards.",
          "Develop early computational thinking and spatial sequencing."
        ),
        bigIdea = "Robots follow steps in exact order; changing the card changes where the robot travels.",
        keyWords = listOf("Forward", "Backward", "Turn Left", "Turn Right", "Sequence", "Goal"),
        day1CoreConcept = "Sequencing story: The robot bee needs nectar from the yellow flower. What 3 steps must it take first?",
        day2HandsOn = "Children place sequence cards on the floor grid (Forward, Forward, Turn Right, Forward) and press the robot buttons to watch it navigate.",
        day3Application = "Community mapping: Guiding the robot safely from Home to School avoiding the river.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      // Part D Extra Weeks
      CurriculumWeek(
        partId = "part_d",
        weekNumber = 1,
        term = 1,
        topic = "Introduction to Robotics, Lab Safety & Microcontrollers",
        learningObjectives = listOf("Define robotics and Education 5.0", "Inspect breadboards, jumpers and ESD precautions", "Setup serial communication"),
        bigIdea = "Robotics combines sensing, computing, and physical actuation.",
        keyWords = listOf("Robotics", "Safety", "Microcontroller", "Actuator", "Sensor"),
        day1CoreConcept = "Introduction to the anatomy of an autonomous robot: power systems, logic controllers, sensor inputs, and mechanical actuators. Safety guidelines with LiPo batteries.",
        day2HandsOn = "Breadboard layout exercises: LED current-limiting resistor wiring and uploading the initial Blink sketch.",
        day3Application = "Analyzing how industrial robots are transforming manufacturing and agriculture across Zimbabwe.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      CurriculumWeek(
        partId = "part_d",
        weekNumber = 2,
        term = 1,
        topic = "Electrical Circuits, Voltage Dividers & Power Storage",
        learningObjectives = listOf("Apply Ohm's law V=IR", "Calculate voltage drop across series resistors", "Measure battery voltage with multimeter"),
        bigIdea = "Voltage represents electrical pressure; managing current prevents component overload.",
        keyWords = listOf("Voltage", "Current", "Resistance", "Multimeter", "Ground"),
        day1CoreConcept = "Ohm's Law principles. Understanding voltage regulation from 9V battery packs down to 5V and 3.3V logic rails.",
        day2HandsOn = "Constructing series and parallel resistor circuits and taking live multimeter measurements.",
        day3Application = "Solar micro-grid battery charging systems for rural schools and clinics.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      CurriculumWeek(
        partId = "part_d",
        weekNumber = 3,
        term = 1,
        topic = "Digital Inputs, Pushbuttons & Debounce Algorithms",
        learningObjectives = listOf("Connect pull-up/pull-down pushbutton inputs", "Understand mechanical contact bounce", "Write software debounce state code"),
        bigIdea = "Switches create mechanical vibrations that microcontrollers interpret as multiple false button presses unless filtered.",
        keyWords = listOf("DigitalRead", "Debounce", "Pull-up", "State Machine", "Noise"),
        day1CoreConcept = "Logic level transitions between 0V (LOW) and 5V (HIGH). Floating input pins and the internal pull-up resistor.",
        day2HandsOn = "Wiring dual tactile switches to digital pins D2 and D3 with software millis() debounce.",
        day3Application = "Emergency stop safety switches on industrial milling machines.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      // Part B Extra Weeks
      CurriculumWeek(
        partId = "part_b",
        weekNumber = 1,
        term = 1,
        topic = "What is a Robot? Machines, Senses and Power",
        learningObjectives = listOf("Differentiate between automated robots and simple tools", "Identify how robots use sensors", "Explore safe battery handling"),
        bigIdea = "A robot is a machine that can sense its environment, think, and take action.",
        keyWords = listOf("Robot", "Sense", "Think", "Act", "Battery"),
        day1CoreConcept = "Comparing human senses (eyes, ears, skin) to robot sensors (cameras, microphones, distance sensors).",
        day2HandsOn = "Classroom sorting activity: categorize real-world machines into Robots vs Manual Tools.",
        day3Application = "Automated tractors that help farmers harvest maize in rainfed fields.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      CurriculumWeek(
        partId = "part_b",
        weekNumber = 2,
        term = 1,
        topic = "Simple Electric Circuits & Light Emitting Diodes",
        learningObjectives = listOf("Form complete closed electrical loops", "Identify LED positive and negative legs", "Build a pocket flashlight"),
        bigIdea = "Electricity only flows through unbroken paths called closed circuits.",
        keyWords = listOf("Circuit", "Battery", "LED", "Switch", "Closed Loop"),
        day1CoreConcept = "How electrons move from battery negative to positive when a conductive path is complete.",
        day2HandsOn = "Creating copper tape paper circuits with a coin cell battery and surface mount LED.",
        day3Application = "Designing bicycle safety lights for walking to school at dusk.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      ),
      // Part C Extra Weeks
      CurriculumWeek(
        partId = "part_c",
        weekNumber = 1,
        term = 1,
        topic = "Introduction to Microcontrollers & Digital Logic",
        learningObjectives = listOf("Understand the role of the CPU brain", "Identify digital pins D0 through D13", "Write first LED blinking pattern"),
        bigIdea = "Code is a recipe of precise instructions written by humans for machines.",
        keyWords = listOf("Microcontroller", "Digital", "Pin", "Upload", "Blink"),
        day1CoreConcept = "How microcontrollers execute code in a continuous loop at millions of instructions per second.",
        day2HandsOn = "Plugging microcontroller into USB and programming rhythmically pulsing dual traffic signal LEDs.",
        day3Application = "Smart urban pedestrian crossings in Harare CBD.",
        checkpointQuestions = listOf(
          "Did you cover the Core Concept & Theory section?",
          "Did you conduct the Hands-on Building activity?",
          "Did you cover the Education 5.0 Application?"
        )
      )
    )

    // Seed completed follow-up records
    _followUpSubmissions.value = listOf(
      FollowUpSubmission(
        id = "FUP-001",
        teacherId = "TCH-001",
        teacherName = "Mutupira",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 6",
        weekNumber = 1,
        topic = "Introduction to Robotics & Lab Safety Protocol",
        submissionDate = "19 Jan 2026",
        answers = mapOf(1 to true, 2 to true, 3 to true),
        coveredCount = 3,
        notCoveredCount = 0,
        coveragePercentage = 100
      ),
      FollowUpSubmission(
        id = "FUP-002",
        teacherId = "TCH-001",
        teacherName = "Mutupira",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 6",
        weekNumber = 2,
        topic = "Electrical Circuits & Power Storage Components",
        submissionDate = "26 Jan 2026",
        answers = mapOf(1 to true, 2 to true, 3 to false),
        coveredCount = 2,
        notCoveredCount = 1,
        coveragePercentage = 67
      ),
      FollowUpSubmission(
        id = "FUP-003",
        teacherId = "TCH-001",
        teacherName = "Mutupira",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 6",
        weekNumber = 3,
        topic = "Microcontroller Architecture & Digital I/O Pins",
        submissionDate = "02 Feb 2026",
        answers = mapOf(1 to true, 2 to true, 3 to true),
        coveredCount = 3,
        notCoveredCount = 0,
        coveragePercentage = 100
      )
    )

    // Seed Students
    _students.value = listOf(
      Student(
        id = "STU-001",
        studentId = "STU-0001",
        fullName = "Chipo Marufu",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        className = "Grade 1 Eagles",
        guardianName = "Mrs. Sarah Marufu",
        guardianPhone = "+263 77 111 2233",
        attendancePresent = 20,
        attendanceLate = 1,
        attendanceAbsent = 0,
        expoScore = 21
      ),
      Student(
        id = "STU-002",
        studentId = "STU-0002",
        fullName = "Tino Moyo",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        className = "Grade 1 Eagles",
        guardianName = "Mr. Tendai Moyo",
        guardianPhone = "+263 77 222 3344",
        attendancePresent = 19,
        attendanceLate = 2,
        attendanceAbsent = 0,
        expoScore = 23
      ),
      Student(
        id = "STU-003",
        studentId = "STU-0003",
        fullName = "Farai Dube",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        className = "Grade 1 Eagles",
        guardianName = "Grace Dube",
        guardianPhone = "+263 77 333 4455",
        attendancePresent = 17,
        attendanceLate = 1,
        attendanceAbsent = 3,
        expoScore = 18
      ),
      Student(
        id = "STU-004",
        studentId = "STU-0004",
        fullName = "Rudo Ncube",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        className = "Grade 1 Eagles",
        guardianName = "Patrick Ncube",
        guardianPhone = "+263 77 444 5566",
        attendancePresent = 18,
        attendanceLate = 2,
        attendanceAbsent = 1,
        expoScore = 20
      ),
      Student(
        id = "STU-005",
        studentId = "STU-0005",
        fullName = "Kudzai Sithole",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 6",
        className = "Grade 6 Red",
        guardianName = "Ethel Sithole",
        guardianPhone = "+263 77 555 6677",
        attendancePresent = 21,
        attendanceLate = 0,
        attendanceAbsent = 0,
        expoScore = 22
      ),
      Student(
        id = "STU-006",
        studentId = "STU-0006",
        fullName = "Tanaka Mpofu",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 6",
        className = "Grade 6 Red",
        guardianName = "Simba Mpofu",
        guardianPhone = "+263 77 666 7788",
        attendancePresent = 19,
        attendanceLate = 2,
        attendanceAbsent = 0,
        expoScore = 24
      )
    )

    // Seed Payments
    _payments.value = listOf(
      Payment(
        id = "PAY-001",
        receiptNumber = "DM-2026-000001",
        studentId = "STU-002",
        studentName = "Tino Moyo",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        month = "October 2026",
        amount = 10.00,
        paymentDate = "10 Oct 2026",
        paymentMethod = "EcoCash",
        status = PaymentStatus.PAID,
        notes = "Term 1 programme fee cleared."
      ),
      Payment(
        id = "PAY-002",
        receiptNumber = "DM-2026-000002",
        studentId = "STU-001",
        studentName = "Chipo Marufu",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        month = "October 2026",
        amount = 10.00,
        paymentDate = "08 Oct 2026",
        paymentMethod = "Cash",
        status = PaymentStatus.PAID,
        notes = "Receipt handed to parent."
      ),
      Payment(
        id = "PAY-003",
        receiptNumber = "DM-2026-000003",
        studentId = "STU-003",
        studentName = "Farai Dube",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        month = "October 2026",
        amount = 5.00,
        paymentDate = "12 Oct 2026",
        paymentMethod = "EcoCash",
        status = PaymentStatus.PARTIAL,
        notes = "Balance $5.00 promised by 20th Oct."
      ),
      Payment(
        id = "PAY-004",
        receiptNumber = "DM-2026-000004",
        studentId = "STU-004",
        studentName = "Rudo Ncube",
        schoolId = "SCH-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        month = "October 2026",
        amount = 10.00,
        paymentDate = "",
        paymentMethod = "Pending",
        status = PaymentStatus.PENDING,
        notes = "Awaiting bank clearance."
      )
    )

    // Seed Expo Rubric Criteria (6 criteria from textbook)
    _expoCriteria.value = listOf(
      ExpoRubricCriterion(
        criterionNumber = 1,
        title = "Problem Definition & Education 5.0 Relevance",
        description = "Clarity in identifying a real Zimbabwean community/industrial challenge addressed by the robotics prototype.",
        level1 = "Problem vague or unrelated to community needs (1 pt)",
        level2 = "Problem stated but lacks clear technological link (2 pts)",
        level3 = "Well-defined community problem with practical tech relevance (3 pts)",
        level4 = "Exemplary Education 5.0 alignment with clear local heritage/industrial impact (4 pts)"
      ),
      ExpoRubricCriterion(
        criterionNumber = 2,
        title = "Mechanical Construction & Chassis Durability",
        description = "Structural stability, gear alignment, motor mounts, and mechanical linkage design.",
        level1 = "Fragile construction; falls apart under operation (1 pt)",
        level2 = "Functional but lacks neatness or robust fastening (2 pts)",
        level3 = "Solid chassis with proper wheel/sensor alignment (3 pts)",
        level4 = "Engineered precision chassis with durable linkages and optimal weight balance (4 pts)"
      ),
      ExpoRubricCriterion(
        criterionNumber = 3,
        title = "Circuits, Wiring & Power Management",
        description = "Neat breadboard/PCB wiring, shared grounding, voltage regulation, and component safety.",
        level1 = "Short-circuit risks or loose hanging wires (1 pt)",
        level2 = "Operational but tangled wire bundles (2 pts)",
        level3 = "Color-coded tidy wiring with secure connections (3 pts)",
        level4 = "Professional wire routing, power isolation, and safety disconnects (4 pts)"
      ),
      ExpoRubricCriterion(
        criterionNumber = 4,
        title = "Algorithm Logic, Code & Autonomous Execution",
        description = "Proper variable use, state machines, threshold comparisons, and reliable autonomous operation.",
        level1 = "Code errors or requires constant manual intervention (1 pt)",
        level2 = "Simple linear delay code with erratic reactions (2 pts)",
        level3 = "Clean conditional logic responding reliably to sensor inputs (3 pts)",
        level4 = "Modular, commented code with intelligent state machines and edge-case handling (4 pts)"
      ),
      ExpoRubricCriterion(
        criterionNumber = 5,
        title = "Innovation & Original Creativity",
        description = "Uniqueness of idea beyond basic textbook starter builds; creative adaptations.",
        level1 = "Direct copy of basic guide with no alterations (1 pt)",
        level2 = "Minor cosmetic customization (2 pts)",
        level3 = "Clever adaptation for a specific functional objective (3 pts)",
        level4 = "Breakthrough novel invention with demonstrable creative genius (4 pts)"
      ),
      ExpoRubricCriterion(
        criterionNumber = 6,
        title = "Engineering Notebook & Team Presentation",
        description = "Clarity in explaining scientific principles, sketch notes, and answering judges' questions.",
        level1 = "Unable to explain circuit or code functionality (1 pt)",
        level2 = "One student speaks; notebook missing key notes (2 pts)",
        level3 = "Clear group explanation with neat project sketches (3 pts)",
        level4 = "Confident, articulate mastery of robotics theory and thorough engineering documentation (4 pts)"
      )
    )

    // Seed Notifications
    _notifications.value = listOf(
      NotificationItem(
        id = "NOTIF-001",
        title = "Follow-up due",
        message = "Week 8 follow-up for Grade 6 at Riverside Primary is ready for submission.",
        timestamp = "Today, 09:30",
        type = "follow_up"
      ),
      NotificationItem(
        id = "NOTIF-002",
        title = "Payment received",
        message = "Receipt DM-2026-000001 generated for Tino Moyo ($10.00 PAID).",
        timestamp = "Yesterday, 14:15",
        type = "payment"
      ),
      NotificationItem(
        id = "NOTIF-003",
        title = "Monthly Report Ready",
        message = "September 2026 Teacher Performance analytics have been calculated.",
        timestamp = "1 Oct 2026",
        type = "report"
      )
    )

    // Seed Audit Log
    _auditLogs.value = listOf(
      AuditLogEntry(
        id = "AUD-001",
        user = "Mutupira",
        action = "Submitted Follow-up",
        record = "Grade 6, Week 3",
        details = "Coverage 100% (3 covered, 0 not covered)",
        timestamp = "02 Feb 2026, 11:20"
      ),
      AuditLogEntry(
        id = "AUD-002",
        user = "Admin Officer",
        action = "Assigned Teacher",
        record = "TCH-001 -> SCH-001 Grade 6",
        details = "Created assignment for Academic Year 2026",
        timestamp = "15 Jan 2026, 09:00"
      ),
      AuditLogEntry(
        id = "AUD-003",
        user = "Admin Officer",
        action = "Recorded Payment",
        record = "DM-2026-000001",
        details = "USD 10.00 recorded for Tino Moyo (EcoCash)",
        timestamp = "10 Oct 2026, 10:15"
      )
    )

    // Seed scheduled timetable
    _scheduledLessons.value = listOf(
      ScheduledLesson(
        id = "SCHED-001",
        schoolName = "Riverside Primary School",
        grade = "Grade 6",
        teacherName = "Mutupira",
        room = "Robotics Lab 1",
        time = "08:30 - 09:30",
        topic = "Autonomous Obstacle Avoidance & Sensor Integration",
        dayOfWeek = "Tuesday"
      ),
      ScheduledLesson(
        id = "SCHED-002",
        schoolName = "Riverside Primary School",
        grade = "Grade 7",
        teacherName = "Mutupira",
        room = "Tech Suite B",
        time = "10:00 - 11:00",
        topic = "H-Bridge DC Motor Controllers & PWM",
        dayOfWeek = "Tuesday"
      ),
      ScheduledLesson(
        id = "SCHED-003",
        schoolName = "Riverside Primary School",
        grade = "Grade 1",
        teacherName = "Mutupira",
        room = "Activity Hall",
        time = "11:30 - 12:30",
        topic = "Simple Mechanical Linkages & Gripper Claws",
        dayOfWeek = "Wednesday"
      )
    )
  }

  // Follow-up actions
  fun submitFollowUp(
    teacherId: String,
    teacherName: String,
    schoolId: String,
    schoolName: String,
    grade: String,
    weekNumber: Int,
    topic: String,
    answers: Map<Int, Boolean>
  ): FollowUpSubmission {
    val covered = answers.values.count { it }
    val notCovered = answers.values.count { !it }
    val total = covered + notCovered
    val coveragePct = if (total > 0) ((covered.toDouble() / total) * 100).toInt() else 0

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    val newSubmission = FollowUpSubmission(
      id = "FUP-${System.currentTimeMillis() % 100000}",
      teacherId = teacherId,
      teacherName = teacherName,
      schoolId = schoolId,
      schoolName = schoolName,
      grade = grade,
      weekNumber = weekNumber,
      topic = topic,
      submissionDate = currentDate,
      answers = answers,
      coveredCount = covered,
      notCoveredCount = notCovered,
      coveragePercentage = coveragePct,
      isSubmitted = true
    )

    // Prepend to submissions
    _followUpSubmissions.value = listOf(newSubmission) + _followUpSubmissions.value

    // Update assignment weekly coverage
    val currentAssignments = _assignments.value.toMutableList()
    val matchIdx = currentAssignments.indexOfFirst {
      it.teacherId == teacherId && it.schoolId == schoolId && it.grade == grade
    }
    if (matchIdx >= 0) {
      val existing = currentAssignments[matchIdx]
      val updatedCoverage = existing.weeklyCoverage.toMutableMap()
      updatedCoverage[weekNumber] = coveragePct
      currentAssignments[matchIdx] = existing.copy(
        weeklyCoverage = updatedCoverage,
        coveragePercent = ((existing.coveragePercent + coveragePct) / 2)
      )
      _assignments.value = currentAssignments
    }

    addAuditEntry(
      user = teacherName,
      action = "Submitted Follow-up",
      record = "$grade, Week $weekNumber",
      details = "Coverage: $coveragePct% ($covered covered, $notCovered not covered)"
    )

    addNotification(
      title = "Follow-up Submitted",
      message = "$teacherName submitted Week $weekNumber follow-up for $grade ($coveragePct% coverage).",
      type = "follow_up"
    )

    return newSubmission
  }

  fun reopenFollowUp(submissionId: String, adminName: String) {
    val list = _followUpSubmissions.value.map {
      if (it.id == submissionId) it.copy(isSubmitted = false, reopenedByAdmin = true) else it
    }
    _followUpSubmissions.value = list
    addAuditEntry(
      user = adminName,
      action = "Reopened Follow-up",
      record = submissionId,
      details = "Admin reopened follow-up for teacher re-answering."
    )
  }

  // Attendance
  fun saveAttendance(
    schoolId: String,
    schoolName: String,
    grade: String,
    date: String,
    teacherId: String,
    studentStatus: Map<String, AttendanceStatus>
  ) {
    val record = AttendanceRecord(
      id = "ATT-${System.currentTimeMillis() % 100000}",
      schoolId = schoolId,
      schoolName = schoolName,
      grade = grade,
      date = date,
      teacherId = teacherId,
      studentAttendance = studentStatus
    )
    _attendanceRecords.value = listOf(record) + _attendanceRecords.value

    addAuditEntry(
      user = _currentUser.value.name,
      action = "Recorded Attendance",
      record = "$schoolName - $grade ($date)",
      details = "Saved marks for ${studentStatus.size} learners."
    )
  }

  // Payments & Receipts
  fun recordPayment(
    studentId: String,
    studentName: String,
    schoolId: String,
    schoolName: String,
    grade: String,
    month: String,
    amount: Double,
    paymentMethod: String,
    status: PaymentStatus,
    notes: String
  ): Payment {
    val nextNum = String.format(Locale.US, "%06d", _payments.value.size + 1)
    val receiptNum = "DM-2026-$nextNum"
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val paymentDate = if (status == PaymentStatus.PAID || status == PaymentStatus.PARTIAL) dateFormat.format(Date()) else ""

    val payment = Payment(
      id = "PAY-${System.currentTimeMillis() % 100000}",
      receiptNumber = receiptNum,
      studentId = studentId,
      studentName = studentName,
      schoolId = schoolId,
      schoolName = schoolName,
      grade = grade,
      month = month,
      amount = amount,
      paymentDate = paymentDate,
      paymentMethod = paymentMethod,
      status = status,
      notes = notes
    )
    _payments.value = listOf(payment) + _payments.value

    addAuditEntry(
      user = _currentUser.value.name,
      action = "Recorded Payment",
      record = receiptNum,
      details = "USD $amount for $studentName ($status)"
    )

    return payment
  }

  // Add School
  fun addSchool(
    name: String,
    location: String,
    address: String,
    phone: String,
    email: String,
    principal: String,
    gradesOffered: List<String>
  ) {
    val code = "SCH-00${_schools.value.size + 1}"
    val newSchool = School(
      id = code,
      schoolCode = "${name.take(3).uppercase()}-0${_schools.value.size + 1}",
      name = name,
      location = location,
      address = address,
      phone = phone,
      email = email,
      principal = principal,
      contactPerson = principal,
      contactPhone = phone,
      contactEmail = email,
      gradesOffered = gradesOffered,
      studentCount = 0,
      teacherCount = 0,
      coveragePercent = 0,
      pendingFollowUps = 0,
      programmeStartDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    )
    _schools.value = _schools.value + newSchool
    addAuditEntry(
      user = _currentUser.value.name,
      action = "Added School",
      record = name,
      details = "Code: $code, Location: $location"
    )
  }

  // Add Teacher
  fun addTeacher(name: String, email: String, phone: String, address: String) {
    val id = "TCH-00${_teachers.value.size + 1}"
    val newTeacher = Teacher(
      id = id,
      name = name,
      email = email,
      phone = phone,
      address = address,
      status = "Active",
      pendingFollowUps = 0,
      completedFollowUps = 0,
      coveragePercent = 0
    )
    _teachers.value = _teachers.value + newTeacher
    addAuditEntry(
      user = _currentUser.value.name,
      action = "Added Teacher",
      record = name,
      details = "ID: $id, Email: $email"
    )
  }

  // Assign Teacher
  fun assignTeacher(
    teacherId: String,
    teacherName: String,
    schoolId: String,
    schoolName: String,
    gradeGroupId: String,
    grade: String,
    className: String
  ) {
    val id = "ASN-${System.currentTimeMillis() % 100000}"
    val newAssignment = TeacherAssignment(
      id = id,
      teacherId = teacherId,
      teacherName = teacherName,
      schoolId = schoolId,
      schoolName = schoolName,
      gradeGroupId = gradeGroupId,
      grade = grade,
      className = className,
      coveragePercent = 0
    )
    _assignments.value = _assignments.value + newAssignment
    addAuditEntry(
      user = _currentUser.value.name,
      action = "Assigned Teacher",
      record = "$teacherName -> $schoolName",
      details = "Grade: $grade ($className)"
    )
  }

  // Add Student
  fun addStudent(
    fullName: String,
    schoolId: String,
    schoolName: String,
    grade: String,
    className: String,
    guardianName: String,
    guardianPhone: String
  ) {
    val id = "STU-00${_students.value.size + 1}"
    val studentId = "STU-${String.format(Locale.US, "%04d", _students.value.size + 1)}"
    val student = Student(
      id = id,
      studentId = studentId,
      fullName = fullName,
      schoolId = schoolId,
      schoolName = schoolName,
      grade = grade,
      className = className,
      guardianName = guardianName,
      guardianPhone = guardianPhone
    )
    _students.value = _students.value + student
    addAuditEntry(
      user = _currentUser.value.name,
      action = "Added Student",
      record = studentId,
      details = "$fullName ($grade, $schoolName)"
    )
  }

  // Expo Evaluation
  fun saveExpoEvaluation(
    studentId: String,
    studentName: String,
    projectTitle: String,
    term: Int,
    scores: List<Int>,
    notes: String
  ) {
    val total = scores.sum()
    val eval = ExpoEvaluation(
      studentId = studentId,
      studentName = studentName,
      projectTitle = projectTitle,
      term = term,
      scores = scores,
      totalScore = total,
      notes = notes
    )
    _expoEvaluations.value = listOf(eval) + _expoEvaluations.value

    // Update student's expoScore
    _students.value = _students.value.map {
      if (it.id == studentId) it.copy(expoScore = total) else it
    }

    addAuditEntry(
      user = _currentUser.value.name,
      action = "Scored Expo Project",
      record = "$studentName - $projectTitle",
      details = "Total: $total / 24"
    )
  }

  fun updateSettings(newSettings: SystemSettings) {
    _systemSettings.value = newSettings
    addAuditEntry(
      user = _currentUser.value.name,
      action = "Updated Settings",
      record = "System Settings",
      details = "Monthly fee: ${newSettings.currency} ${newSettings.monthlyFee}"
    )
  }

  fun addAuditEntry(user: String, action: String, record: String, details: String) {
    val timeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val entry = AuditLogEntry(
      id = "AUD-${System.currentTimeMillis() % 100000}",
      user = user,
      action = action,
      record = record,
      details = details,
      timestamp = timeFormat.format(Date())
    )
    _auditLogs.value = listOf(entry) + _auditLogs.value
  }

  fun addNotification(title: String, message: String, type: String) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val notif = NotificationItem(
      id = "NOTIF-${System.currentTimeMillis() % 100000}",
      title = title,
      message = message,
      timestamp = "Today, ${timeFormat.format(Date())}",
      type = type
    )
    _notifications.value = listOf(notif) + _notifications.value
  }

  fun markNotificationsAsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
  }

  fun addFollowUpCheckpoint(gradeGroupId: String, weekNumber: Int?, shortLabel: String, question: String) {
    val updatedWeeks = _curriculumWeeks.value.map { week ->
      val matches = (week.partId == gradeGroupId) && (weekNumber == null || week.weekNumber == weekNumber)
      if (matches) {
        week.copy(checkpointQuestions = week.checkpointQuestions + question)
      } else week
    }
    _curriculumWeeks.value = updatedWeeks

    addAuditEntry(
      user = _currentUser.value.name,
      action = "Added Checkpoint",
      record = "$gradeGroupId ${if (weekNumber != null) "Week $weekNumber" else "All Weeks"}",
      details = "Label: $shortLabel • $question"
    )
  }

  fun scheduleLesson(
    schoolName: String,
    grade: String,
    teacherName: String,
    room: String,
    time: String,
    topic: String,
    dayOfWeek: String
  ) {
    val newLesson = ScheduledLesson(
      id = "SCHED-${System.currentTimeMillis() % 100000}",
      schoolName = schoolName,
      grade = grade,
      teacherName = teacherName,
      room = room,
      time = time,
      topic = topic,
      dayOfWeek = dayOfWeek
    )
    _scheduledLessons.value = _scheduledLessons.value + newLesson

    addAuditEntry(
      user = _currentUser.value.name,
      action = "Scheduled Lesson",
      record = "$schoolName - $grade",
      details = "$time in $room with $teacherName"
    )
  }

  fun removeScheduledLesson(lessonId: String) {
    _scheduledLessons.value = _scheduledLessons.value.filter { it.id != lessonId }
  }

  fun importStudentsCsv(csvContent: String): Int {
    var count = 0
    val lines = csvContent.lines().filter { it.isNotBlank() }
    val defaultSchool = _schools.value.firstOrNull() ?: return 0

    lines.drop(1).forEach { line -> // skip header
      val parts = line.split(",").map { it.trim() }
      if (parts.size >= 3) {
        val name = parts[0]
        val grade = parts[2]
        val className = if (parts.size > 3) parts[3] else "$grade Class"
        val guardian = if (parts.size > 4) parts[4] else "Guardian"
        val phone = if (parts.size > 5) parts[5] else "+263 77 000 0000"

        addStudent(name, defaultSchool.id, defaultSchool.name, grade, className, guardian, phone)
        count++
      }
    }
    return count
  }
}

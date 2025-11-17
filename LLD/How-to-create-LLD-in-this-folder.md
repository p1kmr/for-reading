You are an expert software engineer and LLD (low-level design) instructor. Explain everything for a beginner and walk me through the design step-by-step. For each step show what to draw first, why, and how the diagram should evolve (incremental/extendable). Use plain language and simple Java examples where helpful.

Create artifact for codes. Like mermaid code create artifacts, for java code create artifacts.


1) Start with requirements (use the list below)

First present Functional Requirements (clear bullet list).

Then present Non-Functional Requirements (performance, reliability, concurrency, security, etc.).


> Use the requirements the user gave and expand/clean them.



2) Deliverables (what I want you to produce)

1. Cleaned & expanded requirements (functional + non-functional).


2. Use case diagram (Mermaid) and text explanation of actors, use cases, and flows.


3. Step-by-step UML diagrams (class diagrams, sequence diagrams, component diagram) that build on each other. For each step:

Label it Step 1, Step 2, ….

Show the Mermaid syntax for that step.

Explain what changed from the previous step and why we added/modified elements.

Use note boxes in Mermaid to add short explanations (show how to use note left / note right).



4. Final complete class diagram (Mermaid + Java class skeletons for main classes).


5. Clear explanations for relationships (association, aggregation, composition, inheritance, dependency):

For each relationship: definition, when to use it, how it looks in UML, and a small Java code snippet illustrating it.

Mention tips/tricks (e.g., for composition: create instance inside class; for aggregation: pass instance via constructor; when to prefer interfaces, explain kind of concept and topic).



6. Explain SOLID principles and show where each principle applies in this design (short examples).


7. Recommend appropriate Design Patterns (e.g., Factory, Strategy, Repository, Singleton for DB connections, Observer for availability updates) and show:

Why the pattern fits the problem,

A small UML or Mermaid snippet,

A short Java skeleton showing how to apply it.



8. Concurrency & consistency: explain how to handle concurrent reservations and avoid double-booking (locking strategies, optimistic vs pessimistic, transactions). Keep it beginner-friendly.


9. Data model suggestions (simple ER / tables) and mapping to classes (if using ORM/JPA).


10. Sequence diagram(s) for main flows (search & reserve, modify reservation, cancel, payment). Provide Mermaid + explanation.


11. Deployment / component diagram (brief) showing modules (API, service layer, DB, payment gateway).


12. Interviewer Q&A section: 10–15 common LLD/interview questions about this system with concise model answers (explain trade-offs).


13. Checklist: “What to draw first” — a short prioritized checklist for someone making diagrams on a whiteboard or paper.


14. Keep everything beginner-friendly: avoid jargon or explain it clearly, and include short code snippets and inline comments in Java.



3) Presentation style & constraints

Use Mermaid for all diagrams. For each Mermaid block include a short rendered explanation and use note boxes to annotate decisions.

Use stepwise incremental diagrams: each step must extend the previous design (don’t jump to final without showing incremental steps).

Keep Java code skeletons only (no full implementations) but show constructors, key methods, and relationships.

When giving examples of SOLID or design patterns, keep them minimal and focused — show the core idea.

Where there are design trade-offs, explicitly state them (e.g., caching vs strong consistency, eager vs lazy loading).

Make sure the final design addresses the functional requirements and highlights any assumptions you make (list assumptions).

Give  mistake and solution beginner do.

End with a one-page summary (bulleted) of design decisions and why they were chosen.




You are an expert software engineer and LLD (low-level design) instructor. Explain everything for a beginner and walk me through the design step-by-step. For each step show what to draw first, why, and how the diagram should evolve (incremental/extendable). Use plain language and simple Java examples where helpful.

Create artifact for codes. Like mermaid code create artifacts, for java code create artifacts.


1) Start with requirements (use the list below)

First present Functional Requirements (clear bullet list).

Then present Non-Functional Requirements (performance, reliability, concurrency, security, etc.).


> Use the requirements the user gave and expand/clean them.



2) Deliverables (what I want you to produce)

1. Cleaned & expanded requirements (functional + non-functional).


2. Use case diagram (Mermaid) and text explanation of actors, use cases, and flows.


3. Step-by-step UML diagrams (class diagrams, sequence diagrams, component diagram) that build on each other. For each step:

Label it Step 1, Step 2, ….

Show the Mermaid syntax for that step.

Explain what changed from the previous step and why we added/modified elements.

Use note boxes in Mermaid to add short explanations (show how to use note left / note right).



4. Final complete class diagram (Mermaid + Java class skeletons for main classes).


5. Clear explanations for relationships (association, aggregation, composition, inheritance, dependency):

For each relationship: definition, when to use it, how it looks in UML, and a small Java code snippet illustrating it.

Mention tips/tricks (e.g., for composition: create instance inside class; for aggregation: pass instance via constructor; when to prefer interfaces, explain kind of concept and topic).



6. Explain SOLID principles and show where each principle applies in this design (short examples).


7. Recommend appropriate Design Patterns (e.g., Factory, Strategy, Repository, Singleton for DB connections, Observer for availability updates) and show:

Why the pattern fits the problem,

A small UML or Mermaid snippet,

A short Java skeleton showing how to apply it.



8. Concurrency & consistency: explain how to handle concurrent reservations and avoid double-booking (locking strategies, optimistic vs pessimistic, transactions). Keep it beginner-friendly.


9. Data model suggestions (simple ER / tables) and mapping to classes (if using ORM/JPA).


10. Sequence diagram(s) for main flows (search & reserve, modify reservation, cancel, payment). Provide Mermaid + explanation.


11. Deployment / component diagram (brief) showing modules (API, service layer, DB, payment gateway).


12. Interviewer Q&A section: 10–15 common LLD/interview questions about this system with concise model answers (explain trade-offs).


13. Checklist: “What to draw first” — a short prioritized checklist for someone making diagrams on a whiteboard or paper.


14. Keep everything beginner-friendly: avoid jargon or explain it clearly, and include short code snippets and inline comments in Java.



3) Presentation style & constraints

Use Mermaid for all diagrams. For each Mermaid block include a short rendered explanation and use note boxes to annotate decisions.

Use stepwise incremental diagrams: each step must extend the previous design (don’t jump to final without showing incremental steps).

Keep Java code skeletons only (no full implementations) but show constructors, key methods, and relationships.

When giving examples of SOLID or design patterns, keep them minimal and focused — show the core idea.

Where there are design trade-offs, explicitly state them (e.g., caching vs strong consistency, eager vs lazy loading).

Make sure the final design addresses the functional requirements and highlights any assumptions you make (list assumptions).

Give  mistake and solution beginner do.

End with a one-page summary (bulleted) of design decisions and why they were chosen.





OUTPUTS:
usecase_diagram.mermaid
step1_class_diagram.mermaid
WHERE TO START?
Why start here?
What SHOULD added
Design Decision

step2_class_diagram.mermaid
 WHAT YOU ADDED IN THSI STEP?
Why YOU ADDED ?
What  IS THE CHANGE FROM PREVIOUS STEP
Design Decision

step3_class_diagram.mermaid

step4_class_diagram.mermaid
step5_class_diagram.mermaid
final_class_diagram.mermaid

EACH STEP SHOUDL CONTAIN
stepdiagram.mermaid
WHAT YOU ADDED?
Why YOU ADDED ?
What  IS THE CHANGE FROM PREVIOUS STEP WHY?
Design Decision

interview_qa_summary.md (QUESTION AND ANSWERS ALSO JAVA CODE SNIPPET WITH EXPLAIANTAION)

GIVE JAVA CODE IN ONE FILE WITH PROPER EXPLAIANTION COMMNENTED IN TEH CODE ITSELF

COMPLETE DETAILD DIAGRAM AT LAST:
graph TB
    subgraph "🌐 PRESENTATION LAYER - Controllers"
        CarController["<b>CarController</b><br/>────────────────<br/>+ searchCars(criteria): List&lt;Car&gt;<br/>+ getCarById(id): Car<br/>+ addCar(car): String<br/>────────────────<br/>🎯 Single Responsibility<br/>Only handles HTTP"]

        ReservationController["<b>ReservationController</b><br/>────────────────<br/>+ createReservation(dto): Reservation<br/>+ modifyReservation(id, dto): boolean<br/>+ cancelReservation(id): boolean<br/>+ getReservation(id): Reservation<br/>────────────────<br/>🎯 Single Responsibility<br/>Only handles HTTP"]

        PaymentController["<b>PaymentController</b><br/>────────────────<br/>+ processPayment(dto): Payment<br/>+ getPaymentStatus(id): Status<br/>────────────────<br/>🎯 Single Responsibility"]

        Note1[📝 Controllers use DTOs<br/>to decouple from domain entities<br/>────────────────<br/>Pattern: Data Transfer Object]
    end

    subgraph "💼 SERVICE LAYER - Business Logic"
        direction TB

        subgraph "Core Services"
            ReservationService["<b>ReservationService</b><br/>────────────────<br/>- reservationRepo: ReservationRepository<br/>- carService: CarService<br/>- paymentService: PaymentService<br/>- notificationService: NotificationService<br/>────────────────<br/>+ createReservation(): Reservation<br/>+ modifyReservation(): boolean<br/>+ cancelReservation(): boolean<br/>────────────────<br/>🎯 ORCHESTRATOR<br/>Coordinates multiple services<br/>────────────────<br/>✅ Dependency Injection<br/>✅ Interface Segregation"]

            CarService["<b>CarService</b><br/>────────────────<br/>- carRepo: CarRepository<br/>- searchCriteria: SearchCriteria<br/>────────────────<br/>+ searchAvailableCars(): List&lt;Car&gt;<br/>+ checkAvailability(): boolean<br/>+ updateCarStatus(): void<br/>────────────────<br/>🎯 Single Responsibility<br/>Only manages cars<br/>────────────────<br/>✅ Dependency Inversion<br/>Depends on interface"]

            PaymentService["<b>PaymentService</b><br/>────────────────<br/>- paymentRepo: PaymentRepository<br/>- gateway: PaymentGateway<br/>- strategies: Map&lt;Type, Strategy&gt;<br/>────────────────<br/>+ processPayment(): Payment<br/>+ refundPayment(): boolean<br/>────────────────<br/>🎯 Single Responsibility<br/>────────────────<br/>✅ Strategy Pattern<br/>Different payment methods<br/>✅ Dependency Inversion"]

            CustomerService["<b>CustomerService</b><br/>────────────────<br/>- customerRepo: CustomerRepository<br/>────────────────<br/>+ registerCustomer(): String<br/>+ validateLicense(): boolean<br/>────────────────<br/>🎯 Single Responsibility"]

            NotificationService["<b>NotificationService</b><br/>────────────────<br/>- emailService: EmailService<br/>- smsService: SMSService<br/>- messageQueue: Queue<br/>────────────────<br/>+ sendConfirmation(): void<br/>+ sendReminder(): void<br/>────────────────<br/>🎯 Async Processing<br/>Uses message queue<br/>────────────────<br/>✅ Open/Closed<br/>Easy to add new channels"]
        end

        Note2[📝 Services orchestrate entities<br/>Like conductor coordinates musicians<br/>────────────────<br/>Services = VERBS<br/>Entities = NOUNS]
    end

    subgraph "🔌 REPOSITORY LAYER - Data Access"
        direction TB

        subgraph "Repository Interfaces"
            ICarRepo["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>CarRepository</b><br/>────────────────<br/>+ save(Car): void<br/>+ findById(String): Car<br/>+ findByStatus(Status): List<br/>+ findAvailable(Date, Date): List<br/>+ update(Car): void<br/>+ delete(String): void<br/>────────────────<br/>✅ Repository Pattern<br/>✅ Dependency Inversion"]

            IReservationRepo["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>ReservationRepository</b><br/>────────────────<br/>+ save(Reservation): void<br/>+ findById(String): Reservation<br/>+ findOverlapping(String, Date, Date): List<br/>+ findByCustomer(String): List<br/>+ update(Reservation): void<br/>────────────────<br/>✅ Repository Pattern<br/>🔥 Critical Method:<br/>findOverlapping()"]

            IPaymentRepo["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>PaymentRepository</b><br/>────────────────<br/>+ save(Payment): void<br/>+ findById(String): Payment<br/>+ findByReservation(String): Payment<br/>+ update(Payment): void"]

            ICustomerRepo["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>CustomerRepository</b><br/>────────────────<br/>+ save(Customer): void<br/>+ findById(String): Customer<br/>+ findByEmail(String): Customer<br/>+ update(Customer): void"]
        end

        subgraph "JPA/Hibernate Implementations"
            JpaCarRepo["<b>JpaCarRepository</b><br/>implements CarRepository<br/>────────────────<br/>extends JpaRepository&lt;Car, String&gt;<br/>────────────────<br/>+ @Query annotations<br/>+ Custom queries<br/>+ Native SQL support<br/>────────────────<br/>🗄️ MySQL Database<br/>────────────────<br/>✅ Liskov Substitution<br/>Can replace interface"]

            JpaReservationRepo["<b>JpaReservationRepository</b><br/>implements ReservationRepository<br/>────────────────<br/>extends JpaRepository<br/>────────────────<br/>@Query for overlapping:<br/>SELECT r FROM Reservation r<br/>WHERE r.car.id = :carId<br/>AND r.startDate &lt;= :end<br/>AND r.endDate &gt;= :start<br/>────────────────<br/>✅ Pessimistic Locking<br/>SELECT FOR UPDATE"]

            JpaPaymentRepo["<b>JpaPaymentRepository</b><br/>implements PaymentRepository<br/>────────────────<br/>extends JpaRepository"]

            JpaCustomerRepo["<b>JpaCustomerRepository</b><br/>implements CustomerRepository<br/>────────────────<br/>extends JpaRepository"]
        end

        subgraph "In-Memory Implementations (Testing)"
            MemCarRepo["<b>InMemoryCarRepository</b><br/>implements CarRepository<br/>────────────────<br/>- cars: Map&lt;String, Car&gt;<br/>────────────────<br/>Uses HashMap<br/>No database needed!<br/>────────────────<br/>🧪 Perfect for testing<br/>────────────────<br/>✅ Liskov Substitution<br/>✅ Strategy Pattern"]

            MemReservationRepo["<b>InMemoryReservationRepository</b><br/>implements ReservationRepository<br/>────────────────<br/>- reservations: Map<br/>────────────────<br/>🧪 Testing only"]

            Note3[📝 Same interface<br/>Different implementations<br/>────────────────<br/>Production: JPA<br/>Testing: In-Memory<br/>────────────────<br/>✅ Dependency Inversion<br/>Service doesn't know which!]
        end
    end

    subgraph "📦 DOMAIN ENTITIES - POJOs"
        direction LR

        Car["<b>Car</b> (Entity)<br/>────────────────<br/>- carId: String<br/>- make: String<br/>- model: String<br/>- year: int<br/>- dailyRate: double<br/>- carType: CarType<br/>- status: CarStatus<br/>- location: Location<br/>────────────────<br/>+ getters/setters<br/>────────────────<br/>📦 Just data holder<br/>NO business logic!"]

        Customer["<b>Customer</b> (Entity)<br/>────────────────<br/>- customerId: String<br/>- name: String<br/>- email: String<br/>- driverLicense: String<br/>- licenseExpiry: Date<br/>────────────────<br/>+ isLicenseValid(): boolean<br/>────────────────<br/>📦 Just data holder"]

        Reservation["<b>Reservation</b> (Entity)<br/>────────────────<br/>- reservationId: String<br/>- customer: Customer<br/>- car: Car<br/>- startDate: Date<br/>- endDate: Date<br/>- status: ReservationStatus<br/>- totalCost: double<br/>- version: int<br/>────────────────<br/>+ calculateCost(): double<br/>────────────────<br/>📦 Just data holder<br/>────────────────<br/>🔒 Version for<br/>Optimistic Locking"]

        Payment["<b>Payment</b> (Entity)<br/>────────────────<br/>- paymentId: String<br/>- reservation: Reservation<br/>- amount: double<br/>- method: PaymentMethod<br/>- status: PaymentStatus<br/>- transactionId: String<br/>────────────────<br/>📦 Just data holder"]

        Note4[📝 Entities = NOUNS<br/>Services = VERBS<br/>────────────────<br/>Entities hold data<br/>Services do work]
    end

    subgraph "🎨 DESIGN PATTERNS - Strategy Pattern"
        direction TB

        IPaymentStrategy["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>PaymentStrategy</b><br/>────────────────<br/>+ processPayment(amount): boolean<br/>+ refund(txnId, amount): boolean<br/>────────────────<br/>✅ Strategy Pattern<br/>Multiple algorithms"]

        StripeStrategy["<b>StripePaymentStrategy</b><br/>implements PaymentStrategy<br/>────────────────<br/>- apiKey: String<br/>- stripeClient: StripeAPI<br/>────────────────<br/>+ processPayment(): boolean<br/>────────────────<br/>💳 Calls Stripe API<br/>POST /v1/charges"]

        PayPalStrategy["<b>PayPalPaymentStrategy</b><br/>implements PaymentStrategy<br/>────────────────<br/>- clientId: String<br/>- clientSecret: String<br/>────────────────<br/>+ processPayment(): boolean<br/>────────────────<br/>💳 Calls PayPal API<br/>OAuth + REST"]

        CryptoStrategy["<b>CryptoPaymentStrategy</b><br/>implements PaymentStrategy<br/>────────────────<br/>- walletAddress: String<br/>────────────────<br/>+ processPayment(): boolean<br/>────────────────<br/>💳 Blockchain transaction"]

        Note5[📝 Easy to add new payment<br/>without changing existing code<br/>────────────────<br/>✅ Open/Closed Principle<br/>✅ Strategy Pattern]

        IPaymentStrategy --> StripeStrategy
        IPaymentStrategy --> PayPalStrategy
        IPaymentStrategy --> CryptoStrategy
    end

    subgraph "🌍 EXTERNAL SERVICES - Integration"
        direction TB

        IPaymentGateway["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>PaymentGateway</b><br/>────────────────<br/>+ charge(amount, details): Result<br/>+ refund(txnId, amount): boolean<br/>+ getStatus(txnId): String<br/>────────────────<br/>✅ Dependency Inversion<br/>Service depends on interface"]

        StripeGateway["<b>StripePaymentGateway</b><br/>implements PaymentGateway<br/>────────────────<br/>- httpClient: HttpClient<br/>────────────────<br/>Calls: api.stripe.com<br/>────────────────<br/>🔐 Handles sensitive data<br/>PCI DSS compliant"]

        MockGateway["<b>MockPaymentGateway</b><br/>implements PaymentGateway<br/>────────────────<br/>Returns success always<br/>────────────────<br/>🧪 Testing only<br/>No real charges!"]

        IEmailService["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>EmailService</b><br/>────────────────<br/>+ send(to, subject, body): void<br/>+ sendTemplate(to, templateId): void"]

        SendGridEmail["<b>SendGridEmailService</b><br/>implements EmailService<br/>────────────────<br/>Calls: api.sendgrid.com<br/>────────────────<br/>📧 Production email"]

        ConsoleEmail["<b>ConsoleEmailService</b><br/>implements EmailService<br/>────────────────<br/>Prints to console<br/>────────────────<br/>🛠️ Development only"]

        ISMSService["<b>&lt;&lt;interface&gt;&gt;</b><br/><b>SMSService</b><br/>────────────────<br/>+ send(phone, message): void"]

        TwilioSMS["<b>TwilioSMSService</b><br/>implements SMSService<br/>────────────────<br/>Calls: api.twilio.com<br/>────────────────<br/>📱 Production SMS"]

        Note6[📝 External services as actors<br/>────────────────<br/>Use interfaces to:<br/>✅ Swap providers easily<br/>✅ Test without real APIs<br/>✅ Mock for development]

        IPaymentGateway --> StripeGateway
        IPaymentGateway --> MockGateway
        IEmailService --> SendGridEmail
        IEmailService --> ConsoleEmail
        ISMSService --> TwilioSMS
    end

    subgraph "🏭 FACTORIES - Object Creation"
        ReservationFactory["<b>ReservationFactory</b><br/>────────────────<br/>+ createReservation(params): Reservation<br/>+ createWithValidation(params): Reservation<br/>────────────────<br/>🏭 Factory Pattern<br/>Centralizes complex creation<br/>────────────────<br/>Validates all inputs<br/>Calculates initial cost<br/>Sets default values"]

        PaymentFactory["<b>PaymentFactory</b><br/>────────────────<br/>+ createPayment(reservation): Payment<br/>────────────────<br/>🏭 Factory Pattern"]

        Note7[📝 Factory handles complexity<br/>────────────────<br/>Services use factories<br/>instead of 'new'<br/>────────────────<br/>✅ Single Responsibility]
    end

    subgraph "🗄️ DATABASE"
        MySQL[("<b>MySQL Database</b><br/>────────────────<br/>Tables:<br/>• cars<br/>• customers<br/>• reservations<br/>• payments<br/>• locations<br/>────────────────<br/>Indexes:<br/>• idx_car_dates<br/>• idx_customer_email<br/>• idx_reservation_status<br/>────────────────<br/>🔒 Transactions<br/>🔒 Locking")]

        Redis[("<b>Redis Cache</b><br/>────────────────<br/>Cached data:<br/>• Available cars<br/>• Search results<br/>• Session data<br/>────────────────<br/>TTL: 5 minutes<br/>────────────────<br/>⚡ Performance boost")]
    end

    subgraph "📊 MONITORING & LOGGING"
        Logger["<b>LoggingService</b><br/>────────────────<br/>+ logInfo(message): void<br/>+ logError(error): void<br/>+ logTransaction(details): void<br/>────────────────<br/>Uses: Logback/Log4j<br/>Output: ELK Stack"]

        Metrics["<b>MetricsCollector</b><br/>────────────────<br/>+ recordReservation(): void<br/>+ recordPayment(amount): void<br/>────────────────<br/>Uses: Prometheus<br/>Visualize: Grafana"]
    end

    subgraph "🔐 SECURITY & VALIDATION"
        AuthService["<b>AuthenticationService</b><br/>────────────────<br/>+ validateToken(token): User<br/>+ generateToken(user): String<br/>────────────────<br/>Uses: JWT<br/>────────────────<br/>🔐 Stateless auth"]

        Validator["<b>ValidationService</b><br/>────────────────<br/>+ validateReservation(dto): Result<br/>+ validatePayment(dto): Result<br/>────────────────<br/>✅ Input validation<br/>Prevents injection"]
    end

    subgraph "⚡ ASYNC PROCESSING"
        MessageQueue[("<b>RabbitMQ / Kafka</b><br/>────────────────<br/>Queues:<br/>• notification-queue<br/>• payment-retry-queue<br/>• audit-queue<br/>────────────────<br/>📨 Async messaging<br/>Decouples services")]

        NotificationWorker["<b>NotificationWorker</b><br/>────────────────<br/>@RabbitListener<br/>────────────────<br/>Processes async:<br/>• Email sending<br/>• SMS sending<br/>────────────────<br/>🔄 Retry on failure"]
    end

    %% ========== CONNECTIONS ==========

    %% Controllers to Services
    CarController --> CarService
    ReservationController --> ReservationService
    PaymentController --> PaymentService

    %% Service Orchestration
    ReservationService --> CarService
    ReservationService --> PaymentService
    ReservationService --> NotificationService
    ReservationService --> ReservationFactory

    PaymentService --> IPaymentGateway
    PaymentService --> IPaymentStrategy
    PaymentService --> PaymentFactory

    NotificationService --> IEmailService
    NotificationService --> ISMSService
    NotificationService --> MessageQueue

    %% Services to Repositories
    CarService --> ICarRepo
    ReservationService --> IReservationRepo
    PaymentService --> IPaymentRepo
    CustomerService --> ICustomerRepo

    %% Repository Implementations
    ICarRepo --> JpaCarRepo
    ICarRepo --> MemCarRepo
    IReservationRepo --> JpaReservationRepo
    IReservationRepo --> MemReservationRepo
    IPaymentRepo --> JpaPaymentRepo
    ICustomerRepo --> JpaCustomerRepo

    %% Repositories to Database
    JpaCarRepo --> MySQL
    JpaReservationRepo --> MySQL
    JpaPaymentRepo --> MySQL
    JpaCustomerRepo --> MySQL

    %% Cache connections
    CarService -.->|Cache check| Redis
    ReservationService -.->|Cache invalidate| Redis

    %% Entities
    ReservationService -.->|Creates/Uses| Reservation
    CarService -.->|Manages| Car
    CustomerService -.->|Manages| Customer
    PaymentService -.->|Manages| Payment

    %% Monitoring
    ReservationService -.->|Logs| Logger
    PaymentService -.->|Logs| Logger
    ReservationService -.->|Metrics| Metrics

    %% Security
    CarController -.->|Validates| AuthService
    ReservationController -.->|Validates| AuthService
    ReservationService -.->|Validates input| Validator

    %% Async
    NotificationService --> MessageQueue
    MessageQueue --> NotificationWorker
    NotificationWorker --> IEmailService
    NotificationWorker --> ISMSService

    %% ========== STYLING ==========

    classDef interfaceStyle fill:#e1f5ff,stroke:#01579b,stroke-width:3px,color:#000
    classDef implementationStyle fill:#f3e5f5,stroke:#4a148c,stroke-width:2px,color:#000
    classDef serviceStyle fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px,color:#000
    classDef entityStyle fill:#fff3e0,stroke:#e65100,stroke-width:2px,color:#000
    classDef controllerStyle fill:#fce4ec,stroke:#880e4f,stroke-width:2px,color:#000
    classDef externalStyle fill:#fff9c4,stroke:#f57f17,stroke-width:2px,color:#000
    classDef noteStyle fill:#ffffcc,stroke:#996633,stroke-width:1px,color:#000
    classDef dbStyle fill:#e0e0e0,stroke:#424242,stroke-width:2px,color:#000

    class ICarRepo,IReservationRepo,IPaymentRepo,ICustomerRepo,IPaymentGateway,IEmailService,ISMSService,IPaymentStrategy interfaceStyle
    class JpaCarRepo,JpaReservationRepo,JpaPaymentRepo,JpaCustomerRepo,MemCarRepo,MemReservationRepo,StripeGateway,MockGateway,SendGridEmail,ConsoleEmail,TwilioSMS,StripeStrategy,PayPalStrategy,CryptoStrategy implementationStyle
    class ReservationService,CarService,PaymentService,CustomerService,NotificationService serviceStyle
    class Car,Customer,Reservation,Payment entityStyle
    class CarController,ReservationController,PaymentController controllerStyle
    class ReservationFactory,PaymentFactory,AuthService,Validator,Logger,Metrics,NotificationWorker externalStyle
    class Note1,Note2,Note3,Note4,Note5,Note6,Note7 noteStyle
    class MySQL,Redis,MessageQueue dbStyle



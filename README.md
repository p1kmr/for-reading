# Spring Boot Interview Questions & Answers
## PayPal & Capital One Preparation Guide

> 🎯 **150+ Questions** | Beginner-Friendly | Code Examples | Common Mistakes | ASCII Diagrams

---

## 📑 Table of Contents

1. [Core Spring Boot Concepts](#1-core-spring-boot-concepts)
2. [Dependency Injection & IoC](#2-dependency-injection--ioc)
3. [Spring Boot Annotations](#3-spring-boot-annotations)
4. [REST API Development](#4-rest-api-development)
5. [Data Access & JPA](#5-data-access--jpa)
6. [Exception Handling](#6-exception-handling)
7. [Spring Security](#7-spring-security)
8. [Microservices](#8-microservices)
9. [Testing](#9-testing)
10. [Performance & Optimization](#10-performance--optimization)
11. [Scenario-Based Questions](#11-scenario-based-questions)
12. [System Design Questions](#12-system-design-questions)

---

## 1. Core Spring Boot Concepts

### Q1: What is Spring Boot and why use it?

**Answer:**

Spring Boot is a framework built on top of Spring Framework that simplifies the development of production-ready applications.

**Key Benefits:**
```
┌─────────────────────────────────────────┐
│  Traditional Spring    │  Spring Boot   │
├─────────────────────────────────────────┤
│  Manual configuration  │  Auto-config   │
│  XML files needed      │  No XML        │
│  External server       │  Embedded      │
│  Complex setup         │  Quick start   │
│  Many dependencies     │  Starters      │
└─────────────────────────────────────────┘
```

**Example Code:**

```java
// Traditional Spring (Old way)
// Need 50+ lines of XML configuration

// Spring Boot (New way)
@SpringBootApplication  // Just one annotation!
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

**Why Companies Like PayPal/Capital One Use It:**
- Fast development → Quick time to market
- Microservices-ready → Scale easily
- Production-ready features → Monitoring, health checks
- Large community → Easy to hire developers

---

### Q2: Explain @SpringBootApplication annotation

**Answer:**

`@SpringBootApplication` is a combination of 3 annotations:

```java
@SpringBootApplication
// This is actually equal to:

@Configuration        // Marks class as source of bean definitions
@EnableAutoConfiguration  // Auto-configures based on classpath
@ComponentScan       // Scans for components in current package
```

**Visual Representation:**
```
         @SpringBootApplication
                  │
     ┌────────────┼────────────┐
     │            │            │
     ▼            ▼            ▼
@Configuration  @EnableAuto  @ComponentScan
                Configuration
```

**Example:**

```java
@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
```

**What Happens Behind the Scenes:**
1. Scans all packages under `com.example.ecommerce`
2. Finds classes with `@Component`, `@Service`, `@Repository`
3. Creates beans (objects) automatically
4. Configures database, web server, etc.

---

### Q3: What are Spring Boot Starters?

**Answer:**

Starters are pre-configured dependency packages that include everything you need for a specific functionality.

**Common Starters:**

```xml
<!-- Web applications with REST APIs -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Database access with JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
</dependency>
```

**What's Inside a Starter?**
```
spring-boot-starter-web includes:
├── Spring MVC
├── Tomcat (embedded server)
├── Jackson (JSON conversion)
├── Validation
└── Logging
```

**❌ Common Mistake:**
```xml
<!-- Don't specify versions! Spring Boot manages them -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- ❌ DON'T DO THIS: <version>2.7.0</version> -->
</dependency>
```

**✅ Correct Way:**
```xml
<!-- Version inherited from parent -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
```

---

### Q4: What is Auto-Configuration in Spring Boot?

**Answer:**

Auto-configuration automatically sets up your application based on dependencies in the classpath.

**How It Works:**

```mermaid
graph TD
    A[Spring Boot Starts] --> B{Checks Classpath}
    B --> C{H2 Database Found?}
    C -->|Yes| D[Configure H2 DataSource]
    C -->|No| E{MySQL Found?}
    E -->|Yes| F[Configure MySQL DataSource]
    B --> G{Tomcat Found?}
    G -->|Yes| H[Configure Embedded Tomcat]
```

**Example:**

```java
// You just add this dependency
<!-- pom.xml -->
<dependency>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

// Spring Boot automatically:
// ✅ Creates EntityManagerFactory
// ✅ Sets up Transaction Manager
// ✅ Configures DataSource
// ✅ Enables JPA repositories

// You don't write ANY configuration code!
```

**How to See Auto-Configurations:**

```properties
# application.properties
debug=true  # Shows what got auto-configured
```

**Output Example:**
```
Positive matches:
-----------------
DataSourceAutoConfiguration matched:
   - @ConditionalOnClass found required class 'javax.sql.DataSource'
   
Negative matches:
-----------------
MongoAutoConfiguration did not match:
   - @ConditionalOnClass did not find required class 'com.mongodb.client.MongoClient'
```

---

### Q5: How to disable specific auto-configurations?

**Answer:**

**Method 1: Using @SpringBootApplication**
```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class MyApplication { }
```

**Method 2: Using application.properties**
```properties
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

**When to Disable Auto-Configuration:**
- Custom security implementation
- Using multiple databases
- Custom configurations conflict

**❌ Common Mistake:**
```java
// Wrong: Excluding but still using the feature
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MyApp { }

@Repository  // ❌ This won't work without DataSource!
public interface UserRepository extends JpaRepository<User, Long> { }
```

---

## 2. Dependency Injection & IoC

### Q6: What is Dependency Injection? Explain with example.

**Answer:**

Dependency Injection (DI) is a design pattern where objects receive their dependencies from external sources rather than creating them.

**Without DI (Tight Coupling):**
```java
// ❌ Bad: Class creates its own dependencies
public class OrderService {
    private PaymentService paymentService = new PaymentService();
    // Hard to test, hard to change
}
```

**With DI (Loose Coupling):**
```java
// ✅ Good: Dependencies injected from outside
@Service
public class OrderService {
    private final PaymentService paymentService;
    
    @Autowired  // Spring injects PaymentService
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

**Real-World Analogy:**
```
Without DI:  You build your own car from scratch
With DI:     Someone gives you a car (Spring manages it)
```

**Visual Representation:**
```
┌─────────────────────────────────────────┐
│           Spring Container              │
│                                         │
│  ┌────────────┐      ┌──────────────┐  │
│  │  Order     │◄─────│  Payment     │  │
│  │  Service   │      │  Service     │  │
│  └────────────┘      └──────────────┘  │
│         ▲                    ▲          │
│         └────────┬───────────┘          │
│              Spring injects              │
└─────────────────────────────────────────┘
```

---

### Q7: What are different types of Dependency Injection?

**Answer:**

**1. Constructor Injection (✅ Recommended)**
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    // Constructor injection
    @Autowired  // Optional in Spring 4.3+
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Why Best:**
- Immutable (thread-safe)
- Easy to test
- Clear dependencies
- Required dependencies are obvious

**2. Setter Injection**
```java
@Service
public class UserService {
    private UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**When to Use:**
- Optional dependencies
- Need to change dependency at runtime

**3. Field Injection (❌ Not Recommended)**
```java
@Service
public class UserService {
    @Autowired  // ❌ Avoid this
    private UserRepository userRepository;
}
```

**Why Bad:**
- Can't test easily
- Hides dependencies
- Can't make final
- Harder to debug

**Comparison:**

| Type | Immutability | Testing | Clarity | Recommendation |
|------|-------------|---------|---------|----------------|
| Constructor | ✅ | ✅ | ✅ | ⭐⭐⭐ |
| Setter | ❌ | ✅ | ⚠️ | ⭐⭐ |
| Field | ❌ | ❌ | ❌ | ❌ |

---

### Q8: Explain IoC (Inversion of Control) Container

**Answer:**

IoC Container is the core of Spring Framework that manages object lifecycle and dependencies.

**Traditional Flow:**
```
You create objects → You wire them → You manage them
```

**IoC Flow:**
```
Spring creates objects → Spring wires them → Spring manages them
```

**IoC Container Types:**

```
┌──────────────────────────────────────┐
│      ApplicationContext              │  ← Recommended
│  (Advanced, with more features)      │
├──────────────────────────────────────┤
│      BeanFactory                     │  ← Basic
│  (Simple, less features)             │
└──────────────────────────────────────┘
```

**Example:**

```java
// Spring creates and manages these objects
@Configuration
public class AppConfig {
    
    @Bean
    public UserService userService() {
        return new UserService(userRepository());
    }
    
    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl();
    }
}
```

**Bean Lifecycle in Container:**

```mermaid
graph LR
    A[Container Starts] --> B[Load Bean Definitions]
    B --> C[Instantiate Beans]
    C --> D[Inject Dependencies]
    D --> E[Post-Processing]
    E --> F[Bean Ready]
    F --> G[Container Shutdown]
    G --> H[Destroy Beans]
```

---

### Q9: What is @Autowired and how does it work?

**Answer:**

`@Autowired` tells Spring to inject dependencies automatically.

**How Spring Finds the Bean:**

```
Step 1: Match by Type
   ↓
Step 2: If multiple beans of same type, match by @Qualifier
   ↓
Step 3: If still multiple, match by parameter name
   ↓
Step 4: Throw error if still can't resolve
```

**Example:**

```java
@Service
public class OrderService {
    
    // Spring will find a bean of type PaymentService
    @Autowired
    private PaymentService paymentService;
}
```

**Multiple Implementations Problem:**

```java
// Two implementations of PaymentService
@Service("creditCard")
public class CreditCardPayment implements PaymentService { }

@Service("paypal")
public class PaypalPayment implements PaymentService { }

// How to choose? Use @Qualifier
@Service
public class OrderService {
    
    @Autowired
    @Qualifier("creditCard")  // ✅ Specifies which one
    private PaymentService paymentService;
}
```

**Required vs Optional:**

```java
// Required (default) - App won't start if not found
@Autowired
private UserService userService;

// Optional - Null if not found
@Autowired(required = false)
private NotificationService notificationService;
```

**❌ Common Mistakes:**

```java
// Mistake 1: Forgetting @Component on the dependency
public class UserRepository { }  // ❌ No @Component!

@Service
public class UserService {
    @Autowired
    private UserRepository repo;  // ❌ Won't work!
}

// Mistake 2: Autowiring in non-Spring managed class
public class MyUtil {  // ❌ Not a Spring bean
    @Autowired
    private UserService service;  // ❌ Won't work!
}
```

---

### Q10: What is @Qualifier and when to use it?

**Answer:**

`@Qualifier` is used when multiple beans of the same type exist.

**Problem Scenario:**
```java
interface NotificationService {
    void send(String message);
}

@Service
public class EmailNotification implements NotificationService { }

@Service
public class SmsNotification implements NotificationService { }

@Service
public class OrderService {
    @Autowired
    private NotificationService service;  // ❌ Which one???
}
```

**Solution 1: Using @Qualifier**
```java
@Service
public class OrderService {
    
    @Autowired
    @Qualifier("emailNotification")  // ✅ Specify bean name
    private NotificationService emailService;
    
    @Autowired
    @Qualifier("smsNotification")
    private NotificationService smsService;
}
```

**Solution 2: Custom Qualifier**
```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Email { }

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface SMS { }

@Service
@Email
public class EmailNotification implements NotificationService { }

@Service
@SMS
public class SmsNotification implements NotificationService { }

// Usage
@Service
public class OrderService {
    @Autowired
    @Email  // ✅ Cleaner!
    private NotificationService emailService;
}
```

**Solution 3: @Primary**
```java
@Service
@Primary  // ✅ Default choice
public class EmailNotification implements NotificationService { }

@Service
public class SmsNotification implements NotificationService { }

@Service
public class OrderService {
    @Autowired
    private NotificationService service;  // ✅ Gets EmailNotification
}
```

---

## 3. Spring Boot Annotations

### Q11: Explain @Component, @Service, @Repository, and @Controller

**Answer:**

All are specializations of `@Component` for different layers.

**Hierarchy:**
```
        @Component (Generic)
             │
     ┌───────┼────────┬──────────┐
     │       │        │          │
@Controller @Service @Repository @RestController
```

**@Component** - Generic Spring bean
```java
@Component
public class EmailValidator {
    public boolean isValid(String email) {
        return email.contains("@");
    }
}
```

**@Service** - Business logic layer
```java
@Service
public class OrderService {
    public void processOrder(Order order) {
        // Business logic here
    }
}
```

**@Repository** - Data access layer
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Database operations
}
```

**Benefits of @Repository:**
- Automatic exception translation (SQLException → DataAccessException)
- Clear separation of concerns

**@Controller** - Web MVC controller (returns views)
```java
@Controller
public class HomeController {
    @GetMapping("/home")
    public String home() {
        return "home.html";  // Returns view name
    }
}
```

**@RestController** - REST API controller (returns data)
```java
@RestController
public class UserController {
    @GetMapping("/users")
    public List<User> getUsers() {
        return userList;  // Returns JSON
    }
}
```

**Comparison:**

| Annotation | Layer | Purpose | Returns |
|-----------|-------|---------|---------|
| @Component | Any | Generic bean | - |
| @Service | Business | Business logic | - |
| @Repository | Data | Database ops | - |
| @Controller | Web | MVC controller | View name |
| @RestController | Web | REST API | Data (JSON) |

**❌ Common Mistake:**
```java
// Wrong: Using wrong annotation for layer
@Controller  // ❌ Should be @Service
public class OrderService {
    public void processOrder() { }
}
```

---

### Q12: Difference between @Controller and @RestController

**Answer:**

```java
// @RestController = @Controller + @ResponseBody

@RestController
// Same as:
@Controller
@ResponseBody
```

**@Controller** (Traditional MVC)
```java
@Controller
public class UserController {
    
    @GetMapping("/user")
    public String getUser(Model model) {
        model.addAttribute("user", user);
        return "userPage";  // ✅ Returns view name (HTML page)
    }
}
```

**@RestController** (REST API)
```java
@RestController
public class UserController {
    
    @GetMapping("/api/user")
    public User getUser() {
        return user;  // ✅ Returns data (JSON)
    }
}
```

**Visual Difference:**
```
@Controller:
Browser → Controller → View (JSP/Thymeleaf) → HTML

@RestController:
Mobile/Web → Controller → JSON → Display
```

**When to Use:**

| Scenario | Use |
|----------|-----|
| Building website with HTML pages | @Controller |
| Building REST API for mobile app | @RestController |
| Single Page App (React/Angular) | @RestController |
| Traditional web app | @Controller |

**❌ Common Mistake:**
```java
@Controller  // ❌ Wrong for REST API
public class UserApi {
    
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return users;  // ❌ Will try to find "users" view!
    }
}

// ✅ Fix: Add @ResponseBody or use @RestController
@RestController  // ✅ Correct
public class UserApi {
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return users;  // ✅ Returns JSON
    }
}
```

---

### Q13: What is @RequestMapping and its variants?

**Answer:**

`@RequestMapping` is the generic annotation for handling HTTP requests.

**Variants (Shortcuts):**

```java
@GetMapping    = @RequestMapping(method = GET)
@PostMapping   = @RequestMapping(method = POST)
@PutMapping    = @RequestMapping(method = PUT)
@DeleteMapping = @RequestMapping(method = DELETE)
@PatchMapping  = @RequestMapping(method = PATCH)
```

**Example:**

```java
@RestController
@RequestMapping("/api/users")  // Base path
public class UserController {
    
    // GET /api/users
    @GetMapping
    public List<User> getAllUsers() { }
    
    // GET /api/users/123
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { }
    
    // POST /api/users
    @PostMapping
    public User createUser(@RequestBody User user) { }
    
    // PUT /api/users/123
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) { }
    
    // DELETE /api/users/123
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) { }
}
```

**Multiple HTTP Methods:**
```java
@RequestMapping(value = "/users", method = {GET, POST})
public void handleUsers() { }
```

**Additional Parameters:**

```java
@GetMapping(
    value = "/users",
    produces = "application/json",  // Response type
    consumes = "application/json",  // Request type
    headers = "X-API-VERSION=1",    // Required header
    params = "version=1"             // Required parameter
)
```

---

### Q14: Explain @PathVariable vs @RequestParam vs @RequestBody

**Answer:**

**1. @PathVariable** - Extract from URL path
```java
// URL: GET /users/123
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    // id = 123
}

// Multiple path variables
// URL: GET /users/123/orders/456
@GetMapping("/users/{userId}/orders/{orderId}")
public Order getOrder(
    @PathVariable Long userId,
    @PathVariable Long orderId
) { }
```

**2. @RequestParam** - Extract from query parameters
```java
// URL: GET /users?name=John&age=30
@GetMapping("/users")
public List<User> searchUsers(
    @RequestParam String name,
    @RequestParam int age
) {
    // name = "John", age = 30
}

// Optional parameter with default
@GetMapping("/users")
public List<User> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String sort
) { }
```

**3. @RequestBody** - Extract from request body (JSON)
```java
// POST /users
// Body: {"name": "John", "email": "john@example.com"}
@PostMapping("/users")
public User createUser(@RequestBody User user) {
    // user object created from JSON
}
```

**Complete Example:**

```java
@RestController
@RequestMapping("/api")
public class ProductController {
    
    // Path Variable: /api/products/123
    @GetMapping("/products/{id}")
    public Product getById(@PathVariable Long id) { }
    
    // Request Param: /api/products?category=electronics&maxPrice=1000
    @GetMapping("/products")
    public List<Product> search(
        @RequestParam String category,
        @RequestParam double maxPrice
    ) { }
    
    // Request Body: POST /api/products with JSON
    @PostMapping("/products")
    public Product create(@RequestBody Product product) { }
    
    // Combination: PUT /api/products/123?notify=true with JSON
    @PutMapping("/products/{id}")
    public Product update(
        @PathVariable Long id,
        @RequestBody Product product,
        @RequestParam(defaultValue = "false") boolean notify
    ) { }
}
```

**Comparison:**

| Annotation | Location | Example | Use Case |
|-----------|----------|---------|----------|
| @PathVariable | URL path | /users/123 | Resource ID |
| @RequestParam | Query string | ?name=John | Filters, pagination |
| @RequestBody | HTTP body | JSON data | Create/Update data |

**❌ Common Mistakes:**

```java
// Mistake 1: Wrong annotation
@GetMapping("/users/{id}")
public User getUser(@RequestParam Long id) { }  // ❌ Should be @PathVariable

// Mistake 2: Missing parameter name mismatch
@GetMapping("/users/{userId}")
public User getUser(@PathVariable Long id) { }  // ❌ Name mismatch

// ✅ Fix:
@GetMapping("/users/{userId}")
public User getUser(@PathVariable("userId") Long id) { }

// Or match names:
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) { }  // ✅
```

---

### Q15: What is @Configuration and @Bean?

**Answer:**

`@Configuration` marks a class as a source of bean definitions. `@Bean` defines a Spring-managed object.

**Example:**

```java
@Configuration
public class AppConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        ds.setUsername("root");
        ds.setPassword("password");
        return ds;
    }
    
    @Bean
    public UserService userService() {
        return new UserService(dataSource());
    }
}
```

**How It Works:**
```mermaid
graph LR
    A[Spring Container] --> B[Configuration Class]
    B --> C[Scans Bean methods]
    C --> D[Creates & Manages Beans]
```

---

## 🎯 Deep Dive: @Bean vs @Component vs @Configuration

### 🔰 Start Here: Understanding POJO, Bean, and Spring Basics

**Let's start from the very beginning:**

#### 1️⃣ What is a POJO?

**POJO = Plain Old Java Object**

It's just a regular Java class. Nothing special. No magic.

```java
// This is a POJO - just a simple class
public class User {
    private String name;
    private int age;
    
    // Constructor
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

**You create POJOs yourself:**
```java
User user1 = new User("John", 25);  // You create it
User user2 = new User("Jane", 30);  // You create it
```

---

#### 2️⃣ What is a Bean?

**Bean = An object that Spring creates and manages for you**

Think of Spring as a factory that creates and stores objects for you.

```java
// Same User class, but now Spring manages it
@Component  // ← This tells Spring: "Please create and manage this for me"
public class UserService {
    
    public void doSomething() {
        System.out.println("UserService is working!");
    }
}
```

**Spring creates it for you:**
```java
// You DON'T write: UserService service = new UserService();
// Spring does it automatically!

@RestController
public class MyController {
    
    @Autowired  // ← Spring gives you the object it created
    private UserService userService;  // Spring injects it here!
    
    public void test() {
        userService.doSomething();  // Use it!
    }
}
```

**Key Difference:**
- **POJO**: You create it with `new`
- **Bean**: Spring creates it and gives it to you

---

#### 3️⃣ Why Use Beans Instead of POJOs?

**Without Spring (Manual POJO creation):**
```java
public class OrderController {
    
    // You create everything manually
    private Database database = new Database("localhost", "root", "password");
    private EmailService emailService = new EmailService("smtp.gmail.com");
    private PaymentService paymentService = new PaymentService(database);
    private OrderService orderService = new OrderService(database, emailService, paymentService);
    
    // If Database changes, you update it everywhere! 😫
}

public class UserController {
    
    // Creating same Database again! 😫
    private Database database = new Database("localhost", "root", "password");
    private UserService userService = new UserService(database);
}
```

**With Spring (Beans):**
```java
// Spring creates ONE Database and shares it everywhere! 😊

@RestController
public class OrderController {
    
    @Autowired
    private OrderService orderService;  // Spring gives you this
    
    // That's it! Spring handles everything!
}

@RestController
public class UserController {
    
    @Autowired
    private UserService userService;  // Spring gives you this
    
    // Same Database is reused! No duplication!
}
```

---

### 📚 What Are These Annotations For?

Now that you understand POJO vs Bean, let's learn the annotations:

#### **@Component** - "I am a Spring-managed class"

**Use this on YOUR OWN simple classes**

```java
// Your class
@Component  // ← Spring will create and manage this
public class EmailService {
    
    public void sendEmail(String to, String message) {
        System.out.println("Sending email to: " + to);
    }
}

// Spring automatically:
// 1. Creates: EmailService emailService = new EmailService();
// 2. Stores it in Spring Container
// 3. Gives it to you when you need it (@Autowired)
```

**When to use:**
- Your own classes
- Simple classes that don't need complex setup
- Classes you can add annotations to

---

#### **@Configuration** - "I contain bean factory methods"

**Use this for a class that creates OTHER beans**

```java
@Configuration  // ← This class creates beans
public class AppConfig {
    
    // This class doesn't do work itself
    // It just tells Spring HOW to create other objects
}
```

Think of it as a **factory class** that builds objects for Spring.

---

#### **@Bean** - "This method creates and configures a bean"

**Use this INSIDE @Configuration to create beans manually**

```java
@Configuration
public class AppConfig {
    
    @Bean  // ← This method returns an object for Spring to manage
    public EmailService emailService() {
        EmailService service = new EmailService();
        service.setSmtpServer("smtp.gmail.com");
        service.setPort(587);
        return service;  // Spring takes this and manages it
    }
}
```

**When to use:**
- Third-party library classes (you can't add @Component to them)
- Complex setup needed
- Need to configure the object before Spring uses it

---

### 🎯 YES! Spring Beans = Singleton Pattern (By Default)

**You're absolutely right!** Spring beans use the Singleton design pattern by default.

#### What is Singleton Pattern?

**Singleton = Only ONE instance exists in the entire application**

```java
// Traditional Singleton Pattern (Manual)
public class Database {
    private static Database instance;  // Only one instance
    
    private Database() {}  // Private constructor
    
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;  // Always returns same instance
    }
}

// Usage
Database db1 = Database.getInstance();
Database db2 = Database.getInstance();
// db1 and db2 are THE SAME object!
System.out.println(db1 == db2);  // true
```

#### Spring Does This Automatically!

```java
@Component
public class UserService {
    // Spring automatically makes this a singleton!
}

// Everywhere in your app
@RestController
public class Controller1 {
    @Autowired
    private UserService userService;  // Instance #1
}

@RestController
public class Controller2 {
    @Autowired
    private UserService userService;  // SAME instance!
}

// Both controllers get THE SAME UserService object!
```

---

### 🔄 Bean Scopes: Not Just Singleton!

Spring supports different scopes. **Singleton is just the default.**

#### **1. Singleton (Default) - ONE instance for entire app**

```java
@Component
@Scope("singleton")  // This is default, you can omit it
public class DatabaseService {
    // Only ONE instance created
}

// Or with @Bean
@Configuration
public class AppConfig {
    
    @Bean
    @Scope("singleton")  // Default
    public DatabaseService databaseService() {
        return new DatabaseService();
    }
}
```

**When to use:**
- Stateless services (no instance variables that change)
- Shared resources (database connections, caches)
- Most services in your app

---

#### **2. Prototype - NEW instance every time**

```java
@Component
@Scope("prototype")  // New instance each time!
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();
}

// Usage
@Service
public class OrderService {
    
    @Autowired
    private ApplicationContext context;
    
    public void createOrder() {
        // Get a NEW cart each time
        ShoppingCart cart1 = context.getBean(ShoppingCart.class);
        ShoppingCart cart2 = context.getBean(ShoppingCart.class);
        
        System.out.println(cart1 == cart2);  // false - different objects!
    }
}
```

**When to use:**
- Stateful objects (objects that hold user-specific data)
- Objects that shouldn't be shared
- Shopping carts, user sessions, temporary objects

---

#### **3. Request - ONE instance per HTTP request (Web apps)**

```java
@Component
@Scope("request")
public class RequestContext {
    private String requestId;
    private String userId;
    
    // Same instance throughout ONE HTTP request
    // New instance for each new request
}
```

**When to use:**
- Web applications
- Data specific to one HTTP request
- Request tracking, logging

---

#### **4. Session - ONE instance per HTTP session (Web apps)**

```java
@Component
@Scope("session")
public class UserSession {
    private String username;
    private List<String> permissions;
    
    // Same instance for entire user session
    // Different instance for different users
}
```

**When to use:**
- User login sessions
- User preferences
- Shopping cart that persists across requests

---

### 📊 Scope Comparison Table

| Scope | Instances | Lifetime | Use Case |
|-------|-----------|----------|----------|
| **singleton** | 1 per app | App startup → shutdown | Services, repositories, configs |
| **prototype** | New each time | Created → garbage collected | Stateful objects, user-specific data |
| **request** | 1 per HTTP request | Request start → end | Request tracking, temp data |
| **session** | 1 per user session | Login → logout | User sessions, shopping carts |

---

### 💡 Real-World Example: When to Use Each Scope

```java
// SINGLETON - Shared database connection pool
@Component
@Scope("singleton")  // Default - only ONE pool for entire app
public class DatabaseConnectionPool {
    private List<Connection> connections = new ArrayList<>();
    
    public Connection getConnection() {
        // All users share the same pool
        return connections.get(0);
    }
}

// PROTOTYPE - Each user gets their own cart
@Component
@Scope("prototype")  // NEW cart for each user
public class ShoppingCart {
    private List<Product> items = new ArrayList<>();
    
    public void addItem(Product product) {
        items.add(product);  // User-specific data
    }
}

// REQUEST - Track each HTTP request
@Component
@Scope("request")  // NEW instance per HTTP request
public class RequestLogger {
    private String requestId = UUID.randomUUID().toString();
    private long startTime = System.currentTimeMillis();
    
    public void logRequest() {
        System.out.println("Request " + requestId + " took " + 
            (System.currentTimeMillis() - startTime) + "ms");
    }
}

// SESSION - User login session
@Component
@Scope("session")  // ONE instance per logged-in user
public class UserSession {
    private String username;
    private boolean isAdmin;
    
    public void login(String username) {
        this.username = username;
    }
}
```

---

### ⚠️ Singleton vs Prototype: Common Pitfall

**Problem: Injecting Prototype into Singleton**

```java
// WRONG! ❌
@Component
@Scope("singleton")
public class OrderService {
    
    @Autowired
    private ShoppingCart cart;  // Prototype bean
    
    // Problem: cart is injected ONCE when OrderService is created
    // All users will share the SAME cart! 😱
}
```

**Solution 1: Use ApplicationContext**
```java
@Component
@Scope("singleton")
public class OrderService {
    
    @Autowired
    private ApplicationContext context;
    
    public void processOrder() {
        // Get a NEW cart each time
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.addItem(new Product());
    }
}
```

**Solution 2: Use @Lookup**
```java
@Component
@Scope("singleton")
public abstract class OrderService {
    
    @Lookup  // Spring generates this method
    public abstract ShoppingCart getShoppingCart();
    
    public void processOrder() {
        ShoppingCart cart = getShoppingCart();  // NEW cart each time
        cart.addItem(new Product());
    }
}
```

---

### 🎯 Summary: Spring Beans and Singleton Pattern

**Yes, you're correct!**

1. **By default, all Spring beans are Singletons**
   - @Component, @Service, @Repository → Singleton
   - @Bean in @Configuration → Singleton

2. **Spring implements Singleton pattern automatically**
   - You don't write getInstance() methods
   - Spring manages the single instance
   - Spring injects the same instance everywhere

3. **But you can change the scope if needed**
   - Use `@Scope("prototype")` for new instances
   - Use `@Scope("request")` for web requests
   - Use `@Scope("session")` for user sessions

4. **Most beans should be Singleton**
   - Stateless services
   - Repositories
   - Configuration classes
   - Utilities

5. **Use Prototype for stateful objects**
   - User-specific data
   - Shopping carts
   - Temporary objects

---

### 🎭 Visual Comparison: POJO vs Bean

```
┌─────────────────────────────────────────────────────────────┐
│                         POJO                                 │
│                                                              │
│  public class User {                                        │
│      private String name;                                   │
│  }                                                          │
│                                                              │
│  // You create it manually                                 │
│  User user = new User();  ← YOU do this                    │
│  user.setName("John");                                     │
│                                                              │
│  ❌ You manage lifecycle                                    │
│  ❌ You create multiple instances                           │
│  ❌ You wire dependencies manually                          │
└─────────────────────────────────────────────────────────────┘

                            ⬇️ Add @Component

┌─────────────────────────────────────────────────────────────┐
│                         BEAN                                 │
│                                                              │
│  @Component  ← Tell Spring to manage this                  │
│  public class UserService {                                 │
│      private String name;                                   │
│  }                                                          │
│                                                              │
│  // Spring creates it automatically                        │
│  @Autowired                                                │
│  private UserService userService;  ← SPRING does this      │
│                                                              │
│  ✅ Spring manages lifecycle                                │
│  ✅ Spring creates ONE instance (singleton)                 │
│  ✅ Spring wires dependencies automatically                 │
└─────────────────────────────────────────────────────────────┘
```

---

### 🔄 The Three Ways to Create Beans

#### **Way 1: @Component (Simplest)**
```java
@Component  // Spring scans and finds this
public class MyService {
    // Your code
}
```
**Use for:** Your own simple classes

---

#### **Way 2: @Bean in @Configuration (More Control)**
```java
@Configuration
public class AppConfig {
    
    @Bean  // You tell Spring exactly how to create it
    public MyService myService() {
        MyService service = new MyService();
        service.setSomething("configured");
        return service;
    }
}
```
**Use for:** Third-party libraries, complex setup

---

#### **Way 3: @Bean for Third-Party (Can't Modify Code)**
```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        // HikariCP is a library - you can't add @Component to it!
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        return ds;
    }
}
```
**Use for:** Libraries you don't own

---

### 🎯 Quick Decision Guide

**Ask yourself:**

1. **Is it MY class?**
   - YES → Use `@Component` (or `@Service`, `@Repository`)
   - NO → Go to question 2

2. **Is it a third-party library?**
   - YES → Use `@Bean` in `@Configuration`
   - NO → Go to question 3

3. **Does it need complex setup?**
   - YES → Use `@Bean` in `@Configuration`
   - NO → Use `@Component`

---

### 💡 Real Example: Putting It All Together

```java
// 1. POJO - Just a data class (no Spring)
public class User {
    private String name;
    private int age;
    // getters, setters
}

// 2. Bean with @Component - Your own service
@Service  // Same as @Component
public class UserService {
    
    public User createUser(String name, int age) {
        return new User(name, age);  // POJO created here
    }
}

// 3. Bean with @Configuration + @Bean - Third-party library
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        // Can't add @Component to HikariDataSource - it's a library!
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        return ds;
    }
}

// 4. Using the beans
@RestController
public class UserController {
    
    @Autowired
    private UserService userService;  // Spring injects this bean
    
    @Autowired
    private DataSource dataSource;  // Spring injects this bean
    
    @GetMapping("/user")
    public User getUser() {
        // userService is a Bean (Spring created it)
        // The User returned is a POJO (you created it)
        return userService.createUser("John", 25);
    }
}
```

---

### What Are These Annotations For?

**@Component** - "I am a Spring-managed class"
- Put this on YOUR OWN classes
- Spring automatically finds and creates instances
- Simple, straightforward bean creation

**@Configuration** - "I contain bean factory methods"
- A special class that defines how to create beans
- Contains @Bean methods
- Used for complex setup and third-party libraries

**@Bean** - "This method creates and configures a bean"
- Used inside @Configuration classes
- Returns an object that Spring should manage
- Gives you full control over object creation

---

### 📦 Why Do We Need @Bean and @Configuration?

**Problem 1: Third-Party Libraries**
You can't modify library code to add @Component!

```java
// ❌ Can't do this - you don't own this class!
@Component  // Can't add this to library code
public class ObjectMapper {  // From Jackson library
    // ...
}

// ✅ Solution: Use @Bean in @Configuration
@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        return mapper;  // Spring manages this now!
    }
}
```

**Problem 2: Complex Bean Creation**
Sometimes creating a bean needs multiple steps or configuration.

```java
// ❌ Too complex for @Component
@Component
public class DataSource {
    // How do you set URL, username, password here?
    // Constructor gets messy with many parameters
}

// ✅ Solution: Use @Bean with full control
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        ds.setUsername("root");
        ds.setPassword("password");
        ds.setMaximumPoolSize(10);
        ds.setConnectionTimeout(30000);
        // Many configuration steps!
        return ds;
    }
}
```

---

### 🔧 How Spring Manages Third-Party and Complex Beans

**Step-by-Step Process:**

1. **Spring Scans @Configuration Classes**
   ```java
   @Configuration  // Spring finds this
   public class AppConfig {
       // ...
   }
   ```

2. **Spring Calls @Bean Methods**
   ```java
   @Bean
   public RestTemplate restTemplate() {
       RestTemplate template = new RestTemplate();
       // Your custom configuration
       template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
       return template;  // Spring takes this object
   }
   ```

3. **Spring Stores Bean in Container**
   - Spring keeps the returned object in its "container"
   - Assigns it a name (method name by default)
   - Manages its lifecycle

4. **Spring Injects Bean Where Needed**
   ```java
   @Service
   public class MyService {
       
       @Autowired
       private RestTemplate restTemplate;  // Spring injects the bean here!
       
       public void callApi() {
           restTemplate.getForObject("http://api.com", String.class);
       }
   }
   ```

---

### 📊 Real-World Examples

**Example 1: Database Connection Pool (Third-Party)**
```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        // HikariCP is a third-party library
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("admin");
        config.setPassword("secret");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        
        return new HikariDataSource(config);
        // Spring now manages this connection pool!
    }
}
```

**Example 2: Redis Cache (Complex Setup)**
```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        config.setPassword("redis-password");
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setDatabase(0);
        return factory;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());  // Uses above bean!
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**Example 3: Security Configuration (Multiple Dependencies)**
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Strength = 12
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        // Complex setup with dependencies
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

---

### 🆚 When to Use What?

**Use @Component (and @Service, @Repository, @Controller):**
```java
// ✅ Your own simple classes
@Service
public class UserService {
    // Simple class you wrote
}

@Repository
public class UserRepository {
    // Your data access class
}
```

**Use @Bean + @Configuration:**
```java
// ✅ Third-party libraries
@Bean
public ObjectMapper objectMapper() { ... }

// ✅ Complex configuration
@Bean
public DataSource dataSource() { ... }

// ✅ Conditional beans
@Bean
@Profile("production")
public DataSource prodDataSource() { ... }

// ✅ Multiple instances of same type
@Bean("primaryDB")
public DataSource primaryDataSource() { ... }

@Bean("secondaryDB")
public DataSource secondaryDataSource() { ... }
```

---

### 🔄 How Spring Container Manages Beans

```
┌─────────────────────────────────────────────────────────┐
│                   Spring Container                       │
│                                                          │
│  1. Scans @Configuration classes                        │
│  2. Calls @Bean methods                                 │
│  3. Stores returned objects                             │
│  4. Manages lifecycle (init, destroy)                   │
│  5. Injects beans where needed (@Autowired)             │
│  6. Handles dependencies between beans                  │
│                                                          │
│  Bean Storage:                                          │
│  ┌──────────────────────────────────────┐              │
│  │ "dataSource" → HikariDataSource obj  │              │
│  │ "userService" → UserService obj      │              │
│  │ "restTemplate" → RestTemplate obj    │              │
│  └──────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────┘
```

---

**When to Use @Bean:**
- Third-party library classes (can't add @Component)
- Complex bean creation logic
- Conditional bean creation

**Example: Third-Party Library**
```java
@Configuration
public class RedisConfig {
    
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
        return factory;  // Can't annotate Jedis class with @Component!
    }
}
```

**@Bean vs @Component:**

| Feature | @Bean | @Component |
|---------|-------|------------|
| Location | Configuration class | On class itself |
| Use | Third-party, complex logic | Your own classes |
| Method-based | ✅ | ❌ |
| Class-based | ❌ | ✅ |

**Bean Lifecycle Hooks:**
```java
@Configuration
public class AppConfig {
    
    @Bean(initMethod = "init", destroyMethod = "cleanup")
    public MyService myService() {
        return new MyService();
    }
}

public class MyService {
    public void init() {
        System.out.println("Bean initialized");
    }
    
    public void cleanup() {
        System.out.println("Bean destroyed");
    }
}
```

---

## 4. REST API Development

### Q16: Design a RESTful API for a User Management System

**Answer:**

**RESTful Design Principles:**
```
Resource-based URLs (nouns, not verbs)
HTTP methods for actions
Status codes for responses
JSON for data exchange
```

**Complete User API:**

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // CREATE: POST /api/v1/users
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.createUser(userDTO);
        return ResponseEntity
            .status(HttpStatus.CREATED)  // 201
            .body(user);
    }
    
    // READ ALL: GET /api/v1/users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String sort
    ) {
        List<User> users = userService.getAllUsers(page, size, sort);
        return ResponseEntity.ok(users);  // 200
    }
    
    // READ ONE: GET /api/v1/users/123
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    // UPDATE (full): PUT /api/v1/users/123
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserDTO userDTO
    ) {
        User user = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(user);
    }
    
    // UPDATE (partial): PATCH /api/v1/users/123
    @PatchMapping("/{id}")
    public ResponseEntity<User> partialUpdate(
        @PathVariable Long id,
        @RequestBody Map<String, Object> updates
    ) {
        User user = userService.partialUpdate(id, updates);
        return ResponseEntity.ok(user);
    }
    
    // DELETE: DELETE /api/v1/users/123
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();  // 204
    }
    
    // SEARCH: GET /api/v1/users/search?email=john@example.com
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String name
    ) {
        List<User> users = userService.searchUsers(email, name);
        return ResponseEntity.ok(users);
    }
    
    // Nested resource: GET /api/v1/users/123/orders
    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = userService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
}
```

**Models:**

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String password;  // Should be hashed!
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Getters and setters
}

@Data
public class UserDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

**API Response Wrapper:**

```java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}

// Usage in controller
@PostMapping
public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserDTO userDTO) {
    User user = userService.createUser(userDTO);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(user, "User created successfully"));
}
```

**REST API Best Practices:**

```
✅ Good API Design:
GET    /api/v1/users              → Get all users
GET    /api/v1/users/123          → Get specific user
POST   /api/v1/users              → Create user
PUT    /api/v1/users/123          → Update entire user
PATCH  /api/v1/users/123          → Update partial user
DELETE /api/v1/users/123          → Delete user
GET    /api/v1/users/123/orders   → Get user's orders

❌ Bad API Design:
GET    /api/v1/getAllUsers
POST   /api/v1/createUser
GET    /api/v1/user?id=123
POST   /api/v1/deleteUser/123     → Should be DELETE
GET    /api/v1/updateUser         → Should be PUT/PATCH
```

**HTTP Status Codes to Use:**

```
2xx Success:
  200 OK - Successful GET, PUT, PATCH
  201 Created - Successful POST
  204 No Content - Successful DELETE

4xx Client Errors:
  400 Bad Request - Invalid data
  401 Unauthorized - Not authenticated
  403 Forbidden - No permission
  404 Not Found - Resource doesn't exist
  409 Conflict - Duplicate resource

5xx Server Errors:
  500 Internal Server Error
  503 Service Unavailable
```

---

### Q17: How to handle pagination and sorting in REST API?

**Answer:**

**Using Spring Data Pageable:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // GET /api/users?page=0&size=10&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<User>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSortParams(sort)));
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    private Sort.Order[] parseSortParams(String[] sort) {
        return Arrays.stream(sort)
            .map(s -> {
                String[] parts = s.split(",");
                String property = parts[0];
                Sort.Direction direction = parts.length > 1 && 
                    parts[1].equalsIgnoreCase("desc") ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
                return new Sort.Order(direction, property);
            })
            .toArray(Sort.Order[]::new);
    }
}
```

**Service Layer:**

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
```

**Repository (No code needed!):**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository already has findAll(Pageable pageable)!
}
```

**Response Example:**

```json
{
  "content": [
    {"id": 1, "name": "Alice", "email": "alice@example.com"},
    {"id": 2, "name": "Bob", "email": "bob@example.com"}
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {"sorted": true, "unsorted": false}
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

**Custom Pagination Response:**

```java
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            page.isFirst()
        );
    }
}

// Usage
@GetMapping
public ResponseEntity<PageResponse<User>> getUsers(Pageable pageable) {
    Page<User> page = userService.getAllUsers(pageable);
    return ResponseEntity.ok(PageResponse.of(page));
}
```

**Advanced: Custom Query with Pagination**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom query with pagination
    @Query("SELECT u FROM User u WHERE u.active = :active")
    Page<User> findByActive(@Param("active") boolean active, Pageable pageable);
    
    // Search with pagination
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

// Usage in controller
@GetMapping("/search")
public ResponseEntity<Page<User>> searchUsers(
    @RequestParam String name,
    @PageableDefault(size = 20, sort = "name") Pageable pageable
) {
    Page<User> users = userRepository.findByNameContainingIgnoreCase(name, pageable);
    return ResponseEntity.ok(users);
}
```

**❌ Common Mistakes:**

```java
// Mistake 1: Not using Pageable
@GetMapping("/users")
public List<User> getAllUsers() {
    return userRepository.findAll();  // ❌ Returns ALL users! Memory issue!
}

// Mistake 2: Wrong sort parameter format
// Wrong: ?sort=name-desc
// Correct: ?sort=name,desc

// Mistake 3: Not setting default values
@GetMapping("/users")
public Page<User> getUsers(@RequestParam int page, @RequestParam int size) {
    // ❌ Required parameters - bad UX
}

// ✅ Correct:
@GetMapping("/users")
public Page<User> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) { }
```

---

### Q18: How to implement API versioning?

**Answer:**

**4 Common Versioning Strategies:**

**1. URI Path Versioning (✅ Most Common)**
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    @GetMapping
    public List<UserV1DTO> getUsers() {
        return userService.getUsersV1();
    }
}

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    @GetMapping
    public List<UserV2DTO> getUsers() {
        return userService.getUsersV2();
    }
}
```

**Pros:** Clear, easy to test, cache-friendly
**Cons:** URL pollution

**2. Request Parameter Versioning**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // /api/users?version=1
    @GetMapping(params = "version=1")
    public List<UserV1DTO> getUsersV1() { }
    
    // /api/users?version=2
    @GetMapping(params = "version=2")
    public List<UserV2DTO> getUsersV2() { }
}
```

**3. Header Versioning**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Header: X-API-Version: 1
    @GetMapping(headers = "X-API-Version=1")
    public List<UserV1DTO> getUsersV1() { }
    
    // Header: X-API-Version: 2
    @GetMapping(headers = "X-API-Version=2")
    public List<UserV2DTO> getUsersV2() { }
}
```

**4. Media Type Versioning (Content Negotiation)**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Header: Accept: application/vnd.myapp.v1+json
    @GetMapping(produces = "application/vnd.myapp.v1+json")
    public List<UserV1DTO> getUsersV1() { }
    
    // Header: Accept: application/vnd.myapp.v2+json
    @GetMapping(produces = "application/vnd.myapp.v2+json")
    public List<UserV2DTO> getUsersV2() { }
}
```

**PayPal/Capital One Style (URI Path):**

```java
// Project structure
com.company.api
├── v1
│   ├── controller
│   │   └── UserController.java
│   ├── dto
│   │   └── UserDTO.java
│   └── service
│       └── UserService.java
└── v2
    ├── controller
    │   └── UserController.java
    ├── dto
    │   └── UserDTO.java
    └── service
        └── UserService.java

// V1 User
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @GetMapping("/{id}")
    public UserV1DTO getUser(@PathVariable Long id) {
        return new UserV1DTO(id, "John", "john@example.com");
    }
}

// V2 User (with additional phone field)
@RestController
@RequestMapping("/api/v2/users")
public class UserController {
    @GetMapping("/{id}")
    public UserV2DTO getUser(@PathVariable Long id) {
        return new UserV2DTO(id, "John", "john@example.com", "+1234567890");
    }
}
```

**Version Deprecation Strategy:**

```java
@RestController
@RequestMapping("/api/v1/users")
@Deprecated  // Mark old version as deprecated
public class UserControllerV1 {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Deprecated", "true");
        headers.add("X-API-Deprecation-Date", "2025-12-31");
        headers.add("X-API-Migration-Guide", "https://api.example.com/docs/v1-to-v2");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(userService.getUser(id));
    }
}
```

**Best Practices:**

```
✅ Good Practices:
- Use URI path versioning (simplest)
- Version only when breaking changes
- Support 2-3 versions maximum
- Document migration path
- Give deprecation notice (6-12 months)

❌ Bad Practices:
- Too many versions (v1, v1.1, v1.2...)
- No deprecation strategy
- Breaking changes without version bump
- Versioning every minor change
```

---

### Q19: Explain Content Negotiation in Spring Boot

**Answer:**

Content Negotiation allows the client to request different response formats (JSON, XML, etc.)

**Enable Content Negotiation:**

```xml
<!-- pom.xml - Add XML support -->
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
</dependency>
```

**Controller Example:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Produces both JSON and XML
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
    
    // Consumes JSON or XML
    @PostMapping(consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}
```

**How Client Requests Different Formats:**

**Method 1: Accept Header (✅ Recommended)**
```
GET /api/users
Accept: application/json      → Returns JSON

GET /api/users
Accept: application/xml       → Returns XML
```

**Method 2: File Extension**
```
GET /api/users.json          → Returns JSON
GET /api/users.xml           → Returns XML
```

**Method 3: Query Parameter**
```
GET /api/users?format=json   → Returns JSON
GET /api/users?format=xml    → Returns XML
```

**Configuration:**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)  // Enable ?format=json
            .parameterName("format")
            .ignoreAcceptHeader(false)  // Enable Accept header
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML);
    }
}
```

**Response Examples:**

**JSON Response:**
```json
[
  {
    "id": 1,
    "name": "John",
    "email": "john@example.com"
  }
]
```

**XML Response:**
```xml
<List>
  <User>
    <id>1</id>
    <name>John</name>
    <email>john@example.com</email>
  </User>
</List>
```

**Custom Message Converter:**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Add custom CSV converter
        converters.add(new CsvMessageConverter());
    }
}

public class CsvMessageConverter extends AbstractHttpMessageConverter<List<?>> {
    
    public CsvMessageConverter() {
        super(new MediaType("text", "csv"));
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }
    
    @Override
    protected List<?> readInternal(Class<? extends List<?>> clazz, HttpInputMessage inputMessage) {
        // CSV to Object
        return null;
    }
    
    @Override
    protected void writeInternal(List<?> list, HttpOutputMessage outputMessage) throws IOException {
        // Object to CSV
        Writer writer = new OutputStreamWriter(outputMessage.getBody());
        // Write CSV logic
    }
}
```

---

### Q20: How to implement HATEOAS in REST API?

**Answer:**

**HATEOAS** = Hypermedia As The Engine Of Application State (Links to related resources)

**Add Dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

**Without HATEOAS (Basic Response):**
```json
{
  "id": 1,
  "name": "John",
  "email": "john@example.com"
}
```

**With HATEOAS (Response with Links):**
```json
{
  "id": 1,
  "name": "John",
  "email": "john@example.com",
  "_links": {
    "self": {"href": "http://localhost:8080/api/users/1"},
    "orders": {"href": "http://localhost:8080/api/users/1/orders"},
    "update": {"href": "http://localhost:8080/api/users/1"},
    "delete": {"href": "http://localhost:8080/api/users/1"}
  }
}
```

**Implementation:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public EntityModel<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        
        // Add links
        EntityModel<User> resource = EntityModel.of(user);
        
        // Self link
        resource.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
        
        // Orders link
        resource.add(linkTo(methodOn(UserController.class).getUserOrders(id)).withRel("orders"));
        
        // All users link
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
        
        return resource;
    }
    
    @GetMapping
    public CollectionModel<EntityModel<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        
        List<EntityModel<User>> userResources = users.stream()
            .map(user -> EntityModel.of(user,
                linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getUserOrders(user.getId())).withRel("orders")
            ))
            .collect(Collectors.toList());
        
        return CollectionModel.of(userResources,
            linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
    }
    
    @GetMapping("/{id}/orders")
    public List<Order> getUserOrders(@PathVariable Long id) {
        return userService.getUserOrders(id);
    }
}
```

**Using RepresentationModelAssembler (✅ Better approach):**

```java
@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {
    
    @Override
    public EntityModel<User> toModel(User user) {
        return EntityModel.of(user,
            linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel(),
            linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"),
            linkTo(methodOn(UserController.class).getUserOrders(user.getId())).withRel("orders")
        );
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserModelAssembler assembler;
    
    @GetMapping("/{id}")
    public EntityModel<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return assembler.toModel(user);  // ✅ Clean and reusable
    }
    
    @GetMapping
    public CollectionModel<EntityModel<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return CollectionModel.of(
            users.stream().map(assembler::toModel).collect(Collectors.toList()),
            linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel()
        );
    }
}
```

**Benefits of HATEOAS:**
```
✅ Self-documenting API
✅ Client doesn't need to know URLs
✅ Easy to change URLs without breaking clients
✅ Discoverable API
```

**When to Use:**
- Public APIs
- Long-lived APIs
- APIs with complex navigation

**When NOT to Use:**
- Simple internal APIs
- Mobile apps (extra data overhead)
- Performance-critical APIs

---

## 5. Data Access & JPA

### Q21: Explain JPA, Hibernate, and Spring Data JPA relationship

**Answer:**

```
┌──────────────────────────────────────────┐
│         Your Application Code            │
├──────────────────────────────────────────┤
│       Spring Data JPA                    │  ← Simplifies JPA usage
│   (Automatic repository implementation)  │
├──────────────────────────────────────────┤
│            JPA (Specification)           │  ← Standard interface
│  (javax.persistence.* annotations)       │
├──────────────────────────────────────────┤
│        Hibernate (Implementation)        │  ← Actual implementation
│   (Translates Java to SQL)               │
├──────────────────────────────────────────┤
│             JDBC                         │  ← Database driver
├──────────────────────────────────────────┤
│         Database (MySQL, PostgreSQL)     │
└──────────────────────────────────────────┘
```

**Explanation:**

**JPA (Java Persistence API):**
- It's a **specification** (interface/standard)
- Defines how to map Java objects to database tables
- Defines annotations like @Entity, @Table, @Column

**Hibernate:**
- It's an **implementation** of JPA
- Actual code that does the work
- Can be used standalone or with JPA

**Spring Data JPA:**
- Built on top of JPA
- Eliminates boilerplate code
- Auto-generates implementation

**Code Comparison:**

```java
// Without Spring Data JPA (Manual JDBC)
public class UserDAO {
    public User findById(Long id) {
        Connection conn = DriverManager.getConnection(url, user, password);
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        // Manual mapping...
        return user;
    }
}

// With JPA + Hibernate (Better, but still code)
public class UserDAO {
    @PersistenceContext
    private EntityManager em;
    
    public User findById(Long id) {
        return em.find(User.class, id);
    }
}

// With Spring Data JPA (Best - No code!)
public interface UserRepository extends JpaRepository<User, Long> {
    // That's it! findById is already implemented!
}
```

---

### Q22: Create a complete JPA Entity with all relationships

**Answer:**

```java
// USER ENTITY (One side)
@Entity
@Table(name = "users", 
    indexes = {@Index(name = "idx_email", columnList = "email")},
    uniqueConstraints = {@UniqueConstraint(columnNames = "email")}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-One: User has one Profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;
    
    // One-to-Many: User has many Orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    
    // Many-to-Many: User has many Roles
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}

// USER PROFILE ENTITY (One-to-One)
@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 500)
    private String bio;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}

// ORDER ENTITY (Many-to-One)
@Entity
@Table(name = "orders")
@Data
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @CreationTimestamp
    private LocalDateTime orderDate;
    
    // Many-to-One: Many orders belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // One-to-Many: Order has many items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // Helper methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
}

// ORDER ITEM ENTITY
@Entity
@Table(name = "order_items")
@Data
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}

// PRODUCT ENTITY
@Entity
@Table(name = "products")
@Data
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stock;
    
    // Many-to-Many: Product can have many categories
    @ManyToMany
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
}

// CATEGORY ENTITY
@Entity
@Table(name = "categories")
@Data
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @ManyToMany(mappedBy = "categories")
    private Set<Product> products = new HashSet<>();
}
```

**Relationship Diagram:**

```mermaid
erDiagram
    User ||--o| UserProfile : has
    User ||--o{ Order : places
    Order ||--o{ OrderItem : contains
    Product ||--o{ OrderItem : "ordered in"
    Product }o--o{ Category : "belongs to"
    
    User {
        Long id PK
        String name
        String email
        String password
    }
    
    UserProfile {
        Long id PK
        Long user_id FK
        String bio
        String phone
    }
    
    Order {
        Long id PK
        Long user_id FK
        String orderNumber
        BigDecimal total
    }
    
    OrderItem {
        Long id PK
        Long order_id FK
        Long product_id FK
        Integer quantity
    }
    
    Product {
        Long id PK
        String name
        BigDecimal price
    }
```

**Enums:**

```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

public enum Role {
    CUSTOMER,
    ADMIN,
    SELLER
}
```

**❌ Common Mistakes:**

```java
// Mistake 1: Bi-directional relationship without mappedBy
@Entity
public class User {
    @OneToMany  // ❌ Missing mappedBy
    private List<Order> orders;
}

// ✅ Correct:
@OneToMany(mappedBy = "user")
private List<Order> orders;

// Mistake 2: Using FetchType.EAGER everywhere
@OneToMany(fetch = FetchType.EAGER)  // ❌ N+1 problem!
private List<Order> orders;

// ✅ Correct:
@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
private List<Order> orders;

// Mistake 3: Forgetting cascade and orphanRemoval
@OneToMany(mappedBy = "user")  // ❌ Items won't be deleted with parent
private List<Order> orders;

// ✅ Correct:
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Order> orders;
```

---

### Q23: Write Spring Data JPA repository with custom queries

**Answer:**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 1. METHOD NAME QUERIES (Spring generates SQL automatically)
    
    // Find by single field
    Optional<User> findByEmail(String email);
    
    // Find by multiple fields (AND)
    Optional<User> findByEmailAndPassword(String email, String password);
    
    // Find with OR
    List<User> findByNameOrEmail(String name, String email);
    
    // Find with LIKE
    List<User> findByNameContaining(String keyword);
    List<User> findByNameStartingWith(String prefix);
    List<User> findByNameEndingWith(String suffix);
    
    // Case insensitive
    List<User> findByEmailIgnoreCase(String email);
    
    // Comparison operators
    List<User> findByAgeGreaterThan(Integer age);
    List<User> findByAgeLessThan(Integer age);
    List<User> findByAgeBetween(Integer start, Integer end);
    
    // Date queries
    List<User> findByCreatedAtAfter(LocalDateTime date);
    List<User> findByCreatedAtBefore(LocalDateTime date);
    
    // Boolean queries
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();
    
    // Null checks
    List<User> findByPhoneNumberIsNull();
    List<User> findByPhoneNumberIsNotNull();
    
    // IN clause
    List<User> findByIdIn(List<Long> ids);
    
    // Ordering
    List<User> findByActiveOrderByNameAsc(Boolean active);
    List<User> findTop10ByOrderByCreatedAtDesc();
    
    // Exists
    boolean existsByEmail(String email);
    
    // Count
    long countByActive(Boolean active);
    
    // Delete
    void deleteByEmail(String email);
    @Transactional
    int deleteByActiveAndCreatedAtBefore(Boolean active, LocalDateTime date);
    
    
    // 2. @QUERY ANNOTATION (JPQL - Java Persistence Query Language)
    
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findUserByEmail(String email);
    
    // Named parameters (better)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = :active")
    Optional<User> findActiveUserByEmail(@Param("email") String email, 
                                         @Param("active") Boolean active);
    
    // Join queries
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :userId")
    Optional<User> findUserWithOrders(@Param("userId") Long userId);
    
    // Custom projection
    @Query("SELECT u.name as name, u.email as email FROM User u WHERE u.active = true")
    List<UserSummary> findActiveUsersSummary();
    
    // Pagination with JPQL
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    
    // 3. NATIVE SQL QUERIES
    
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    Optional<User> findByEmailNative(String email);
    
    // Complex native query
    @Query(value = """
        SELECT u.* FROM users u
        INNER JOIN orders o ON u.id = o.user_id
        WHERE o.status = :status
        GROUP BY u.id
        HAVING COUNT(o.id) > :minOrders
        """, nativeQuery = true)
    List<User> findUsersWithMinOrders(@Param("status") String status, 
                                      @Param("minOrders") int minOrders);
    
    
    // 4. @MODIFYING QUERIES (UPDATE/DELETE)
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.active = :active WHERE u.id = :userId")
    int updateUserStatus(@Param("userId") Long userId, @Param("active") Boolean active);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.active = false AND u.createdAt < :date")
    int deleteInactiveUsers(@Param("date") LocalDateTime date);
    
    
    // 5. DTO PROJECTIONS
    
    @Query("SELECT new com.example.dto.UserDTO(u.id, u.name, u.email) " +
           "FROM User u WHERE u.active = true")
    List<UserDTO> findAllActiveUsersAsDTO();
    
    
    // 6. AGGREGATION QUERIES
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersCreatedAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    @Query("SELECT AVG(SIZE(u.orders)) FROM User u")
    Double getAverageOrdersPerUser();
}

// PROJECTION INTERFACE
public interface UserSummary {
    String getName();
    String getEmail();
    LocalDateTime getCreatedAt();
}

// DTO CLASS
@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
}
```

**Usage in Service:**

```java
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Method name query
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    // Custom JPQL query
    public User getUserWithOrders(Long userId) {
        return userRepository.findUserWithOrders(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    // Search with pagination
    public Page<User> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return userRepository.searchUsers(keyword, pageable);
    }
    
    // Update query
    public void deactivateUser(Long userId) {
        int updated = userRepository.updateUserStatus(userId, false);
        if (updated == 0) {
            throw new ResourceNotFoundException("User not found");
        }
    }
    
    // Batch delete
    @Scheduled(cron = "0 0 2 * * ?")  // Run at 2 AM daily
    public void cleanupInactiveUsers() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(90);
        int deleted = userRepository.deleteInactiveUsers(thresholdDate);
        log.info("Deleted {} inactive users", deleted);
    }
}
```

**Query Method Keywords Reference:**

```
Keyword              | Example                           | JPQL snippet
---------------------|-----------------------------------|---------------------------
And                  | findByNameAndEmail                | ... where x.name = ?1 and x.email = ?2
Or                   | findByNameOrEmail                 | ... where x.name = ?1 or x.email = ?2
Is, Equals           | findByName, findByNameIs          | ... where x.name = ?1
Between              | findByAgeBetween                  | ... where x.age between ?1 and ?2
LessThan             | findByAgeLessThan                 | ... where x.age < ?1
GreaterThan          | findByAgeGreaterThan              | ... where x.age > ?1
After                | findByCreatedAtAfter              | ... where x.createdAt > ?1
Before               | findByCreatedAtBefore             | ... where x.createdAt < ?1
IsNull               | findByAgeIsNull                   | ... where x.age is null
IsNotNull            | findByAgeIsNotNull                | ... where x.age is not null
Like                 | findByNameLike                    | ... where x.name like ?1
NotLike              | findByNameNotLike                 | ... where x.name not like ?1
StartingWith         | findByNameStartingWith            | ... where x.name like ?1%
EndingWith           | findByNameEndingWith              | ... where x.name like %?1
Containing           | findByNameContaining              | ... where x.name like %?1%
OrderBy              | findByAgeOrderByNameDesc          | ... where x.age = ?1 order by x.name desc
Not                  | findByNameNot                     | ... where x.name <> ?1
In                   | findByAgeIn(Collection ages)      | ... where x.age in ?1
NotIn                | findByAgeNotIn(Collection ages)   | ... where x.age not in ?1
True                 | findByActiveTrue()                | ... where x.active = true
False                | findByActiveFalse()               | ... where x.active = false
IgnoreCase           | findByNameIgnoreCase              | ... where UPPER(x.name) = UPPER(?1)
```

**❌ Common Mistakes:**

```java
// Mistake 1: Not using @Transactional with @Modifying
@Modifying  // ❌ Will throw exception!
@Query("UPDATE User u SET u.name = :name WHERE u.id = :id")
int updateName(Long id, String name);

// ✅ Correct:
@Modifying
@Transactional
@Query("UPDATE User u SET u.name = :name WHERE u.id = :id")
int updateName(Long id, String name);

// Mistake 2: N+1 problem with lazy loading
List<User> users = userRepository.findAll();
for (User user : users) {
    user.getOrders().size();  // ❌ Triggers separate query for each user!
}

// ✅ Correct: Use JOIN FETCH
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();

// Mistake 3: Wrong native query syntax
@Query(value = "SELECT * FROM User WHERE email = :email", nativeQuery = true)  // ❌ Table name is 'users' not 'User'
Optional<User> findByEmail(String email);

// ✅ Correct:
@Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
Optional<User> findByEmail(String email);
```

---

### Q24: Explain N+1 problem and how to solve it

**Answer:**

**N+1 Problem:** When you fetch N records, and then for each record, you execute an additional query (1 extra query per record).

**Example of N+1 Problem:**

```java
// Fetch 100 users
List<User> users = userRepository.findAll();  // 1 query

// For each user, fetch orders
for (User user : users) {
    List<Order> orders = user.getOrders();  // 100 more queries!
    System.out.println(orders.size());
}

// Total: 1 + 100 = 101 queries! 🔥
```

**SQL Queries Executed:**
```sql
-- Query 1: Fetch users
SELECT * FROM users;

-- Query 2-101: For each user
SELECT * FROM orders WHERE user_id = 1;
SELECT * FROM orders WHERE user_id = 2;
SELECT * FROM orders WHERE user_id = 3;
-- ... 97 more queries
SELECT * FROM orders WHERE user_id = 100;
```

**Visual Representation:**

```
Without Fix (N+1):
┌─────────────────────────────────────┐
│  Query 1: SELECT all users          │ ← 1 query
├─────────────────────────────────────┤
│  Query 2: SELECT orders user_id=1   │ ┐
│  Query 3: SELECT orders user_id=2   │ │
│  Query 4: SELECT orders user_id=3   │ │← 100 queries
│  ...                                │ │
│  Query 101: SELECT orders user_id=100│ ┘
└─────────────────────────────────────┘
Total: 101 queries 😱

With Fix (JOIN FETCH):
┌─────────────────────────────────────┐
│  SELECT users & orders in ONE query │ ← 1 query
└─────────────────────────────────────┘
Total: 1 query ✅
```

**Solutions:**

**Solution 1: JOIN FETCH (✅ Best for small datasets)**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Fetch users with orders in single query
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders")
    List<User> findAllWithOrders();
    
    // Fetch single user with orders
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);
    
    // Multiple joins
    @Query("""
        SELECT DISTINCT u FROM User u 
        LEFT JOIN FETCH u.orders o
        LEFT JOIN FETCH o.orderItems
        WHERE u.active = true
        """)
    List<User> findActiveUsersWithOrdersAndItems();
}

// Usage
@Service
public class UserService {
    
    public List<User> getAllUsersWithOrders() {
        return userRepository.findAllWithOrders();  // ✅ Single query!
    }
}
```

**Solution 2: @EntityGraph (✅ Better alternative)**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @EntityGraph(attributePaths = {"orders"})
    List<User> findAll();
    
    @EntityGraph(attributePaths = {"orders", "orders.orderItems"})
    Optional<User> findById(Long id);
    
    // Named entity graph
    @EntityGraph(attributePaths = {"orders", "profile"})
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsersWithOrdersAndProfile();
}
```

**Solution 3: Batch Fetching**

```java
@Entity
public class User {
    
    @OneToMany(mappedBy = "user")
    @BatchSize(size = 10)  // Fetch orders in batches of 10
    private List<Order> orders;
}

// Queries executed:
// SELECT * FROM users;                    -- 1 query
// SELECT * FROM orders WHERE user_id IN (1,2,3,4,5,6,7,8,9,10);  -- Query for first 10
// SELECT * FROM orders WHERE user_id IN (11,12,13,14,15,16,17,18,19,20); -- Query for next 10
// Total: 1 + 10 = 11 queries (much better than 101!)
```

**Solution 4: DTO Projections with JOIN (✅ Best for large datasets)**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("""
        SELECT new com.example.dto.UserWithOrdersDTO(
            u.id, u.name, u.email, 
            o.id, o.orderNumber, o.totalAmount
        )
        FROM User u
        LEFT JOIN u.orders o
        """)
    List<UserWithOrdersDTO> findAllUsersWithOrders();
}

@Data
@AllArgsConstructor
public class UserWithOrdersDTO {
    private Long userId;
    private String userName;
    private String userEmail;
    private Long orderId;
    private String orderNumber;
    private BigDecimal orderAmount;
}
```

**Solution 5: Lazy Loading with Explicit Fetching**

```java
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getUsersWithOrders() {
        List<User> users = userRepository.findAll();
        
        // Explicitly initialize lazy collections within transaction
        users.forEach(user -> {
            Hibernate.initialize(user.getOrders());  // Force load
        });
        
        return users;
    }
}
```

**Comparison of Solutions:**

| Solution | Queries | Memory | Use Case |
|----------|---------|--------|----------|
| JOIN FETCH | 1 | High | Small datasets (<1000) |
| @EntityGraph | 1 | High | Small datasets, cleaner code |
| Batch Fetching | Few | Medium | Medium datasets |
| DTO Projections | 1 | Low | Large datasets, reporting |
| Lazy + Manual | Few | Medium | Complex scenarios |

**How to Detect N+1:**

```properties
# application.properties
# Show SQL queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Show query statistics
logging.level.org.hibernate.stat=DEBUG
spring.jpa.properties.hibernate.generate_statistics=true
```

**Real Interview Scenario:**

```java
// ❌ Bad code (N+1 problem)
@GetMapping("/users")
public List<UserDTO> getAllUsers() {
    List<User> users = userRepository.findAll();  // Query 1
    
    return users.stream()
        .map(user -> new UserDTO(
            user.getId(),
            user.getName(),
            user.getOrders().size()  // Query 2, 3, 4... N+1!
        ))
        .collect(Collectors.toList());
}

// ✅ Good code (Optimized)
@GetMapping("/users")
public List<UserDTO> getAllUsers() {
    // Single query with JOIN
    List<User> users = userRepository.findAllWithOrders();
    
    return users.stream()
        .map(user -> new UserDTO(
            user.getId(),
            user.getName(),
            user.getOrders().size()  // Already loaded!
        ))
        .collect(Collectors.toList());
}
```

---

### Q25: What is the difference between save() and saveAndFlush()?

**Answer:**

```java
// save() - Queues the operation
User user = new User("John", "john@example.com");
userRepository.save(user);  
// SQL not executed yet! Waits for transaction commit or flush

// saveAndFlush() - Executes immediately
User user = new User("John", "john@example.com");
userRepository.saveAndFlush(user);
// SQL executed immediately! INSERT statement runs now
```

**Detailed Comparison:**

| Feature | save() | saveAndFlush() |
|---------|--------|----------------|
| SQL Execution | Deferred | Immediate |
| Performance | Faster (batching) | Slower |
| Use Case | Normal operations | Need immediate DB constraint check |
| Transaction | Waits for commit | Flushes immediately |

**Example Scenario:**

```java
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Example 1: save() - SQL deferred
    public void createUsers() {
        User user1 = new User("John", "john@example.com");
        User user2 = new User("Jane", "jane@example.com");
        
        userRepository.save(user1);  // SQL not executed
        userRepository.save(user2);  // SQL not executed
        
        // Both INSERTs executed together when transaction commits
        // Benefit: Can be batched for better performance
    }
    
    // Example 2: saveAndFlush() - Need ID immediately
    public void createUserAndAssignRole() {
        User user = new User("John", "john@example.com");
        
        userRepository.saveAndFlush(user);  // SQL executed immediately
        
        // Now user.getId() has the database-generated ID
        Long userId = user.getId();  // ✅ Available immediately
        
        // Can use the ID right away
        Role role = new Role(userId, "ADMIN");
        roleRepository.save(role);
    }
    
    // Example 3: Checking DB constraints
    public void createUserWithUniqueCheck() {
        User user = new User("John", "john@example.com");
        
        try {
            userRepository.saveAndFlush(user);  // Throws exception immediately if email exists
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Email already exists");
        }
    }
}
```

**When to Use saveAndFlush():**

```java
// ✅ Use saveAndFlush() when:

// 1. Need database-generated ID immediately
User user = userRepository.saveAndFlush(new User("John"));
Long id = user.getId();  // Use ID right away

// 2. Check database constraints immediately
try {
    userRepository.saveAndFlush(user);
} catch (DataIntegrityViolationException e) {
    // Handle duplicate email
}

// 3. Mixed read/write in same transaction
userRepository.saveAndFlush(newUser);
List<User> users = userRepository.findAll();  // Should include newUser

// 4. Debugging (see SQL immediately in logs)
userRepository.saveAndFlush(user);  // SQL logged immediately
```

**When to Use save():**

```java
// ✅ Use save() when:

// 1. Batch operations (better performance)
List<User> users = getUsers();
users.forEach(userRepository::save);  // All flushed together

// 2. Normal CRUD operations
userRepository.save(user);  // Let Spring handle flush timing

// 3. Don't need ID immediately
User user = userRepository.save(newUser);
// ID available after transaction commit
```

**Performance Impact:**

```java
// ❌ Poor performance
for (int i = 0; i < 1000; i++) {
    User user = new User("User" + i);
    userRepository.saveAndFlush(user);  // 1000 separate DB calls!
}

// ✅ Better performance
for (int i = 0; i < 1000; i++) {
    User user = new User("User" + i);
    userRepository.save(user);  // Batched into fewer calls
}
```

---

## 6. Exception Handling

### Q26: Create a global exception handler for REST API

**Answer:**

**Complete Exception Handling Architecture:**

```java
// 1. CUSTOM EXCEPTIONS

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}


// 2. ERROR RESPONSE DTO

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<String> errors;  // For validation errors
    
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}


// 3. GLOBAL EXCEPTION HANDLER

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // Handle custom exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
        ResourceNotFoundException ex,
        WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        log.error("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid input data")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .errors(errors)
            .build();
        
        log.error("Validation error: {}", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    // Handle duplicate key violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        WebRequest request
    ) {
        String message = "Database constraint violation";
        
        if (ex.getCause() instanceof ConstraintViolationException) {
            message = "Duplicate entry or constraint violation";
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            message,
            request.getDescription(false).replace("uri=", "")
        );
        
        log.error("Data integrity violation: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    // Handle method argument type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        WebRequest request
    ) {
        String message = String.format(
            "Parameter '%s' should be of type %s",
            ex.getName(),
            ex.getRequiredType().getSimpleName()
        );
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // Handle missing request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
        MissingServletRequestParameterException ex,
        WebRequest request
    ) {
        String message = String.format(
            "Required parameter '%s' is missing",
            ex.getParameterName()
        );
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // Handle HTTP message not readable
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON",
            "Invalid JSON format or missing required fields",
            request.getDescription(false).replace("uri=", "")
        );
        
        log.error("Malformed JSON request: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // Handle access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
        AccessDeniedException ex,
        WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "You don't have permission to access this resource",
            request.getDescription(false).replace("uri=", "")
        );
        
        log.warn("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    
    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
        Exception ex,
        WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getDescription(false).replace("uri=", "")
        );
        
        log.error("Unexpected error", ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**Usage in Controller:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // If user not found, service throws ResourceNotFoundException
        // GlobalExceptionHandler catches it and returns proper error response
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        // If validation fails, MethodArgumentNotValidException thrown
        // GlobalExceptionHandler returns validation errors
        User user = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

**Service Layer:**

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    public User createUser(UserDTO userDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("User with this email already exists");
        }
        
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        return userRepository.save(user);
    }
}
```

**Error Response Examples:**

```json
// 404 Not Found
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: '123'",
  "path": "/api/users/123",
  "timestamp": "2025-11-10T10:30:00"
}

// 400 Validation Error
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/users",
  "timestamp": "2025-11-10T10:30:00",
  "errors": [
    "email: must be a well-formed email address",
    "name: must not be blank",
    "password: size must be between 8 and 100"
  ]
}

// 409 Conflict
{
  "status": 409,
  "error": "Conflict",
  "message": "User with this email already exists",
  "path": "/api/users",
  "timestamp": "2025-11-10T10:30:00"
}
```

**❌ Common Mistakes:**

```java
// Mistake 1: Not using @RestControllerAdvice
@ControllerAdvice  // ❌ Returns view names, not JSON
public class GlobalExceptionHandler { }

// ✅ Correct:
@RestControllerAdvice  // Returns JSON responses
public class GlobalExceptionHandler { }

// Mistake 2: Catching exception in controller
@GetMapping("/{id}")
public ResponseEntity<?> getUser(@PathVariable Long id) {
    try {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    } catch (Exception e) {  // ❌ Don't catch here!
        return ResponseEntity.status(500).body("Error");
    }
}

// ✅ Correct: Let GlobalExceptionHandler handle it
@GetMapping("/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.getUserById(id);  // Let exception propagate
    return ResponseEntity.ok(user);
}

// Mistake 3: Not logging exceptions
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    // ❌ No logging!
    return ResponseEntity.status(500).body(error);
}

// ✅ Correct:
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Unexpected error", ex);  // ✅ Log with stack trace
    return ResponseEntity.status(500).body(error);
}
```

---

## 7. Spring Security

### Q27: Implement JWT authentication in Spring Boot

**Answer:**

**Complete JWT Authentication Implementation:**

**1. Dependencies (pom.xml):**

```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**2. JWT Utility Class:**

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    // Generate JWT token
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    // Generate token from username
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    // Get username from token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return claims.getSubject();
    }
    
    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**3. JWT Authentication Filter:**

```java
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (jwt != null && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " prefix
        }
        
        return null;
    }
}
```

**4. User Details Service:**

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found with email: " + email)
            );
        
        return UserPrincipal.create(user);
    }
    
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found with id: " + id)
            );
        
        return UserPrincipal.create(user);
    }
}

// UserPrincipal class
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String name;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
        
        return new UserPrincipal(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

**5. Security Configuration:**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Authenticated endpoints
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

// JWT Authentication Entry Point
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        error.put("error", "Unauthorized");
        error.put("message", authException.getMessage());
        error.put("path", request.getServletPath());
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(error));
    }
}
```

**6. Authentication Controller:**

```java
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    // Login
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return ResponseEntity.ok(new JwtAuthenticationResponse(
            jwt,
            userPrincipal.getId(),
            userPrincipal.getName(),
            userPrincipal.getEmail()
        ));
    }
    
    // Register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        
        // Check if email exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body(new ApiResponse(false, "Email is already taken"));
        }
        
        // Create new user
        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        
        User savedUser = userRepository.save(user);
        
        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
    }
    
    // Get current user
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        
        return ResponseEntity.ok(user);
    }
}

// DTOs
@Data
public class LoginRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
}

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 2, max = 40)
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
}

@Data
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long id;
    private String name;
    private String email;
    
    public JwtAuthenticationResponse(String accessToken, Long id, String name, String email) {
        this.accessToken = accessToken;
        this.id = id;
        this.name = name;
        this.email = email;
    }
}

// Custom annotation to get current user
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser { }
```

**7. Configuration (application.properties):**

```properties
# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
# 86400000 ms = 24 hours

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/myapp
spring.datasource.username=root
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Authentication Flow:**

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthManager
    participant UserDetailsService
    participant JwtProvider
    participant Database

    Client->>AuthController: POST /api/auth/login (email, password)
    AuthController->>AuthManager: authenticate(email, password)
    AuthManager->>UserDetailsService: loadUserByUsername(email)
    UserDetailsService->>Database: findByEmail(email)
    Database-->>UserDetailsService: User data
    UserDetailsService-->>AuthManager: UserDetails
    AuthManager->>AuthManager: Verify password
    AuthManager-->>AuthController: Authentication object
    AuthController->>JwtProvider: generateToken(authentication)
    JwtProvider-->>AuthController: JWT token
    AuthController-->>Client: {token, userId, name, email}
    
    Note over Client: Store token in localStorage
    
    Client->>AuthController: GET /api/users (with JWT in header)
    AuthController->>JwtAuthenticationFilter: Extract & validate JWT
    JwtAuthenticationFilter->>JwtProvider: validateToken(jwt)
    JwtProvider-->>JwtAuthenticationFilter: Valid
    JwtAuthenticationFilter->>UserDetailsService: loadUserByUsername()
    UserDetailsService-->>JwtAuthenticationFilter: UserDetails
    JwtAuthenticationFilter->>AuthController: Set SecurityContext
    AuthController-->>Client: Protected resource data
```

**Testing with Postman:**

```bash
# 1. Register
POST http://localhost:8080/api/auth/register
Body (JSON):
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}

# 2. Login
POST http://localhost:8080/api/auth/login
Body (JSON):
{
  "email": "john@example.com",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}

# 3. Access protected endpoint
GET http://localhost:8080/api/users
Headers:
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Method-Level Security:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Only authenticated users
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public User getProfile(@CurrentUser UserPrincipal currentUser) {
        return userService.getUserById(currentUser.getId());
    }
    
    // Only ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
    
    // Multiple roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    // Custom expression
    @PreAuthorize("@userSecurity.isOwner(#id, principal.id)")
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }
}

// Custom security service
@Service("userSecurity")
public class UserSecurityService {
    public boolean isOwner(Long userId, Long currentUserId) {
        return userId.equals(currentUserId);
    }
}
```

---

### Q28: How to implement role-based access control (RBAC)?

**Answer:**

**Complete RBAC Implementation:**

**1. Entity Models:**

```java
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String password;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    public void addRole(Role role) {
        this.roles.add(role);
    }
}

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}

@Entity
@Table(name = "permissions")
@Data
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String name;  // e.g., "USER_READ", "USER_WRITE", "ADMIN_ACCESS"
    
    private String description;
}

// Enums
public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MANAGER,
    ROLE_SELLER
}
```

**Database Structure:**

```
users                    user_roles               roles
┌────────┬──────┐       ┌─────────┬─────────┐    ┌────────┬────────────┐
│ id(PK) │ name │◄──────┤ user_id │ role_id │───►│ id(PK) │ name       │
├────────┼──────┤       └─────────┴─────────┘    ├────────┼────────────┤
│ 1      │ John │                                 │ 1      │ ROLE_USER  │
│ 2      │ Jane │                                 │ 2      │ ROLE_ADMIN │
└────────┴──────┘                                 └────────┴────────────┘
                                                         ▲
                        role_permissions                 │
                        ┌─────────┬──────────────┐       │
                        │ role_id │ permission_id├───────┘
                        ├─────────┼──────────────┤
                        │ 1       │ 1            │
                        │ 1       │ 2            │
                        │ 2       │ 1            │
                        │ 2       │ 2            │
                        │ 2       │ 3            │
                        └─────────┴──────────────┘
                                    │
    permissions                     ▼
    ┌────────┬──────────────┐
    │ id(PK) │ name         │
    ├────────┼──────────────┤
    │ 1      │ USER_READ    │
    │ 2      │ USER_WRITE   │
    │ 3      │ ADMIN_ACCESS │
    └────────┴──────────────┘
```

**2. Initialize Roles & Permissions:**

```java
@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Create permissions
        Permission userRead = createPermissionIfNotFound("USER_READ", "Read user data");
        Permission userWrite = createPermissionIfNotFound("USER_WRITE", "Create/Update users");
        Permission userDelete = createPermissionIfNotFound("USER_DELETE", "Delete users");
        Permission adminAccess = createPermissionIfNotFound("ADMIN_ACCESS", "Admin panel access");
        
        // Create roles
        Role userRole = createRoleIfNotFound(
            RoleName.ROLE_USER,
            Set.of(userRead)
        );
        
        Role adminRole = createRoleIfNotFound(
            RoleName.ROLE_ADMIN,
            Set.of(userRead, userWrite, userDelete, adminAccess)
        );
        
        Role managerRole = createRoleIfNotFound(
            RoleName.ROLE_MANAGER,
            Set.of(userRead, userWrite)
        );
        
        // Create admin user
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.addRole(adminRole);
            userRepository.save(admin);
            log.info("Admin user created");
        }
    }
    
    private Permission createPermissionIfNotFound(String name, String description) {
        return permissionRepository.findByName(name)
            .orElseGet(() -> {
                Permission permission = new Permission();
                permission.setName(name);
                permission.setDescription(description);
                return permissionRepository.save(permission);
            });
    }
    
    private Role createRoleIfNotFound(RoleName name, Set<Permission> permissions) {
        return roleRepository.findByName(name)
            .orElseGet(() -> {
                Role role = new Role();
                role.setName(name);
                role.setPermissions(permissions);
                return roleRepository.save(role);
            });
    }
}
```

**3. Controller with Role-Based Access:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // Public endpoint - No authentication required
    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is public";
    }
    
    // Authenticated users only
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public User getProfile(@CurrentUser UserPrincipal currentUser) {
        return userService.getUserById(currentUser.getId());
    }
    
    // ADMIN role required
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    // ADMIN or MANAGER role
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public User createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }
    
    // Permission-based access
    @PreAuthorize("hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
    
    // Custom security expression
    @PreAuthorize("@userSecurity.canAccessUser(#id, authentication)")
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }
    
    // Multiple conditions
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
```

**4. Custom Security Service:**

```java
@Service("userSecurity")
public class UserSecurityService {
    
    public boolean canAccessUser(Long userId, Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        
        // Admin can access anyone
        if (hasRole(currentUser, "ROLE_ADMIN")) {
            return true;
        }
        
        // Users can only access themselves
        return userId.equals(currentUser.getId());
    }
    
    public boolean hasPermission(UserPrincipal user, String permission) {
        return user.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(permission));
    }
    
    private boolean hasRole(UserPrincipal user, String role) {
        return user.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}
```

**5. Method Security Annotations:**

```java
// Enable method security
@Configuration
@EnableMethodSecurity(
    prePostEnabled = true,      // @PreAuthorize, @PostAuthorize
    securedEnabled = true,       // @Secured
    jsr250Enabled = true         // @RolesAllowed
)
public class MethodSecurityConfig { }

// Different annotation styles
@Service
public class OrderService {
    
    // Spring Security @PreAuthorize (✅ Most flexible)
    @PreAuthorize("hasRole('ADMIN') or #order.userId == authentication.principal.id")
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }
    
    // @PostAuthorize - Check after method execution
    @PostAuthorize("returnObject.userId == authentication.principal.id")
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }
    
    // @Secured (simpler, less flexible)
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    // JSR-250 @RolesAllowed
    @RolesAllowed("ADMIN")
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
    
    // @PreFilter - Filter collection before method
    @PreFilter("filterObject.userId == authentication.principal.id")
    public List<Order> processOrders(List<Order> orders) {
        // Only orders belonging to current user will be processed
        return orders;
    }
    
    // @PostFilter - Filter collection after method
    @PostFilter("filterObject.userId == authentication.principal.id")
    public List<Order> getMyOrders() {
        // Returns only orders belonging to current user
        return orderRepository.findAll();
    }
}
```

**6. Role Management API:**

```java
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    // Assign role to user
    @PostMapping("/assign")
    public ResponseEntity<?> assignRole(
        @RequestParam Long userId,
        @RequestParam RoleName roleName
    ) {
        roleService.assignRoleToUser(userId, roleName);
        return ResponseEntity.ok(new ApiResponse(true, "Role assigned successfully"));
    }
    
    // Remove role from user
    @PostMapping("/remove")
    public ResponseEntity<?> removeRole(
        @RequestParam Long userId,
        @RequestParam RoleName roleName
    ) {
        roleService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(new ApiResponse(true, "Role removed successfully"));
    }
    
    // Get user roles
    @GetMapping("/user/{userId}")
    public Set<Role> getUserRoles(@PathVariable Long userId) {
        return roleService.getUserRoles(userId);
    }
}

@Service
public class RoleService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Transactional
    public void assignRoleToUser(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        
        user.addRole(role);
        userRepository.save(user);
    }
    
    @Transactional
    public void removeRoleFromUser(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        
        user.getRoles().remove(role);
        userRepository.save(user);
    }
    
    public Set<Role> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return user.getRoles();
    }
}
```

**Authorization Flow:**

```mermaid
graph TD
    A[HTTP Request] --> B{JWT Valid?}
    B -->|No| C[401 Unauthorized]
    B -->|Yes| D[Extract User & Roles]
    D --> E{Has Required Role/Permission?}
    E -->|No| F[403 Forbidden]
    E -->|Yes| G[Execute Method]
    G --> H[Return Response]
```

**Testing Roles:**

```bash
# Login as admin
POST /api/auth/login
{
  "email": "admin@example.com",
  "password": "admin123"
}

# Get token from response
# Use token in subsequent requests

# Access admin endpoint (✅ Success)
GET /api/users
Authorization: Bearer <admin_token>

# Login as regular user
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "user123"
}

# Try to access admin endpoint (❌ 403 Forbidden)
GET /api/users
Authorization: Bearer <user_token>
```

---

## 8. Microservices

### Q29: Explain Microservices Architecture and its components

**Answer:**

**Microservices Architecture:**

```
Monolithic Application:
┌──────────────────────────────────┐
│     Single Application           │
│  ┌────────────────────────────┐  │
│  │   User Management          │  │
│  │   Order Processing         │  │
│  │   Payment Service          │  │
│  │   Inventory Management     │  │
│  │   Notification Service     │  │
│  └────────────────────────────┘  │
│        Single Database           │
└──────────────────────────────────┘

Microservices Architecture:
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│    User     │  │   Order     │  │  Payment    │
│  Service    │  │  Service    │  │  Service    │
│     🗄️      │  │     🗄️      │  │     🗄️      │
└─────────────┘  └─────────────┘  └─────────────┘
       ▲                ▲                ▲
       │                │                │
       └────────────────┼────────────────┘
                        │
                 ┌──────┴───────┐
                 │  API Gateway │
                 └──────────────┘
                        ▲
                        │
                    Client App
```

**Key Components:**

**1. Service Discovery (Eureka)**
- Services register themselves
- Services find each other dynamically
- Handles service instances

```java
// Eureka Server
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

// application.yml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

// Eureka Client (Microservice)
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

// application.yml
spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**2. API Gateway (Spring Cloud Gateway)**
- Single entry point
- Routing
- Load balancing
- Authentication

```java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

// application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE  # Load balanced
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /${segment}
            
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - AddRequestHeader=X-Gateway-Header, Gateway-Value
            
        - id: payment-service
          uri: lb://PAYMENT-SERVICE
          predicates:
            - Path=/api/payments/**

server:
  port: 8080
```

**3. Config Server (Centralized Configuration)**

```java
// Config Server
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

// application.yml
server:
  port: 8888

spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/myorg/config-repo
          default-label: main
          
// Config Client (Microservice)
// bootstrap.yml
spring:
  application:
    name: user-service
  cloud:
    config:
      uri: http://localhost:8888
```

**4. Circuit Breaker (Resilience4j)**

```java
// Dependencies
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>

// Service with Circuit Breaker
@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    @RateLimiter(name = "paymentService")
    public PaymentResponse processPayment(PaymentRequest request) {
        String url = "http://PAYMENT-SERVICE/api/payments";
        return restTemplate.postForObject(url, request, PaymentResponse.class);
    }
    
    // Fallback method
    public PaymentResponse paymentFallback(PaymentRequest request, Exception ex) {
        log.error("Payment service unavailable, using fallback", ex);
        return new PaymentResponse("PENDING", "Payment queued for later processing");
    }
}

// application.yml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 500ms
        
  ratelimiter:
    instances:
      paymentService:
        limit-for-period: 10
        limit-refresh-period: 1s
```

**5. Service-to-Service Communication:**

**Method 1: RestTemplate (Traditional)**
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced  // Enable load balancing
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUserDetails(Long userId) {
        String url = "http://USER-SERVICE/api/users/" + userId;
        return restTemplate.getForObject(url, User.class);
    }
}
```

**Method 2: WebClient (Reactive - ✅ Preferred)**
```java
@Configuration
public class WebClientConfig {
    
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

@Service
public class OrderService {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    public Mono<User> getUserDetails(Long userId) {
        return webClientBuilder.build()
            .get()
            .uri("http://USER-SERVICE/api/users/{id}", userId)
            .retrieve()
            .bodyToMono(User.class);
    }
    
    // Synchronous call (if needed)
    public User getUserDetailsSync(Long userId) {
        return webClientBuilder.build()
            .get()
            .uri("http://USER-SERVICE/api/users/{id}", userId)
            .retrieve()
            .bodyToMono(User.class)
            .block();  // Blocks until response received
    }
}
```

**Method 3: Feign Client (✅ Easiest)**
```java
// Dependency
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

// Enable Feign
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication { }

// Feign Client Interface
@FeignClient(name = "USER-SERVICE", fallback = UserClientFallback.class)
public interface UserClient {
    
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);
    
    @PostMapping("/api/users")
    User createUser(@RequestBody UserDTO userDTO);
}

// Fallback implementation
@Component
public class UserClientFallback implements UserClient {
    
    @Override
    public User getUserById(Long id) {
        return new User(id, "Unknown User", "unavailable@example.com");
    }
    
    @Override
    public User createUser(UserDTO userDTO) {
        throw new ServiceUnavailableException("User service is down");
    }
}

// Usage in Service
@Service
public class OrderService {
    
    @Autowired
    private UserClient userClient;
    
    public Order createOrder(OrderDTO orderDTO) {
        // Call user service using Feign
        User user = userClient.getUserById(orderDTO.getUserId());
        
        // Create order
        Order order = new Order();
        order.setUser(user);
        // ... rest of the logic
        
        return orderRepository.save(order);
    }
}
```

**Complete Microservices Example:**

```
Project Structure:
my-microservices-app/
├── eureka-server/
├── config-server/
├── api-gateway/
├── user-service/
├── order-service/
├── payment-service/
└── notification-service/

Service Communication Flow:
┌────────┐
│ Client │
└───┬────┘
    │
    ▼
┌─────────────┐
│ API Gateway │ :8080
└──────┬──────┘
       │
       ├──────────────────┬──────────────────┐
       │                  │                  │
       ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ User Service │   │Order Service │   │Payment Service│
│    :8081     │   │    :8082     │   │    :8083     │
└──────────────┘   └──────┬───────┘   └──────────────┘
                          │
                          │ (Feign Client)
                          │
                          ▼
                   ┌──────────────┐
                   │Notification  │
                   │  Service     │
                   │    :8084     │
                   └──────────────┘
```

**Microservices Best Practices:**

```
✅ Do:
- One database per service
- Use API Gateway as single entry point
- Implement circuit breakers
- Use centralized logging (ELK Stack)
- Implement distributed tracing (Zipkin)
- Use message queues for async communication (RabbitMQ/Kafka)
- Containerize with Docker
- Orchestrate with Kubernetes

❌ Don't:
- Share databases between services
- Direct service-to-service calls without circuit breakers
- Forget about monitoring and logging
- Ignore security (implement OAuth2)
- Create too many microservices (nano-services)
```

This covers the core Spring Boot topics! Would you like me to continue with more advanced topics like Testing, Performance Optimization, and Scenario-Based questions?

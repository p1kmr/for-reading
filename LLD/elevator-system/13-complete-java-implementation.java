/*
 * ========================================
 * ELEVATOR SYSTEM - COMPLETE IMPLEMENTATION
 * ========================================
 *
 * This file contains the complete implementation of an Elevator System.
 * It demonstrates Low-Level Design (LLD) principles including:
 * - SOLID principles
 * - Design patterns (Singleton, Strategy, Factory)
 * - Clean architecture
 * - Thread safety
 *
 * FOR LEARNING PURPOSES - Beginner-friendly with extensive comments
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

// ========================================
// PART 1: ENUMERATIONS (Type Safety)
// ========================================

/**
 * Direction of elevator movement.
 * Using enum ensures type safety - no invalid values possible.
 */
enum Direction {
    UP,    // Moving upward (increasing floor numbers)
    DOWN,  // Moving downward (decreasing floor numbers)
    IDLE;  // Not moving

    /**
     * Get the opposite direction.
     * Useful when elevator needs to reverse.
     */
    public Direction getOpposite() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            default: return IDLE;
        }
    }
}

/**
 * Operational state of the elevator.
 */
enum ElevatorState {
    IDLE,              // Waiting for requests
    MOVING,            // Traveling between floors
    STOPPED,           // Stopped at a floor (doors open)
    OUT_OF_SERVICE;    // Maintenance mode

    /**
     * Check if elevator is available for requests.
     */
    public boolean isAvailable() {
        return this != OUT_OF_SERVICE;
    }
}

/**
 * Type of request - external (hall call) or internal (car call).
 */
enum RequestType {
    EXTERNAL,  // From outside elevator (UP/DOWN buttons)
    INTERNAL   // From inside elevator (floor buttons)
}

// ========================================
// PART 2: ENTITY CLASSES (Data Holders)
// ========================================

/**
 * Floor represents a single floor in the building.
 * Mostly a data holder - no complex business logic.
 */
class Floor {
    private final int floorNumber;
    private boolean hasUpButton;
    private boolean hasDownButton;

    public Floor(int floorNumber, int maxFloor) {
        this.floorNumber = floorNumber;
        // Ground floor (0): only UP button
        // Top floor: only DOWN button
        // Middle floors: both buttons
        this.hasUpButton = (floorNumber < maxFloor);
        this.hasDownButton = (floorNumber > 0);
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public boolean hasUpButton() {
        return hasUpButton;
    }

    public boolean hasDownButton() {
        return hasDownButton;
    }
}

/**
 * Request represents a passenger's elevator request.
 * Can be external (hall call) or internal (car call).
 *
 * DESIGN PATTERN: Factory methods for convenient creation
 */
class Request {
    private final int floor;
    private final Direction direction;  // For external requests
    private final RequestType type;
    private final long timestamp;       // For FCFS ordering

    // Private constructor - forces use of factory methods
    private Request(int floor, Direction direction, RequestType type) {
        this.floor = floor;
        this.direction = direction;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    // FACTORY METHOD PATTERN: Clear intent in method names

    /**
     * Create an external request (hall call).
     * @param floor Floor where passenger is waiting
     * @param direction Direction passenger wants to go
     */
    public static Request createExternalRequest(int floor, Direction direction) {
        return new Request(floor, direction, RequestType.EXTERNAL);
    }

    /**
     * Create an internal request (car call).
     * Direction is determined by elevator's current position.
     * @param floor Destination floor
     */
    public static Request createInternalRequest(int floor) {
        return new Request(floor, null, RequestType.INTERNAL);
    }

    // Getters
    public int getFloor() { return floor; }
    public Direction getDirection() { return direction; }
    public RequestType getType() { return type; }
    public long getTimestamp() { return timestamp; }

    public boolean isExternal() {
        return type == RequestType.EXTERNAL;
    }

    public boolean isInternal() {
        return type == RequestType.INTERNAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return floor == request.floor &&
               direction == request.direction &&
               type == request.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, direction, type);
    }

    @Override
    public String toString() {
        return String.format("Request{floor=%d, dir=%s, type=%s}",
            floor, direction, type);
    }
}

/**
 * Elevator represents a single elevator car.
 * Stores state and pending requests.
 *
 * DESIGN PRINCIPLE: Single Responsibility - only stores data, no operations
 */
class Elevator {
    private final int elevatorId;
    private int currentFloor;
    private Direction currentDirection;
    private ElevatorState state;

    // SCAN ALGORITHM: Separate queues for UP and DOWN requests
    private List<Request> upRequests;    // Sorted ascending
    private List<Request> downRequests;  // Sorted descending

    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;                    // Start at ground floor
        this.currentDirection = Direction.IDLE;
        this.state = ElevatorState.IDLE;
        this.upRequests = new ArrayList<>();
        this.downRequests = new ArrayList<>();
    }

    /**
     * Add a request to appropriate queue (UP or DOWN).
     * Thread-safe with synchronized keyword.
     */
    public synchronized void addRequest(Request request) {
        int requestFloor = request.getFloor();

        if (requestFloor > currentFloor) {
            // Request is above - add to UP queue
            if (!upRequests.contains(request)) {
                upRequests.add(request);
                sortUpRequests();
            }
        } else if (requestFloor < currentFloor) {
            // Request is below - add to DOWN queue
            if (!downRequests.contains(request)) {
                downRequests.add(request);
                sortDownRequests();
            }
        }
        // If requestFloor == currentFloor, handle immediately (not queued)
    }

    /**
     * Sort UP requests in ascending order (nearest first).
     */
    private void sortUpRequests() {
        upRequests.sort(Comparator.comparingInt(Request::getFloor));
    }

    /**
     * Sort DOWN requests in descending order (nearest first).
     */
    private void sortDownRequests() {
        downRequests.sort((r1, r2) -> Integer.compare(r2.getFloor(), r1.getFloor()));
    }

    /**
     * Get the next request based on current direction (SCAN algorithm).
     * Thread-safe.
     */
    public synchronized Request getNextRequest() {
        if (currentDirection == Direction.UP && !upRequests.isEmpty()) {
            return upRequests.get(0);
        } else if (currentDirection == Direction.DOWN && !downRequests.isEmpty()) {
            return downRequests.get(0);
        } else if (!upRequests.isEmpty()) {
            currentDirection = Direction.UP;
            return upRequests.get(0);
        } else if (!downRequests.isEmpty()) {
            currentDirection = Direction.DOWN;
            return downRequests.get(0);
        } else {
            currentDirection = Direction.IDLE;
            return null;
        }
    }

    /**
     * Remove a request after processing.
     */
    public synchronized void removeRequest(Request request) {
        upRequests.remove(request);
        downRequests.remove(request);
    }

    /**
     * Check if elevator has any pending requests.
     */
    public synchronized boolean hasRequests() {
        return !upRequests.isEmpty() || !downRequests.isEmpty();
    }

    // Getters and setters
    public int getElevatorId() { return elevatorId; }
    public int getCurrentFloor() { return currentFloor; }
    public void setCurrentFloor(int floor) { this.currentFloor = floor; }
    public Direction getDirection() { return currentDirection; }
    public void setDirection(Direction direction) { this.currentDirection = direction; }
    public ElevatorState getState() { return state; }
    public void setState(ElevatorState state) { this.state = state; }

    public synchronized List<Request> getUpRequests() {
        return new ArrayList<>(upRequests);  // Return copy for thread safety
    }

    public synchronized List<Request> getDownRequests() {
        return new ArrayList<>(downRequests);
    }

    public boolean isAvailable() {
        return state.isAvailable();
    }

    @Override
    public String toString() {
        return String.format("Elevator{id=%d, floor=%d, dir=%s, state=%s, up=%d, down=%d}",
            elevatorId, currentFloor, currentDirection, state,
            upRequests.size(), downRequests.size());
    }
}

// ========================================
// PART 3: STRATEGY PATTERN (Scheduling Algorithms)
// ========================================

/**
 * Strategy interface for elevator selection algorithms.
 * DESIGN PATTERN: Strategy Pattern
 * SOLID PRINCIPLE: Open/Closed Principle - open for extension, closed for modification
 */
interface SchedulingStrategy {
    /**
     * Select the best elevator to handle a request.
     * @param elevators List of all available elevators
     * @param request Request to be assigned
     * @return Best elevator for this request, or null if none available
     */
    Elevator selectElevator(List<Elevator> elevators, Request request);
}

/**
 * STRATEGY 1: Select the nearest elevator.
 * Simple distance-based selection.
 */
class NearestElevatorStrategy implements SchedulingStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        return elevators.stream()
            .filter(Elevator::isAvailable)
            .min(Comparator.comparingInt(e ->
                Math.abs(e.getCurrentFloor() - request.getFloor())))
            .orElse(null);
    }
}

/**
 * STRATEGY 2: First-Come-First-Served.
 * Select elevator with fewest pending requests (load balancing).
 */
class FCFSStrategy implements SchedulingStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        return elevators.stream()
            .filter(Elevator::isAvailable)
            .min(Comparator.comparingInt(e ->
                e.getUpRequests().size() + e.getDownRequests().size()))
            .orElse(null);
    }
}

/**
 * STRATEGY 3: SCAN (Elevator Algorithm).
 * Select elevator moving in same direction as request (most efficient).
 */
class SCANStrategy implements SchedulingStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        return elevators.stream()
            .filter(Elevator::isAvailable)
            .max(Comparator.comparingInt(e -> calculateScore(e, request)))
            .orElse(null);
    }

    /**
     * Calculate priority score for an elevator.
     * Higher score = better match.
     */
    private int calculateScore(Elevator elevator, Request request) {
        int score = 0;
        int elevatorFloor = elevator.getCurrentFloor();
        int requestFloor = request.getFloor();
        Direction elevatorDir = elevator.getDirection();
        Direction requestDir = request.getDirection();

        // PRIORITY 1: Elevator moving in same direction towards request (highest priority)
        if (elevatorDir == requestDir) {
            if (elevatorDir == Direction.UP && requestFloor >= elevatorFloor) {
                score += 100;
            } else if (elevatorDir == Direction.DOWN && requestFloor <= elevatorFloor) {
                score += 100;
            }
        }

        // PRIORITY 2: Idle elevator (adjust by distance - closer is better)
        if (elevatorDir == Direction.IDLE) {
            int distance = Math.abs(elevatorFloor - requestFloor);
            score += (50 - distance);
        }

        // PRIORITY 3: Moving in opposite direction (low priority)
        if (elevatorDir != requestDir && elevatorDir != Direction.IDLE) {
            score += 10;
        }

        return score;
    }
}

// ========================================
// PART 4: CONTROLLER LAYER (Business Logic)
// ========================================

/**
 * ElevatorController controls a single elevator's operations.
 * DESIGN PRINCIPLE: Single Responsibility - only controls elevator operations
 * DESIGN PRINCIPLE: Separation of Concerns - logic separate from data (Elevator)
 */
class ElevatorController {
    private final Elevator elevator;
    private final int minFloor;
    private final int maxFloor;
    private boolean doorsOpen;

    // Timing constants (in milliseconds)
    private static final long FLOOR_TRAVEL_TIME = 1000;   // 1 second per floor (faster for demo)
    private static final long DOOR_OPERATION_TIME = 500;  // 0.5 seconds

    public ElevatorController(Elevator elevator, int minFloor, int maxFloor) {
        this.elevator = elevator;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.doorsOpen = false;
    }

    /**
     * Main processing loop - processes all pending requests.
     * This would typically run in a separate thread.
     */
    public void processRequests() {
        while (elevator.hasRequests()) {
            Request nextRequest = elevator.getNextRequest();
            if (nextRequest != null) {
                System.out.printf("[E%d] Processing: %s%n",
                    elevator.getElevatorId(), nextRequest);

                moveToFloor(nextRequest.getFloor());
                elevator.removeRequest(nextRequest);
                updateDirection();
            }
        }

        // No more requests - become idle
        elevator.setDirection(Direction.IDLE);
        elevator.setState(ElevatorState.IDLE);
        System.out.printf("[E%d] Now IDLE%n", elevator.getElevatorId());
    }

    /**
     * Move elevator to target floor.
     */
    private void moveToFloor(int targetFloor) {
        int currentFloor = elevator.getCurrentFloor();

        if (currentFloor == targetFloor) {
            stop();
            return;
        }

        // Determine direction
        Direction direction = (targetFloor > currentFloor) ? Direction.UP : Direction.DOWN;
        elevator.setDirection(direction);
        elevator.setState(ElevatorState.MOVING);

        System.out.printf("[E%d] Moving %s from %d to %d%n",
            elevator.getElevatorId(), direction, currentFloor, targetFloor);

        // Move floor by floor
        while (elevator.getCurrentFloor() != targetFloor) {
            moveOneFloor();
        }
    }

    /**
     * Move one floor in current direction.
     */
    private void moveOneFloor() {
        int currentFloor = elevator.getCurrentFloor();
        Direction direction = elevator.getDirection();

        int nextFloor;
        if (direction == Direction.UP) {
            nextFloor = Math.min(currentFloor + 1, maxFloor);
        } else if (direction == Direction.DOWN) {
            nextFloor = Math.max(currentFloor - 1, minFloor);
        } else {
            return;
        }

        // Simulate travel time
        sleep(FLOOR_TRAVEL_TIME);

        elevator.setCurrentFloor(nextFloor);
        System.out.printf("[E%d] Now at floor %d%n",
            elevator.getElevatorId(), nextFloor);
    }

    /**
     * Stop at current floor - open doors, wait, close doors.
     */
    private void stop() {
        elevator.setState(ElevatorState.STOPPED);
        System.out.printf("[E%d] Stopped at floor %d%n",
            elevator.getElevatorId(), elevator.getCurrentFloor());

        openDoors();
        sleep(2000);  // Wait for passengers
        closeDoors();
    }

    /**
     * Open elevator doors (safety check: not while moving).
     */
    private void openDoors() {
        if (elevator.getState() == ElevatorState.MOVING) {
            System.out.println("ERROR: Cannot open doors while moving!");
            return;
        }

        System.out.printf("[E%d] Opening doors...%n", elevator.getElevatorId());
        sleep(DOOR_OPERATION_TIME);
        doorsOpen = true;
        System.out.printf("[E%d] Doors open%n", elevator.getElevatorId());
    }

    /**
     * Close elevator doors.
     */
    private void closeDoors() {
        System.out.printf("[E%d] Closing doors...%n", elevator.getElevatorId());
        sleep(DOOR_OPERATION_TIME);
        doorsOpen = false;
        System.out.printf("[E%d] Doors closed%n", elevator.getElevatorId());
    }

    /**
     * Update direction based on remaining requests.
     */
    private void updateDirection() {
        Direction currentDirection = elevator.getDirection();

        if (currentDirection == Direction.UP) {
            if (elevator.getUpRequests().isEmpty()) {
                if (!elevator.getDownRequests().isEmpty()) {
                    elevator.setDirection(Direction.DOWN);
                    System.out.printf("[E%d] Switching to DOWN%n", elevator.getElevatorId());
                } else {
                    elevator.setDirection(Direction.IDLE);
                }
            }
        } else if (currentDirection == Direction.DOWN) {
            if (elevator.getDownRequests().isEmpty()) {
                if (!elevator.getUpRequests().isEmpty()) {
                    elevator.setDirection(Direction.UP);
                    System.out.printf("[E%d] Switching to UP%n", elevator.getElevatorId());
                } else {
                    elevator.setDirection(Direction.IDLE);
                }
            }
        }
    }

    /**
     * Helper method for simulating delays.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Elevator getElevator() {
        return elevator;
    }
}

// ========================================
// PART 5: DISPATCHER LAYER (Coordination)
// ========================================

/**
 * ElevatorDispatcher coordinates elevator assignment.
 * Uses a pluggable SchedulingStrategy.
 * DESIGN PATTERN: Strategy Pattern
 * SOLID PRINCIPLE: Dependency Inversion - depends on interface, not concrete class
 */
class ElevatorDispatcher {
    private final List<Elevator> elevators;
    private SchedulingStrategy strategy;

    public ElevatorDispatcher(List<Elevator> elevators, SchedulingStrategy strategy) {
        this.elevators = elevators;
        this.strategy = strategy;
    }

    /**
     * Dispatch an external request to the best elevator.
     */
    public void dispatchRequest(Request request) {
        System.out.printf("Dispatching: %s%n", request);

        Elevator bestElevator = strategy.selectElevator(elevators, request);

        if (bestElevator != null) {
            System.out.printf("Assigned to Elevator %d%n", bestElevator.getElevatorId());
            bestElevator.addRequest(request);
        } else {
            System.out.println("No available elevator!");
        }
    }

    /**
     * Change scheduling strategy at runtime.
     * Demonstrates Strategy Pattern flexibility.
     */
    public void setStrategy(SchedulingStrategy strategy) {
        this.strategy = strategy;
        System.out.printf("Strategy changed to: %s%n",
            strategy.getClass().getSimpleName());
    }

    public List<Elevator> getElevators() {
        return elevators;
    }
}

// ========================================
// PART 6: SYSTEM LAYER (Singleton)
// ========================================

/**
 * ElevatorSystem is the main system coordinator.
 * DESIGN PATTERN: Singleton - only one system per building
 */
class ElevatorSystem {
    // Singleton instance (thread-safe eager initialization)
    private static final ElevatorSystem instance = new ElevatorSystem();

    private List<Elevator> elevators;
    private List<ElevatorController> controllers;
    private ElevatorDispatcher dispatcher;
    private int numFloors;

    // Private constructor prevents external instantiation
    private ElevatorSystem() {
        this.elevators = new ArrayList<>();
        this.controllers = new ArrayList<>();
    }

    /**
     * Get the singleton instance.
     */
    public static ElevatorSystem getInstance() {
        return instance;
    }

    /**
     * Initialize the elevator system.
     * @param numElevators Number of elevators
     * @param numFloors Number of floors (0 to numFloors)
     */
    public void initialize(int numElevators, int numFloors) {
        this.numFloors = numFloors;

        // Create elevators and controllers
        for (int i = 1; i <= numElevators; i++) {
            Elevator elevator = new Elevator(i);
            elevators.add(elevator);

            ElevatorController controller = new ElevatorController(elevator, 0, numFloors);
            controllers.add(controller);
        }

        // Create dispatcher with default strategy
        SchedulingStrategy defaultStrategy = new SCANStrategy();
        this.dispatcher = new ElevatorDispatcher(elevators, defaultStrategy);

        System.out.println("========================================");
        System.out.println("ELEVATOR SYSTEM INITIALIZED");
        System.out.println("========================================");
        System.out.printf("Elevators: %d%n", numElevators);
        System.out.printf("Floors: 0 to %d%n", numFloors);
        System.out.printf("Strategy: %s%n", defaultStrategy.getClass().getSimpleName());
        System.out.println("========================================\n");
    }

    /**
     * External request - passenger presses UP/DOWN button at a floor.
     */
    public void requestElevator(int floor, Direction direction) {
        Request request = Request.createExternalRequest(floor, direction);
        dispatcher.dispatchRequest(request);
    }

    /**
     * Internal request - passenger inside elevator presses floor button.
     */
    public void selectDestination(int elevatorId, int destinationFloor) {
        Elevator elevator = elevators.stream()
            .filter(e -> e.getElevatorId() == elevatorId)
            .findFirst()
            .orElse(null);

        if (elevator != null) {
            Request request = Request.createInternalRequest(destinationFloor);
            elevator.addRequest(request);
            System.out.printf("Internal request: E%d → Floor %d%n",
                elevatorId, destinationFloor);
        }
    }

    /**
     * Process all pending requests (in separate threads in production).
     */
    public void processAllRequests() {
        for (ElevatorController controller : controllers) {
            if (controller.getElevator().hasRequests()) {
                // In production: new Thread(() -> controller.processRequests()).start();
                controller.processRequests();
            }
        }
    }

    /**
     * Change scheduling strategy.
     */
    public void setSchedulingStrategy(SchedulingStrategy strategy) {
        dispatcher.setStrategy(strategy);
    }

    // Getters
    public List<Elevator> getElevators() {
        return new ArrayList<>(elevators);
    }

    public List<ElevatorController> getControllers() {
        return new ArrayList<>(controllers);
    }

    public void printStatus() {
        System.out.println("\n========== ELEVATOR STATUS ==========");
        for (Elevator elevator : elevators) {
            System.out.println(elevator);
        }
        System.out.println("=====================================\n");
    }
}

// ========================================
// PART 7: DEMO / MAIN
// ========================================

/**
 * Demo class showing how to use the Elevator System.
 */
class ElevatorSystemDemo {
    public static void main(String[] args) {
        // Get the singleton instance
        ElevatorSystem system = ElevatorSystem.getInstance();

        // Initialize system: 3 elevators, 11 floors (0-10)
        system.initialize(3, 10);

        // Scenario 1: External requests
        System.out.println("=== SCENARIO 1: External Requests ===\n");
        system.requestElevator(5, Direction.UP);
        system.requestElevator(2, Direction.DOWN);
        system.requestElevator(8, Direction.DOWN);

        system.printStatus();

        // Process requests
        system.processAllRequests();

        // Scenario 2: Internal requests
        System.out.println("\n=== SCENARIO 2: Internal Requests ===\n");
        system.selectDestination(1, 7);
        system.selectDestination(1, 3);

        system.printStatus();
        system.processAllRequests();

        // Scenario 3: Change strategy
        System.out.println("\n=== SCENARIO 3: Change Strategy ===\n");
        system.setSchedulingStrategy(new NearestElevatorStrategy());
        system.requestElevator(6, Direction.UP);
        system.processAllRequests();

        System.out.println("\n=== DEMO COMPLETE ===");
    }
}

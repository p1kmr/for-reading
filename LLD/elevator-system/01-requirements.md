# Phase 1: Requirements Analysis

## Overview
An **Elevator System** is a computer-controlled system that manages multiple elevators in a building. It efficiently handles passenger requests, optimizes elevator movement, and ensures safe and timely transportation between floors.

---

## 1. Functional Requirements

### 1.1 Core Elevator Operations
- **Move Between Floors**: Each elevator can move up or down to any floor within the building range (e.g., Floor 0 to Floor N)
- **Stop at Requested Floors**: Elevator must stop at floors where passengers want to get on or off
- **Open/Close Doors**: Doors open when the elevator reaches a destination floor and close before moving
- **Display Current Floor**: Show the current floor number inside and outside the elevator
- **Display Direction**: Show whether the elevator is moving UP, DOWN, or is IDLE

### 1.2 Request Handling
- **External Requests** (Hall Call): Passengers outside the elevator press UP/DOWN buttons at any floor
  - Each floor has two buttons: UP and DOWN (except top floor has only DOWN, bottom floor has only UP)
  - System must record the floor number and desired direction
- **Internal Requests** (Car Call): Passengers inside the elevator press destination floor buttons
  - Elevator panel has buttons for all floors
  - System must queue these requests and process them in order

### 1.3 Multiple Elevator Management
- **Handle Multiple Elevators**: System manages N elevators (e.g., 3-5 elevators in a building)
- **Dispatch Optimization**: When an external request comes, assign the most suitable elevator based on:
  - Current position of each elevator
  - Current direction of movement
  - Number of pending requests
  - Estimated arrival time

### 1.4 Direction Management
- **Dynamic Direction Update**: Elevator direction changes based on pending requests
  - If moving UP and no more UP requests, switch to DOWN (or IDLE)
  - If moving DOWN and no more DOWN requests, switch to UP (or IDLE)
- **Efficient Movement**: Process all requests in one direction before switching (SCAN/LOOK algorithm)

### 1.5 State Management
- **Track Elevator State**: Each elevator maintains:
  - Current floor position
  - Current direction (UP, DOWN, IDLE)
  - List of pending requests (sorted by floor number)
  - Door state (OPEN, CLOSED, OPENING, CLOSING)
  - Operational state (IDLE, MOVING, STOPPED, OUT_OF_SERVICE)

---

## 2. Non-Functional Requirements

### 2.1 Performance
- **Response Time**: System should assign an elevator within 2-3 seconds of request
- **Wait Time**: Average passenger wait time should be minimized (< 60 seconds in normal conditions)
- **Throughput**: Handle up to 100 requests per minute across all elevators
- **Optimal Movement**: Minimize unnecessary floor stops and direction changes

### 2.2 Scalability
- **Support Multiple Elevators**: Design should easily support 1 to 10+ elevators
- **Building Height**: Handle buildings with 1 to 100+ floors
- **Concurrent Requests**: Process multiple simultaneous requests from different floors

### 2.3 Reliability & Safety
- **No Request Loss**: Every request must be processed; no request should be forgotten
- **Safe Movement**: Elevator should not move with doors open
- **Emergency Handling**: Support emergency stop and priority modes
- **Fault Tolerance**: If one elevator fails, others continue working

### 2.4 Concurrency
- **Thread Safety**: Multiple threads (users) can request elevators simultaneously
- **Synchronized State**: Elevator state updates must be atomic and consistent
- **Avoid Race Conditions**: Prevent multiple elevators responding to the same request
- **Deadlock Prevention**: Ensure system doesn't freeze due to lock contention

### 2.5 Maintainability
- **Extensible Design**: Easy to add new scheduling algorithms (FCFS, SCAN, LOOK, SSTF)
- **Modular Architecture**: Clear separation between elevator logic, request handling, and dispatching
- **Easy Testing**: Components should be testable independently
- **Clear Abstractions**: Use interfaces for strategy patterns

### 2.6 Usability
- **Clear Status Display**: Users see which elevator is coming and its current floor
- **Predictable Behavior**: Elevators behave consistently and logically
- **User Feedback**: Button lights up when pressed, indicating request is registered

---

## 3. Assumptions

1. **Building Structure**:
   - Single building with floors numbered 0 to N (e.g., 0 to 10)
   - All elevators can access all floors (no restricted floors)
   - Floors are equally spaced

2. **Elevator Characteristics**:
   - All elevators have the same speed
   - Travel time between consecutive floors is constant (e.g., 2 seconds)
   - Door open/close time is constant (e.g., 2 seconds)
   - No weight/capacity constraints (simplified model)

3. **Request Behavior**:
   - Passengers don't cancel requests after pressing buttons
   - External request doesn't specify exact destination (only direction)
   - Internal requests are always valid floor numbers

4. **System Initialization**:
   - Initially, all elevators start at ground floor (Floor 0) in IDLE state
   - System is always powered on (no restart scenarios in this design)

5. **Simplifications**:
   - No emergency scenarios (fire, power outage) in basic design
   - No VIP/priority passengers
   - No scheduling based on time patterns (morning rush, evening rush)

---

## 4. Out of Scope (For Basic Design)

- **Advanced Features**:
  - Predictive algorithms using AI/ML
  - Energy optimization modes
  - Access control (security cards)
  - Elevator maintenance scheduling

- **Edge Cases**:
  - Earthquake detection and safe parking
  - Fire emergency mode (all elevators to ground floor)
  - Overload detection and handling

- **UI/UX**:
  - Physical button hardware integration
  - Display screen rendering
  - Audio announcements

---

## 5. System Constraints

1. **Physical Constraints**:
   - Minimum floor: 0 (Ground floor)
   - Maximum floor: Configurable (default 10)
   - Number of elevators: Configurable (default 3)

2. **Time Constraints**:
   - Maximum wait time: 120 seconds (after which request is re-dispatched)
   - Request timeout: None (requests don't expire)

3. **Capacity Constraints**:
   - Request queue size per elevator: Unlimited (in basic design)
   - System-wide pending requests: Unlimited (in basic design)

---

## 6. Success Criteria

The design is successful if:

1. **Correctness**: All requests are processed; no passenger is ignored
2. **Efficiency**: Average wait time is minimized
3. **Extensibility**: New scheduling algorithms can be added with minimal code changes
4. **Clarity**: A beginner can understand the design and implement it
5. **Thread-Safety**: System works correctly under concurrent load

---

## 7. Clarifying Questions (Interview Perspective)

When asked to design an elevator system, ask these questions:

1. **Scale**: How many elevators? How many floors?
2. **Requests**: Do we need to handle both internal and external requests?
3. **Optimization**: Should we optimize for wait time, energy, or both?
4. **Features**: Do we need emergency modes, VIP access, or just basic functionality?
5. **Concurrency**: How many simultaneous users do we expect?
6. **Persistence**: Do we need to save state (for system restarts)?

---

## Summary

This requirements document provides:
- ✅ Clear functional requirements (what the system does)
- ✅ Non-functional requirements (how well it does it)
- ✅ Assumptions and constraints (boundaries)
- ✅ Out-of-scope items (what we won't build)
- ✅ Success criteria (when are we done?)

**Next Step**: Phase 2 - Use Case Diagram to visualize actors and their interactions with the system.

# 🎮 Snake and Ladder Game - Low Level Design (LLD)

## 📚 Complete Beginner-Friendly LLD Tutorial

This comprehensive guide walks you through designing a Snake and Ladder game from scratch, following industry best practices, SOLID principles, and design patterns.

---

## 📋 Table of Contents

1. [Requirements Analysis](#requirements)
2. [Use Case Diagrams](#use-case-diagrams)
3. [Class Diagrams (Step-by-Step)](#class-diagrams)
4. [Relationships & SOLID Principles](#relationships--solid)
5. [Design Patterns](#design-patterns)
6. [Sequence Diagrams](#sequence-diagrams)
7. [Complete Implementation](#implementation)
8. [Interview Q&A](#interview-qa)
9. [Quick Start](#quick-start)
10. [Design Summary](#design-summary)

---

## 🎯 Requirements

### Functional Requirements
- ✅ Standard 10x10 board (100 cells)
- ✅ Configurable snakes and ladders
- ✅ Multiple players (2-6)
- ✅ Dice rolling with random values (1-6)
- ✅ Extra turn on rolling 6
- ✅ Exact number to win (must land on 100)
- ✅ Multiple players can occupy same cell

### Non-Functional Requirements
- ✅ **Modularity**: Clear separation of concerns
- ✅ **Extensibility**: Easy to add features
- ✅ **Maintainability**: Clean, readable code
- ✅ **Testability**: Easy to unit test

**📖 Read More:** [01-requirements.md](./01-requirements.md)

---

## 🎭 Use Case Diagrams

### Main Actors
- **Player**: Human user playing the game
- **Game System**: Orchestrator managing game flow
- **Dice System**: Random number generator
- **Board System**: Board configuration manager

### Key Use Cases
1. Start New Game
2. Roll Dice
3. Move Player Token
4. Check Snake or Ladder
5. Grant Extra Turn
6. Switch Turn
7. Check Win Condition
8. Display Game State
9. Announce Winner

**📖 Read More:** [02-usecase-diagram.md](./02-usecase-diagram.md)

---

## 🏗️ Class Diagrams

### Evolution Through 5 Steps

#### Step 1: Core Entities
- Start with `Player` and `Board`
- Establish basic structure
- **Why:** Foundation for everything else

#### Step 2: Add Dice and Cell
- Introduce `Dice` for randomness
- Add `Cell` for board positions
- **Why:** Core game mechanics

#### Step 3: Polymorphism (Snake & Ladder)
- Create `BoardElement` abstract class
- `Snake` and `Ladder` extend `BoardElement`
- **Why:** Enable polymorphic behavior

#### Step 4: Game Controller
- Add `Game` orchestrator
- Add `GameService` interface
- **Why:** Coordinate all components

#### Step 5: Factories and Strategies
- Add `GameFactory`, `BoardFactory`, `DiceFactory`
- Introduce `DiceStrategy` interface
- Add `GameBuilder` for flexible construction
- **Why:** Maximum flexibility and extensibility

**📖 Read More:**
- [03-class-diagram-steps-1-2.md](./03-class-diagram-steps-1-2.md)
- [04-class-diagram-steps-3-4.md](./04-class-diagram-steps-3-4.md)
- [05-final-class-design.md](./05-final-class-design.md)

---

## 🔗 Relationships & SOLID

### UML Relationships

| Relationship | Symbol | Example | Strength |
|--------------|--------|---------|----------|
| **Dependency** | `- - >` | GameFactory → Game | Weakest |
| **Association** | `──>` | Game → DiceStrategy | Weak |
| **Aggregation** | `◇──>` | Board ◇→ BoardElement | Medium |
| **Composition** | `◆──>` | Board ◆→ Cell | Strong |
| **Inheritance** | `──▷` | Snake ──▷ BoardElement | Strongest |
| **Implementation** | `--▷` | StandardDice --▷ DiceStrategy | Interface |

### SOLID Principles Applied

✅ **Single Responsibility Principle (SRP)**
- Each class has ONE reason to change
- `Game`: Game flow only
- `Player`: Player state only
- `Board`: Board structure only
- `Dice`: Random generation only

✅ **Open/Closed Principle (OCP)**
- Open for extension, closed for modification
- Add new dice types without changing existing code
- Add new board elements without modifying Board class

✅ **Liskov Substitution Principle (LSP)**
- Subclasses substitutable for parent
- Any `DiceStrategy` works in Game
- Any `BoardElement` works in Board

✅ **Interface Segregation Principle (ISP)**
- Small, focused interfaces
- `DiceStrategy` has only `roll()`
- No fat interfaces forcing unnecessary methods

✅ **Dependency Inversion Principle (DIP)**
- Depend on abstractions, not concretions
- Game depends on `DiceStrategy` interface
- Not on `StandardDice` concrete class

**📖 Read More:**
- [06-relationships-and-solid.md](./06-relationships-and-solid.md)
- [07-solid-principles.md](./07-solid-principles.md)

---

## 🎨 Design Patterns

### 1. Strategy Pattern (Behavioral)
**Where:** DiceStrategy interface

```java
public interface DiceStrategy {
    int roll();
}

public class StandardDice implements DiceStrategy { ... }
public class LoadedDice implements DiceStrategy { ... }
public class CustomRangeDice implements DiceStrategy { ... }
```

**Why:** Different dice behaviors, runtime swappable

---

### 2. Factory Method Pattern (Creational)
**Where:** GameFactory, BoardFactory, DiceFactory

```java
public class GameFactory {
    public static Game createStandardGame(List<Player> players) {
        Board board = BoardFactory.createStandardBoard();
        DiceStrategy dice = DiceFactory.createStandardDice();
        return new GameBuilder()
                .setPlayers(players)
                .setBoard(board)
                .setDiceStrategy(dice)
                .build();
    }
}
```

**Why:** Centralize creation, hide complexity

---

### 3. Builder Pattern (Creational)
**Where:** GameBuilder

```java
Game game = new GameBuilder()
                .setPlayers(players)
                .setBoard(board)
                .setDiceStrategy(dice)
                .build();
```

**Why:** Flexible construction, readable API

---

### 4. Template Method Pattern (Behavioral)
**Where:** BoardElement abstract class

```java
public abstract class BoardElement {
    protected int startPosition;
    protected int endPosition;
    public abstract String getMessage();
}
```

**Why:** Common structure, specific implementations

---

### 5. Facade Pattern (Structural)
**Where:** Game class

```java
public class Game {
    public void start() {
        // Hides complexity of Player, Board, Dice interactions
    }
}
```

**Why:** Simple interface to complex subsystem

**📖 Read More:** [05-final-class-design.md](./05-final-class-design.md)

---

## 📊 Sequence Diagrams

### 1. Game Initialization
Shows how GameFactory creates all components and assembles the game.

### 2. Player Turn Flow
Shows complete turn: roll dice → move → check snake/ladder → check win → switch turn.

### 3. Game Completion
Shows win condition check and winner announcement.

**📖 Read More:** [08-sequence-diagrams.md](./08-sequence-diagrams.md)

---

## 💻 Implementation

### Complete Java Code

```java
// Example usage:
List<Player> players = Arrays.asList(
    new Player("P1", "Alice"),
    new Player("P2", "Bob")
);

Game game = GameFactory.createStandardGame(players);
game.start();
```

**Features:**
- ✅ Fully functional game
- ✅ All SOLID principles applied
- ✅ All design patterns implemented
- ✅ Comprehensive comments
- ✅ Production-ready code

**📖 Read More:** [SnakeAndLadderComplete.java](./SnakeAndLadderComplete.java)

---

## 🎤 Interview Q&A

15 common interview questions with detailed answers:

1. Walk me through your design
2. Why Strategy Pattern for Dice?
3. Aggregation vs Composition
4. How to handle concurrent access?
5. How to support different board sizes?
6. What design patterns did you use?
7. How to ensure SOLID principles?
8. How to add "save game" feature?
9. How would you test this design?
10. Common beginner mistakes
11. How to add new board element type?
12. How to add multiplayer support?
13. How to add undo/redo functionality?
14. Performance optimizations
15. How to make it web-based?

**📖 Read More:** [09-interview-qa.md](./09-interview-qa.md)

---

## 🚀 Quick Start

### Compile and Run

```bash
# Compile
javac SnakeAndLadderComplete.java

# Run
java SnakeAndLadderComplete
```

### Example Output

```
========================================
🎮 SNAKE AND LADDER GAME STARTED! 🎮
========================================

📊 Current Positions:
========================================
  Alice: Position 0 ← CURRENT
  Bob: Position 0
========================================

========================================
👤 Alice's turn
========================================
🎲 Rolled: 4
📍 Moved from 0 → 4
🪜 Yay! Climb the ladder from 4 to 14
📍 New position: 14

...

🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉
  CONGRATULATIONS ALICE!
  You won the game!
🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉
```

---

## 📝 Design Summary

### Key Design Decisions

| Decision | Rationale | Benefit |
|----------|-----------|---------|
| **Strategy Pattern for Dice** | Multiple dice behaviors needed | Easy to add new dice types |
| **Factory Pattern** | Complex object creation | Centralized, consistent creation |
| **Builder Pattern** | Many constructor parameters | Flexible, readable construction |
| **Polymorphism for BoardElement** | Snake and Ladder are similar | Uniform handling, easy extension |
| **Composition for Board→Cell** | Cells are part of board | Strong ownership, clear lifecycle |
| **Aggregation for Board→BoardElement** | Elements created externally | Flexible configuration |
| **Dependency Injection** | Game depends on interfaces | Testable, flexible |

---

### Architecture Layers

```
┌─────────────────────────────────────┐
│     CLIENT LAYER (Main)             │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│   FACTORY LAYER                     │
│   GameFactory, BoardFactory         │
└─────────────┬───────────────────────┘
              │
┌─────────────▼───────────────────────┐
│   CONTROLLER LAYER (Game)           │
│   Orchestrates game flow            │
└──────┬──────┬──────────┬────────────┘
       │      │          │
┌──────▼──┐ ┌▼─────┐ ┌──▼────────┐
│ STRATEGY│ │DOMAIN│ │ HIERARCHY │
│  Dice   │ │Player│ │BoardElement
│         │ │Board │ │ ├─Snake   │
└─────────┘ └──────┘ │ └─Ladder  │
                     └───────────┘
```

---

### Extensibility Points

**Easy to extend:**

1. **Add new dice type:**
   ```java
   class WeightedDice implements DiceStrategy { ... }
   ```

2. **Add new board element:**
   ```java
   class Portal extends BoardElement { ... }
   ```

3. **Add new board size:**
   ```java
   Board board = new Board(200);
   ```

4. **Add special rules:**
   ```java
   class SpecialRulesGame extends Game { ... }
   ```

**All without modifying existing code!** ✅ (Open/Closed Principle)

---

## 🎓 Learning Path

### For Beginners

1. Start with [Requirements](./01-requirements.md)
2. Understand [Use Cases](./02-usecase-diagram.md)
3. Follow [Class Diagrams Step-by-Step](./03-class-diagram-steps-1-2.md)
4. Learn [Relationships](./06-relationships-and-solid.md)
5. Study [SOLID Principles](./07-solid-principles.md)
6. Review [Complete Code](./SnakeAndLadderComplete.java)
7. Practice [Interview Q&A](./09-interview-qa.md)

### For Interview Preparation

1. Memorize SOLID principles with examples
2. Practice whiteboard class diagrams
3. Understand design pattern choices
4. Be ready to extend the design
5. Know trade-offs and alternatives

---

## 🔑 Key Takeaways

### 1. Design Principles
- ✅ Each class has single responsibility
- ✅ Design is open for extension
- ✅ Depend on abstractions, not concretions
- ✅ Small, focused interfaces
- ✅ Subclasses are substitutable

### 2. Code Quality
- ✅ Clean, readable code
- ✅ Comprehensive comments
- ✅ Proper validation
- ✅ Meaningful names
- ✅ Testable design

### 3. Extensibility
- ✅ Easy to add new features
- ✅ No modification of existing code
- ✅ Pluggable components
- ✅ Configuration flexibility

---

## 🛠️ Common Mistakes to Avoid

❌ **Don't:**
1. Create God class doing everything
2. Hardcode values (dice range, board size)
3. Use `instanceof` for type checking
4. Make fields public
5. Skip input validation
6. Forget edge cases
7. Tight coupling between classes
8. Not designing for testability

✅ **Do:**
1. Separate concerns into focused classes
2. Parameterize what might vary
3. Use polymorphism
4. Encapsulate data
5. Validate all inputs
6. Handle edge cases
7. Inject dependencies
8. Make code testable

---

## 📚 Additional Resources

### Related Patterns
- **State Pattern**: For game states (PLAYING, PAUSED, ENDED)
- **Observer Pattern**: For game event notifications
- **Command Pattern**: For undo/redo functionality
- **Memento Pattern**: For save/load game state

### Further Reading
- Design Patterns: Elements of Reusable Object-Oriented Software (GoF)
- Clean Code by Robert C. Martin
- Head First Design Patterns
- Effective Java by Joshua Bloch

---

## 🤝 Contributing

This is a learning resource. Feel free to:
- Add more examples
- Improve explanations
- Fix errors
- Suggest enhancements

---

## 📄 License

This is an educational resource. Free to use for learning purposes.

---

## ✨ Author

Created as a comprehensive LLD tutorial for beginners.

---

## 📞 Contact

For questions or clarifications, refer to the [Interview Q&A](./09-interview-qa.md) section.

---

**Happy Learning! 🎉**

Remember: Good design is not about making code work - it's about making code **easy to understand, test, extend, and maintain**!

# SQL Interview Preparation & Learning System

**The most comprehensive SQL learning resource for technical interviews**

---

## Overview

This repository takes you from absolute beginner to advanced SQL proficiency, specifically optimized for technical interviews at FAANG and top tech companies.

### What's Included
- **10-Phase Learning Curriculum** - Progressive skill building
- **100+ Interview Questions** - From easy to hard with solutions
- **200+ Code Examples** - Practical, runnable queries
- **50+ Diagrams** - Visual learning aids
- **Common Mistakes Guide** - Learn from others' errors
- **Interview Strategy Guide** - Ace your SQL interviews

---

## Quick Start

### For Beginners
Start with Phase 1 and work through each phase sequentially.

### For Interview Prep
1. Review the [SQL Cheat Sheet](./code-snippets/sql-cheatsheet.md)
2. Practice the [Top 100 Questions](./interview-questions/top-100-questions.md)
3. Study the [Common Mistakes](./common-mistakes/README.md)
4. Read the [Interview Strategy Guide](./interview-questions/interview-strategy.md)

### For Specific Topics
Jump directly to the relevant phase based on your needs.

---

## Repository Structure

```
feat/SQL/
├── README.md                          # You are here
├── database-foundations/
│   ├── phase-01-basics/               # SQL Fundamentals
│   ├── phase-02-intermediate/         # CRUD Operations
│   ├── phase-03-filtering/            # Filtering & Sorting
│   ├── phase-04-aggregates/           # Aggregate Functions
│   ├── phase-05-joins/                # Joins Deep Dive
│   ├── phase-06-subqueries/           # Subqueries & CTEs
│   ├── phase-07-advanced-functions/   # Advanced Functions
│   ├── phase-08-window-functions/     # Window Functions
│   ├── phase-09-optimization/         # Performance & Optimization
│   └── phase-10-design/               # Database Design
├── interview-questions/
│   ├── top-100-questions.md           # Complete question bank
│   └── interview-strategy.md          # How to ace interviews
├── common-mistakes/
│   └── README.md                      # 27+ mistakes to avoid
├── code-snippets/
│   ├── sample-database-schema.sql     # Practice database
│   └── sql-cheatsheet.md              # Quick reference
└── database-design/                   # Design templates
```

---

## 10-Phase Learning Curriculum

### Beginner Level (Phases 1-3)

| Phase | Topic | Key Skills | Est. Time |
|-------|-------|------------|-----------|
| [**Phase 1**](./database-foundations/phase-01-basics/README.md) | SQL Fundamentals | Database concepts, data types, basic SELECT | 4-6 hours |
| [**Phase 2**](./database-foundations/phase-02-intermediate/README.md) | CRUD Operations | INSERT, UPDATE, DELETE, ORDER BY, LIMIT | 4-6 hours |
| [**Phase 3**](./database-foundations/phase-03-filtering/README.md) | Filtering & Sorting | AND/OR, IN, BETWEEN, LIKE, NULL handling | 4-6 hours |

### Intermediate Level (Phases 4-6)

| Phase | Topic | Key Skills | Est. Time |
|-------|-------|------------|-----------|
| [**Phase 4**](./database-foundations/phase-04-aggregates/README.md) | Aggregate Functions | COUNT, SUM, AVG, GROUP BY, HAVING | 5-7 hours |
| [**Phase 5**](./database-foundations/phase-05-joins/README.md) | Joins Deep Dive | INNER, LEFT, RIGHT, FULL, SELF, CROSS joins | 6-8 hours |
| [**Phase 6**](./database-foundations/phase-06-subqueries/README.md) | Subqueries & CTEs | Nested queries, EXISTS, CTEs | 5-7 hours |

### Advanced Level (Phases 7-10)

| Phase | Topic | Key Skills | Est. Time |
|-------|-------|------------|-----------|
| [**Phase 7**](./database-foundations/phase-07-advanced-functions/README.md) | Advanced Functions | CASE, string/date functions, type casting | 5-7 hours |
| [**Phase 8**](./database-foundations/phase-08-window-functions/README.md) | Window Functions | ROW_NUMBER, RANK, LAG, LEAD, running totals | 6-8 hours |
| [**Phase 9**](./database-foundations/phase-09-optimization/README.md) | Performance | Indexes, EXPLAIN, query optimization, ACID | 6-8 hours |
| [**Phase 10**](./database-foundations/phase-10-design/README.md) | Database Design | ER diagrams, normalization, schema design | 6-8 hours |

**Total Estimated Time: 50-70 hours**

---

## Interview Resources

### [Top 100 Interview Questions](./interview-questions/top-100-questions.md)
Real interview questions from FAANG companies organized by difficulty:
- **Easy (30)**: Basic queries, filtering, simple joins
- **Medium (50)**: Complex joins, subqueries, aggregates
- **Hard (20)**: Window functions, optimization, design

### [Interview Strategy Guide](./interview-questions/interview-strategy.md)
- 5-step problem-solving framework
- 20+ clarifying questions to ask
- Time management strategies
- How to communicate during interviews

### [Common Mistakes](./common-mistakes/README.md)
27+ mistakes organized by category:
- Syntax errors
- NULL handling
- JOIN errors
- Aggregation mistakes
- Performance anti-patterns
- Data modification disasters

### [SQL Cheat Sheet](./code-snippets/sql-cheatsheet.md)
Quick reference for:
- Syntax patterns
- Function reference
- Common query templates
- Interview patterns

---

## Sample Database

All examples use a consistent set of tables. Set up your practice environment:

### [Sample Database Schema](./code-snippets/sample-database-schema.sql)

**Tables included:**
- `employees` - Employee records with departments and managers
- `departments` - Department information with budgets
- `projects` - Project details and timelines
- `employee_projects` - Employee-project assignments
- `customers` - Customer information
- `sales` - Sales transactions

### Quick Setup
```sql
-- Download and run the schema file in your database
source sample-database-schema.sql;

-- Verify setup
SELECT 'employees' AS table_name, COUNT(*) FROM employees
UNION ALL SELECT 'departments', COUNT(*) FROM departments
UNION ALL SELECT 'sales', COUNT(*) FROM sales;
```

---

## Learning Path Recommendations

### Path 1: Complete Beginner (4-6 weeks)
```
Week 1: Phases 1-2 (Fundamentals + CRUD)
Week 2: Phase 3 (Filtering)
Week 3: Phases 4-5 (Aggregates + Joins)
Week 4: Phase 6 (Subqueries)
Week 5: Phases 7-8 (Functions + Windows)
Week 6: Phases 9-10 + Interview Prep
```

### Path 2: Interview Prep (2-3 weeks)
```
Week 1: Review Phases 1-6, focus on Joins & Aggregates
Week 2: Deep dive Phases 7-8 (Advanced Functions + Windows)
Week 3: Top 100 Questions + Common Mistakes + Strategy Guide
```

### Path 3: Quick Refresh (1 week)
```
Days 1-2: Cheat Sheet + Common Mistakes
Days 3-5: Top 100 Questions (Medium + Hard)
Days 6-7: Window Functions + Optimization + Mock interviews
```

---

## Key Topics for Interviews

### Most Commonly Asked Topics

1. **JOINs** (Phase 5) - Asked in 90% of interviews
2. **Window Functions** (Phase 8) - Critical for FAANG
3. **Aggregates + GROUP BY** (Phase 4) - Fundamental
4. **Subqueries** (Phase 6) - Complex problem solving
5. **NULL Handling** (Phase 3) - Subtle but important

### Topics by Company Type

| Company Type | Focus Areas |
|-------------|-------------|
| **FAANG** | Window functions, optimization, system design |
| **Startups** | Practical queries, joins, aggregates |
| **Finance** | Transactions, accuracy, audit trails |
| **Analytics** | Window functions, CTEs, reporting |

---

## Practice Platforms

After completing this curriculum, practice on these platforms:

| Platform | Difficulty | Best For |
|----------|------------|----------|
| [LeetCode SQL](https://leetcode.com/problemset/database/) | Easy-Hard | Algorithm-style problems |
| [HackerRank SQL](https://www.hackerrank.com/domains/sql) | Easy-Medium | Skill certificates |
| [DataLemur](https://datalemur.com/) | Medium-Hard | FAANG-specific |
| [StrataScratch](https://www.stratascratch.com/) | Medium-Hard | Real interview questions |
| [SQLZoo](https://sqlzoo.net/) | Easy-Medium | Interactive tutorials |

---

## How to Use This Repository

### 1. Set Up Your Environment
- Install MySQL or PostgreSQL
- Run the sample database schema
- Optionally use online SQL editors

### 2. Work Through Phases
- Read the concept explanations
- Study code examples
- Solve practice questions
- Review common mistakes

### 3. Practice Interview Questions
- Start with easy questions
- Time yourself (aim for 15-20 min per question)
- Practice explaining your thought process

### 4. Review and Reinforce
- Use the cheat sheet for quick reference
- Re-visit topics you struggle with
- Do mock interviews

---

## Success Metrics

Track your progress:

- [ ] Completed all 10 phases
- [ ] Solved 30 Easy questions
- [ ] Solved 50 Medium questions
- [ ] Solved 20 Hard questions
- [ ] Can write window functions confidently
- [ ] Understand query optimization
- [ ] Can design a database schema
- [ ] Completed 3+ mock SQL interviews

---

## Contributing

Found an error or want to add content? Contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## Additional Resources

### Books
- "SQL in 10 Minutes" by Ben Forta
- "Learning SQL" by Alan Beaulieu
- "High Performance MySQL" by Baron Schwartz

### Documentation
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

### YouTube Channels
- TechTFQ
- Alex the Analyst
- Data with Danny

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Good luck with your SQL interview preparation!**

*Start with [Phase 1](./database-foundations/phase-01-basics/README.md) or jump to the [Top 100 Questions](./interview-questions/top-100-questions.md).*

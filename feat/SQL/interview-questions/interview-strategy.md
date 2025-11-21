# SQL Interview Strategy Guide

A comprehensive guide to approaching SQL interview questions systematically, communicating effectively, and maximizing your chances of success.

---

## 1. How to Approach SQL Problems Systematically

### The 5-Step Framework

#### Step 1: UNDERSTAND - Clarify the Question

**What to do:**
- Read the problem statement twice
- Identify the expected output columns
- Understand what "success" looks like
- Note any specific requirements (ordering, formatting, limits)
- Examine the table schemas provided

**What to say to interviewer:**
- "Let me make sure I understand the problem correctly..."
- "So we need to return [columns] that satisfy [conditions], is that right?"
- "Before I start, can I ask a few clarifying questions?"
- "I see we have [X] tables. Let me understand the relationships..."

**Common pitfalls:**
- Jumping into code without fully understanding requirements
- Missing implicit requirements (like handling NULLs)
- Confusing similar-sounding requirements (e.g., "highest" vs "top 3")
- Not clarifying ambiguous terms like "recent" or "active"

---

#### Step 2: PLAN - Identify Tables and Relationships

**What to do:**
- List all tables needed
- Identify primary keys and foreign keys
- Determine join types required
- Sketch the query structure mentally or on paper
- Consider whether aggregation is needed

**What to say to interviewer:**
- "I'll need to join these tables: [list them]"
- "The relationship between A and B is one-to-many through [column]"
- "I'm thinking of approaching this by first [step], then [step]"
- "I'll need to use a GROUP BY because we're aggregating [what]"

**Common pitfalls:**
- Missing necessary tables
- Using wrong join types (INNER vs LEFT)
- Forgetting about many-to-many relationships
- Not considering whether subqueries or CTEs are needed

---

#### Step 3: WRITE - Build Query Incrementally

**What to do:**
- Start with the simplest working version
- Add complexity layer by layer
- Use CTEs for complex queries (improves readability)
- Name aliases meaningfully
- Format code cleanly with proper indentation

**What to say to interviewer:**
- "Let me start with the basic structure and build from there"
- "First, I'll get [basic data], then I'll add the [filter/aggregation]"
- "I'm using a CTE here to make the query more readable"
- "Let me add comments to explain this complex part"

**Common pitfalls:**
- Trying to write the perfect query on the first attempt
- Poor formatting making it hard to debug
- Using cryptic aliases (a, b, x) instead of meaningful names
- Not building incrementally (makes debugging harder)

---

#### Step 4: VERIFY - Check Edge Cases

**What to do:**
- Mentally trace through with sample data
- Check for NULL handling
- Verify GROUP BY includes all non-aggregated columns
- Confirm join conditions are complete
- Check for off-by-one errors in date ranges

**What to say to interviewer:**
- "Let me trace through this with a simple example..."
- "I should check what happens when [edge case]"
- "I need to handle the NULL case here by..."
- "Let me verify my GROUP BY includes all necessary columns"

**Common pitfalls:**
- Forgetting to handle NULLs in comparisons
- Not considering empty result sets
- Missing edge cases in date/time logic
- Incorrect comparison operators (< vs <=)

---

#### Step 5: OPTIMIZE - Consider Performance

**What to do:**
- Identify potential performance bottlenecks
- Consider indexing implications
- Evaluate if subqueries can be converted to joins
- Check for unnecessary columns in SELECT
- Consider query execution order

**What to say to interviewer:**
- "This query would benefit from an index on [column]"
- "For large datasets, I'd consider [optimization]"
- "I could improve performance by [specific change]"
- "The current complexity is O(n), but we could reduce it by..."

**Common pitfalls:**
- Premature optimization before correctness
- Using SELECT * in production queries
- Correlated subqueries when joins would work
- Not considering the data volume

---

## 2. Clarifying Questions to Ask

### Data Understanding Questions

1. **"What does each row represent in this table?"**
   - Example: "Does each row in the orders table represent a single order or a line item?"

2. **"What's the granularity of this data?"**
   - Example: "Is this daily, hourly, or event-level data?"

3. **"What are the primary and foreign keys?"**
   - Example: "Is user_id the primary key, or can users have multiple records?"

4. **"What's the relationship between these tables?"**
   - Example: "Is it one-to-one, one-to-many, or many-to-many between users and orders?"

5. **"What's the date range of the data?"**
   - Example: "Does this data span multiple years, or just the current year?"

6. **"Are there any derived or calculated columns?"**
   - Example: "Is total_amount pre-calculated, or should I sum line items?"

### Edge Case Questions

7. **"How should I handle NULL values?"**
   - Example: "If status is NULL, should I treat it as 'unknown' or exclude it?"

8. **"Can there be duplicate records?"**
   - Example: "Can a user place multiple orders on the same day?"

9. **"What if the result set is empty?"**
   - Example: "If no users match the criteria, should I return empty or a message?"

10. **"How should I handle ties?"**
    - Example: "If two products have the same sales, how do I determine ranking?"

11. **"Are there any invalid or test records to exclude?"**
    - Example: "Should I filter out internal test accounts?"

12. **"What about future-dated records?"**
    - Example: "Are there orders with future dates that should be excluded?"

13. **"How should I handle deleted or inactive records?"**
    - Example: "Should I include soft-deleted users in the count?"

### Performance Requirements Questions

14. **"How large is the dataset?"**
    - Example: "Are we talking thousands, millions, or billions of rows?"

15. **"Are there any time constraints on query execution?"**
    - Example: "Does this need to run in real-time or can it be a batch job?"

16. **"What indexes exist on these tables?"**
    - Example: "Is there an index on the date column I'm filtering on?"

17. **"Should I optimize for read or write performance?"**
    - Example: "Is this query running frequently or just for one-time analysis?"

### Output Format Questions

18. **"What columns should be included in the output?"**
    - Example: "Should I return just the user_id or include name and email too?"

19. **"What should the column names/aliases be?"**
    - Example: "Should it be 'total_sales' or 'revenue'?"

20. **"How should the results be sorted?"**
    - Example: "By date ascending or descending? Primary and secondary sort?"

21. **"Should results be limited?"**
    - Example: "Top 10, top 100, or all matching records?"

22. **"What precision for numerical results?"**
    - Example: "Should amounts be rounded to 2 decimal places?"

23. **"What date format is expected?"**
    - Example: "Should dates be 'YYYY-MM-DD' or 'MM/DD/YYYY'?"

24. **"Should aggregations include zeros or exclude them?"**
    - Example: "If a product had no sales, should it appear with 0 or be omitted?"

---

## 3. Time Management in Coding Interviews

### Typical SQL Interview Timeline (45 minutes)

#### Minutes 0-5: Problem Introduction (5 min)
**What to do:**
- Listen carefully to the problem statement
- Take notes on key requirements
- Review provided table schemas

**What NOT to do:**
- Start writing code immediately
- Interrupt the interviewer
- Skip reviewing the schemas

---

#### Minutes 5-10: Clarification Phase (5 min)
**What to do:**
- Ask 3-5 targeted clarifying questions
- Confirm your understanding
- Identify edge cases early

**What NOT to do:**
- Ask more than 7-8 questions (appears unprepared)
- Ask obvious questions answered in the problem
- Skip this phase entirely

---

#### Minutes 10-15: Planning Phase (5 min)
**What to do:**
- Verbalize your approach
- Identify tables and joins needed
- Outline query structure

**What NOT to do:**
- Stay silent while thinking
- Skip straight to coding
- Over-plan complex solutions

---

#### Minutes 15-30: Coding Phase (15 min)
**What to do:**
- Write query incrementally
- Think out loud
- Use meaningful aliases
- Format code cleanly

**What NOT to do:**
- Write silently for long periods
- Try to write perfect code first time
- Use poor formatting

---

#### Minutes 30-38: Testing & Verification (8 min)
**What to do:**
- Trace through with sample data
- Check edge cases
- Verify column names and ordering

**What NOT to do:**
- Skip verification
- Only test the happy path
- Forget to check NULLs

---

#### Minutes 38-45: Optimization & Discussion (7 min)
**What to do:**
- Discuss performance considerations
- Mention indexing strategies
- Talk about alternative approaches

**What NOT to do:**
- End without discussing optimization
- Make changes without explaining
- Ignore interviewer's hints

---

### When to Ask for Hints

**Good times to ask:**
- After spending 3+ minutes stuck on one part
- When you have two approaches and can't decide
- When you're unsure about syntax for a specific function
- After attempting something that clearly isn't working

**How to ask:**
- "I'm considering [approach], but I'm not sure about [specific part]. Could you point me in the right direction?"
- "I know I need a window function here, but I'm blanking on the syntax. Could you help?"
- "I've been stuck on this join for a few minutes. Am I on the right track?"

**What NOT to say:**
- "I don't know how to do this"
- "What's the answer?"
- "I give up on this part"

---

### When to Move On

**Move on when:**
- You've spent more than 5 minutes on a single issue
- The interviewer suggests moving forward
- You have a working solution (even if not optimal)
- You're stuck on syntax but have the right logic

**How to move on gracefully:**
- "I'll note that this needs optimization and come back if time permits"
- "Let me use placeholder syntax here and refine it later"
- "I'll move forward with this approach and we can discuss alternatives"

---

### Red Flags to Avoid

| Red Flag | Why It's Bad | What to Do Instead |
|----------|-------------|---------------------|
| Long silences (>30 sec) | Interviewer can't assess your thinking | Think out loud continuously |
| Defensive about mistakes | Shows poor collaboration | Acknowledge and correct gracefully |
| Giving up easily | Shows lack of persistence | Ask for hints, try alternatives |
| Ignoring hints | Shows poor listening | Incorporate suggestions immediately |
| Not testing | Shows lack of rigor | Always verify with examples |
| Over-engineering | Wastes time | Start simple, optimize later |
| Poor time awareness | May not finish | Set mental checkpoints |

---

## 4. Testing Your Solution Mentally

### Test Case Categories

#### Normal Cases (Happy Path)
**Purpose:** Verify basic functionality works

**Examples:**
- Standard input with typical values
- Middle-of-the-range dates
- Average-sized result sets
- Common data patterns

**Mental test:**
```
"If I have a user with 3 orders totaling $100,
does my query correctly return user_id and $100?"
```

---

#### Edge Cases

**Empty Tables**
- What if the main table has no rows?
- What if a joined table is empty?
- Does your LEFT JOIN handle this correctly?

**NULL Values**
- NULL in columns used for filtering
- NULL in columns used for calculations
- NULL in GROUP BY columns
- NULL comparisons (remember: NULL = NULL is false!)

**Duplicates**
- Multiple records with same key values
- Exact duplicate rows
- Duplicates after joining

**Single Row**
- Only one record in the table
- Only one group in GROUP BY
- Only one value to aggregate

---

#### Boundary Conditions

**Date Boundaries**
- First/last day of month
- Year boundaries (Dec 31 vs Jan 1)
- Leap year dates
- Time zone edge cases

**Numeric Boundaries**
- Zero values
- Negative numbers (if applicable)
- Maximum/minimum values
- Division edge cases (divide by zero)

**String Boundaries**
- Empty strings vs NULL
- Case sensitivity
- Leading/trailing spaces
- Special characters

---

#### Large Dataset Considerations

**Performance questions to consider:**
- How does this scale with 1M rows? 10M?
- Are there any N+1 query patterns?
- Could this cause a Cartesian product?
- What's the memory impact of this approach?

---

### Verification Checklist

#### SELECT Clause
- [ ] All required columns included
- [ ] Column aliases are correct and meaningful
- [ ] Aggregation functions are appropriate (SUM vs COUNT vs AVG)
- [ ] No unnecessary columns (avoid SELECT *)
- [ ] Calculated columns have correct formulas

#### FROM and JOINs
- [ ] Correct join type (INNER, LEFT, RIGHT, FULL)
- [ ] Join conditions are complete (all keys specified)
- [ ] No accidental Cartesian products
- [ ] Table aliases are clear

#### WHERE Clause
- [ ] Filtering logic is correct
- [ ] Date ranges are inclusive/exclusive as required
- [ ] NULL handling is explicit
- [ ] No redundant conditions
- [ ] Comparison operators are correct (< vs <=)

#### GROUP BY Clause
- [ ] All non-aggregated columns in SELECT are in GROUP BY
- [ ] No extra columns that would change granularity
- [ ] Grouping matches the business requirement

#### HAVING Clause
- [ ] Applied to aggregated values (not WHERE conditions)
- [ ] Comparison values are correct

#### ORDER BY Clause
- [ ] Correct columns specified
- [ ] ASC/DESC direction is right
- [ ] Multiple sort columns in correct priority

#### Overall
- [ ] Query returns expected number of columns
- [ ] Column order matches requirements
- [ ] Result set granularity is correct
- [ ] NULLs appear where expected

---

## 5. Communication During Interview

### Think Out Loud Examples

**When starting the problem:**
> "Okay, so I need to find the top 5 customers by total purchase amount in 2023. Let me think about what tables I need... I'll need the customers table for names and the orders table for amounts. These join on customer_id..."

**When making decisions:**
> "I'm choosing a LEFT JOIN here instead of INNER JOIN because the question asks for 'all customers', which suggests we want customers even if they have no orders. I'll handle the NULL case with COALESCE..."

**When hitting an obstacle:**
> "Hmm, this approach would give me duplicates because of the many-to-many relationship. Let me reconsider... I could use a subquery to aggregate first, then join. That would give me one row per customer..."

**When testing:**
> "Let me trace through this with an example. If customer A has orders of $100 and $50, my SUM should give $150. The GROUP BY is on customer_id, so yes, that should work correctly..."

---

### How to Explain Your Approach

**Structure your explanation:**

1. **State the goal:** "The goal is to find X that satisfies Y"
2. **Outline the strategy:** "I'll approach this by first doing A, then B, then C"
3. **Justify decisions:** "I'm using a CTE because it makes the query more readable"
4. **Acknowledge trade-offs:** "This approach is clearer but might be slower; we could optimize by..."

**Good explanation example:**
> "To solve this, I need to:
> 1. First, join orders with customers to get customer names
> 2. Then, aggregate by customer to get total purchase amounts
> 3. Filter for 2023 orders only
> 4. Finally, order by total amount and take the top 5
>
> I'm using a CTE for the aggregation step because it keeps the main query clean. The filtering on year happens before aggregation for efficiency."

---

### How to Handle Mistakes

**When you spot your own error:**
> "Actually, wait - I just realized I have a bug here. I'm filtering on order_date in the WHERE clause, but that will exclude customers with no orders. Let me move that condition to the JOIN instead... There, now customers without 2023 orders will still appear with NULL values."

**When the interviewer points out an error:**
> "Oh, you're right! Good catch. [Pause to understand] So the issue is that I'm grouping by the wrong column. It should be customer_id, not order_id. Let me fix that... Thank you for pointing that out."

**What NOT to do:**
- Don't get defensive: "Well, it depends on interpretation..."
- Don't blame the question: "That wasn't clear from the problem..."
- Don't minimize: "Oh, that's just a typo..."

**What TO do:**
- Acknowledge quickly
- Understand the root cause
- Fix it systematically
- Learn from it visibly

---

### How to Discuss Alternatives

**Proactively offer alternatives:**
> "I've implemented this using a subquery, but there's another approach using a window function that might be more efficient for large datasets. Would you like me to show that alternative?"

**When asked about different approaches:**
> "Yes, there are a few ways to solve this:
> 1. **Subquery approach** - what I wrote, good for readability
> 2. **JOIN approach** - might be faster, but harder to read
> 3. **Window function** - most elegant, but requires specific SQL support
>
> For this dataset size, I'd recommend the subquery approach for maintainability."

**Compare trade-offs clearly:**
> "The CTE approach is more readable and easier to debug, while the single-query approach might have slightly better performance. In a production environment, I'd probably start with the CTE for clarity and optimize only if we see performance issues."

---

## 6. Common Interview Question Patterns

### Pattern 1: Top N Queries

**Problem type:** Find the top/bottom N items based on some criteria

**Approach:**
1. Calculate the ranking metric
2. ORDER BY that metric (DESC for top, ASC for bottom)
3. LIMIT to N

**Example structure:**
```sql
SELECT column1, column2, aggregate_function
FROM table
GROUP BY column1, column2
ORDER BY aggregate_function DESC
LIMIT N;
```

**Variations:**
- Top N per group (requires window functions)
- Top N with ties
- Top N excluding certain records

**Key considerations:**
- Handle ties explicitly (RANK vs ROW_NUMBER)
- Clarify if N is fixed or variable
- Consider performance for large datasets

---

### Pattern 2: Running Totals / Cumulative Sums

**Problem type:** Calculate cumulative values over time or sequence

**Approach:**
1. Identify the ordering column (usually date)
2. Use SUM() with OVER() clause
3. Specify the window frame correctly

**Example structure:**
```sql
SELECT
    date,
    amount,
    SUM(amount) OVER (ORDER BY date ROWS UNBOUNDED PRECEDING) as running_total
FROM transactions;
```

**Variations:**
- Running total per category (add PARTITION BY)
- Moving averages (different window frame)
- Cumulative percentage

**Key considerations:**
- ROWS vs RANGE in window frame
- Handling of ties in ORDER BY
- Performance with large datasets

---

### Pattern 3: Finding Duplicates

**Problem type:** Identify duplicate records based on certain columns

**Approach:**
1. GROUP BY the columns that define uniqueness
2. HAVING COUNT(*) > 1
3. Optionally join back to get full records

**Example structure:**
```sql
SELECT column1, column2, COUNT(*) as count
FROM table
GROUP BY column1, column2
HAVING COUNT(*) > 1;
```

**Variations:**
- Find and delete duplicates (keep one)
- Find near-duplicates (fuzzy matching)
- Count duplicates per category

**Key considerations:**
- Define what makes a "duplicate" clearly
- Decide what to return (just keys or full records)
- Consider case sensitivity

---

### Pattern 4: Gap Analysis

**Problem type:** Find missing values in a sequence

**Approach:**
1. Generate the complete sequence (or use a numbers table)
2. LEFT JOIN with actual data
3. Find where the join produces NULLs

**Example structure:**
```sql
WITH expected AS (
    SELECT generate_series(1, 100) as num
)
SELECT e.num as missing_number
FROM expected e
LEFT JOIN actual_table a ON e.num = a.id
WHERE a.id IS NULL;
```

**Variations:**
- Missing dates in a range
- Gaps in sequential IDs
- Missing combinations

**Key considerations:**
- How to generate the expected sequence
- Performance for large ranges
- Edge cases at boundaries

---

### Pattern 5: Year-over-Year Comparison

**Problem type:** Compare metrics between time periods

**Approach:**
1. Create separate aggregations for each period
2. Join them together (or use conditional aggregation)
3. Calculate the difference/percentage change

**Example structure:**
```sql
SELECT
    category,
    SUM(CASE WHEN YEAR(date) = 2023 THEN amount ELSE 0 END) as year_2023,
    SUM(CASE WHEN YEAR(date) = 2022 THEN amount ELSE 0 END) as year_2022,
    (SUM(CASE WHEN YEAR(date) = 2023 THEN amount ELSE 0 END) -
     SUM(CASE WHEN YEAR(date) = 2022 THEN amount ELSE 0 END)) as difference
FROM sales
GROUP BY category;
```

**Variations:**
- Month-over-month
- Same period last year
- Rolling comparisons

**Key considerations:**
- Handle missing data periods
- Avoid division by zero in percentages
- Consider seasonality

---

### Pattern 6: Sessionization

**Problem type:** Group events into sessions based on time gaps

**Approach:**
1. Calculate time differences between consecutive events
2. Identify session boundaries (gap > threshold)
3. Assign session IDs using cumulative sum

**Example structure:**
```sql
WITH time_diffs AS (
    SELECT *,
        LAG(event_time) OVER (PARTITION BY user_id ORDER BY event_time) as prev_time
    FROM events
),
session_flags AS (
    SELECT *,
        CASE WHEN DATEDIFF(minute, prev_time, event_time) > 30
             OR prev_time IS NULL THEN 1 ELSE 0 END as new_session
    FROM time_diffs
)
SELECT *,
    SUM(new_session) OVER (PARTITION BY user_id ORDER BY event_time) as session_id
FROM session_flags;
```

**Key considerations:**
- Define session timeout threshold
- Handle first event (always starts new session)
- Partition by user/entity

---

### Pattern 7: Pivot / Unpivot

**Problem type:** Transform rows to columns or vice versa

**Approach (Pivot):**
1. Use CASE WHEN for each column value
2. Aggregate with appropriate function
3. GROUP BY the row identifier

**Example structure:**
```sql
SELECT
    product_id,
    SUM(CASE WHEN month = 'Jan' THEN sales ELSE 0 END) as jan_sales,
    SUM(CASE WHEN month = 'Feb' THEN sales ELSE 0 END) as feb_sales,
    SUM(CASE WHEN month = 'Mar' THEN sales ELSE 0 END) as mar_sales
FROM monthly_sales
GROUP BY product_id;
```

**Key considerations:**
- Dynamic pivoting requires dynamic SQL
- Handle NULL in pivoted columns
- UNPIVOT for reverse operation

---

### Pattern 8: Finding Consecutive Sequences

**Problem type:** Identify streaks or consecutive occurrences

**Approach:**
1. Create row numbers for overall and grouped sequences
2. Subtract to create group identifiers
3. Count consecutive records in each group

**Example structure:**
```sql
WITH numbered AS (
    SELECT *,
        ROW_NUMBER() OVER (ORDER BY date) as rn,
        ROW_NUMBER() OVER (PARTITION BY status ORDER BY date) as status_rn
    FROM daily_status
)
SELECT status, date, (rn - status_rn) as streak_group
FROM numbered;
```

**Key considerations:**
- The "difference of row numbers" trick
- Handle first/last in sequence
- Find longest streak with additional aggregation

---

### Pattern 9: Hierarchical / Recursive Queries

**Problem type:** Navigate tree or graph structures

**Approach:**
1. Define base case (root nodes)
2. Define recursive case (child lookup)
3. Join with UNION ALL

**Example structure:**
```sql
WITH RECURSIVE hierarchy AS (
    -- Base case: root nodes
    SELECT id, name, parent_id, 1 as level
    FROM employees
    WHERE parent_id IS NULL

    UNION ALL

    -- Recursive case: children
    SELECT e.id, e.name, e.parent_id, h.level + 1
    FROM employees e
    JOIN hierarchy h ON e.parent_id = h.id
)
SELECT * FROM hierarchy;
```

**Key considerations:**
- Maximum recursion depth
- Cycle detection
- Performance with deep hierarchies

---

### Pattern 10: Percentile / Median Calculations

**Problem type:** Calculate distribution statistics

**Approach:**
1. Use PERCENTILE_CONT or PERCENTILE_DISC functions
2. Or calculate manually using row numbers

**Example structure:**
```sql
-- Using built-in function
SELECT
    department,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) as median_salary
FROM employees
GROUP BY department;

-- Manual calculation
WITH ranked AS (
    SELECT salary,
        ROW_NUMBER() OVER (ORDER BY salary) as rn,
        COUNT(*) OVER () as total
    FROM employees
)
SELECT AVG(salary) as median
FROM ranked
WHERE rn IN (FLOOR((total + 1) / 2.0), CEIL((total + 1) / 2.0));
```

**Key considerations:**
- Odd vs even number of records
- PERCENTILE_CONT (interpolates) vs PERCENTILE_DISC (actual value)
- Performance on large datasets

---

## 7. Before the Interview Checklist

### Technical Preparation (1-2 weeks before)

#### SQL Fundamentals Review
- [ ] JOIN types (INNER, LEFT, RIGHT, FULL, CROSS)
- [ ] Aggregation functions (SUM, COUNT, AVG, MIN, MAX)
- [ ] GROUP BY and HAVING clauses
- [ ] Subqueries (correlated and non-correlated)
- [ ] Common Table Expressions (CTEs)

#### Advanced Topics
- [ ] Window functions (ROW_NUMBER, RANK, LAG, LEAD)
- [ ] PARTITION BY and ORDER BY in window functions
- [ ] Date/time functions for your target database
- [ ] String manipulation functions
- [ ] CASE WHEN expressions
- [ ] NULL handling (COALESCE, NULLIF, IS NULL)

#### Practice Problems
- [ ] Complete 20+ LeetCode/HackerRank SQL problems
- [ ] Focus on medium difficulty problems
- [ ] Practice with time limits (15-20 min per problem)
- [ ] Review solutions even for problems you solved

#### Database-Specific Syntax
- [ ] Know the target database (MySQL, PostgreSQL, SQL Server, etc.)
- [ ] Review database-specific functions
- [ ] Understand syntax variations (LIMIT vs TOP, date functions)
- [ ] Practice with that specific dialect

---

### Platform Setup (1 day before)

#### Technical Setup
- [ ] Test your internet connection
- [ ] Update your browser
- [ ] Test screen sharing capability
- [ ] Check microphone and camera
- [ ] Have backup device ready

#### Environment Preparation
- [ ] Have a SQL reference sheet ready (for syntax lookup)
- [ ] Open an online SQL editor for practice
- [ ] Close unnecessary browser tabs and applications
- [ ] Silence phone notifications
- [ ] Inform household members about interview time

#### Interview Platform
- [ ] Log into the interview platform early
- [ ] Familiarize yourself with the IDE/editor
- [ ] Know how to run queries
- [ ] Test copy/paste functionality
- [ ] Understand how to submit/share answers

#### Backup Plans
- [ ] Have interviewer's contact information
- [ ] Know how to dial in if video fails
- [ ] Have mobile hotspot ready if internet fails
- [ ] Prepare to share screen via alternative method

---

### Mental Preparation (Day of interview)

#### Mindset
- [ ] Remember: It's a conversation, not an interrogation
- [ ] Accept that you might not solve everything perfectly
- [ ] Focus on demonstrating your thought process
- [ ] Mistakes are opportunities to show resilience

#### Confidence Boosters
- [ ] Review 2-3 problems you solved well
- [ ] Remember your past SQL accomplishments
- [ ] Visualize yourself explaining solutions clearly
- [ ] Take deep breaths before starting

#### Physical Preparation
- [ ] Get good sleep the night before
- [ ] Eat a light meal before interview
- [ ] Have water nearby
- [ ] Dress professionally (even for video calls)
- [ ] Use the restroom before the interview

#### Last-Minute Review
- [ ] Glance at common SQL patterns
- [ ] Review the 5-step framework
- [ ] Remember key clarifying questions
- [ ] Recall communication best practices

---

### During the Interview Reminders

**Keep visible:**
- [ ] The 5-step framework
- [ ] Time management breakdown
- [ ] Verification checklist
- [ ] List of clarifying questions

**Mental reminders:**
- [ ] Think out loud always
- [ ] It's okay to ask questions
- [ ] Start simple, optimize later
- [ ] Verify before finalizing

---

## Quick Reference Card

### The 5-Step Framework
1. **UNDERSTAND** - Clarify requirements (2-3 min)
2. **PLAN** - Identify tables and approach (2-3 min)
3. **WRITE** - Build incrementally (10-15 min)
4. **VERIFY** - Test with examples (3-5 min)
5. **OPTIMIZE** - Discuss performance (2-3 min)

### Must-Ask Questions
1. How to handle NULLs?
2. Expected output columns and format?
3. Sort order (ASC/DESC)?
4. How to handle ties?
5. Table relationships and keys?

### Common Window Functions
- `ROW_NUMBER()` - Unique sequential numbers
- `RANK()` - Ranking with gaps
- `DENSE_RANK()` - Ranking without gaps
- `LAG()` / `LEAD()` - Previous/next row values
- `SUM() OVER()` - Running totals

### NULL Handling
- `NULL = NULL` is FALSE (use IS NULL)
- `COALESCE(col, default)` - Replace NULL
- `NULLIF(a, b)` - Return NULL if a = b
- Aggregations ignore NULL (except COUNT(*))

### Time Management
- 0-5 min: Understand problem
- 5-10 min: Clarify and plan
- 10-30 min: Write solution
- 30-40 min: Verify and test
- 40-45 min: Optimize and discuss

---

**Remember:** The goal is not just to write correct SQL, but to demonstrate your problem-solving process, communication skills, and technical depth. Even if you don't reach the optimal solution, a clear thought process and good communication can leave a strong impression.

Good luck with your interview!

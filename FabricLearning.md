# Microsoft Fabric Hands-On Lab
## Learning by Doing - Complete Step-by-Step Guide (Lakehouse Focus)

**Duration:** 2-3 hours  
**Workspace:** FabricLearning (already created ✅)  
**Focus:** Lakehouse with PySpark transformations and Medallion Architecture

---

# 🎯 What You'll Build

By the end of this lab, you will have:
- ✅ A Data Lakehouse with full Medallion Architecture (Bronze → Silver → Gold)
- ✅ Multiple PySpark notebooks for data transformations
- ✅ 7 Delta tables across Bronze, Silver, and Gold layers
- ✅ Power BI reports from Lakehouse Gold layer
- ✅ Real-world data pipeline patterns using only Lakehouse

---

# 📋 Lab Structure

This hands-on lab is divided into 4 main sections:

**LAB 1:** Create Data Lakehouse (15 minutes)
- Create your lakehouse in Fabric
- Upload sample CSV data

**LAB 2:** PySpark - Medallion Architecture (90 minutes) ⭐ MAIN FOCUS
- **Bronze Layer:** Raw data ingestion with metadata tracking
- **Silver Layer:** Data cleaning, transformation, and quality checks
- **Gold Layer:** Business aggregates (4 analytical tables)

**LAB 3:** Power BI Report from Lakehouse (30 minutes)
- Query Gold tables using SQL Analytics Endpoint
- Create semantic model
- Build interactive dashboard with 6 visuals

**LAB 4:** Advanced PySpark (Optional)
- Window functions, rankings, cumulative sums
- Pivot tables
- Complex joins and transformations

---

# 📊 Sample Data (10 Rows)

Save this as **sales_data.csv** on your computer:

```csv
order_id,order_date,customer_id,customer_name,product_category,product_name,quantity,unit_price,total_amount,region,city
1001,2024-01-15,C101,John Smith,Electronics,Laptop,1,1299.99,1299.99,North,Seattle
1002,2024-01-16,C102,Sarah Johnson,Clothing,T-Shirt,3,29.99,89.97,South,Miami
1003,2024-01-16,C103,Michael Chen,Electronics,Wireless Mouse,2,49.99,99.98,West,Los Angeles
1004,2024-01-17,C104,Emily Davis,Home & Garden,Coffee Maker,1,89.99,89.99,East,New York
1005,2024-01-18,C101,John Smith,Electronics,USB-C Cable,5,15.99,79.95,North,Seattle
1006,2024-01-19,C105,David Martinez,Clothing,Running Shoes,1,129.99,129.99,West,San Francisco
1007,2024-01-20,C102,Sarah Johnson,Home & Garden,Kitchen Towels,4,12.50,50.00,South,Miami
1008,2024-01-21,C106,Lisa Anderson,Electronics,Bluetooth Headphones,1,199.99,199.99,East,Boston
1009,2024-01-22,C103,Michael Chen,Clothing,Jeans,2,79.99,159.98,West,Los Angeles
1010,2024-01-23,C107,Robert Wilson,Home & Garden,Bed Sheets,1,69.99,69.99,North,Portland
```

**✋ ACTION:** Copy the CSV content above and save it to your desktop as `sales_data.csv`

---

# 📝 LAB 1: Create Data Lakehouse (15 minutes)

## Step 1.1: Create a Lakehouse

**ACTIONS:**
1. Go to https://app.fabric.microsoft.com
2. Sign in
3. Click on **workspaces** icon on the left sidebar
4. Select **FabricLearning** workspace
5. Click the **+ New** button (top left)
6. Select **Lakehouse** from the dropdown
7. Name it: `SalesLakehouse`
8. Click **Create**

**✅ What you'll see:** Lakehouse opens with two sections: "Files" and "Tables" (both empty)

---

## Step 1.2: Upload CSV to Lakehouse

**ACTIONS:**
1. In the **Files** section, click **Upload** → **Upload files**
2. Select your `sales_data.csv` file
3. Click **Upload**
4. Wait for upload to complete (5 seconds)

**✅ What you'll see:** Your `sales_data.csv` appears in the Files section

**📝 NOTE:** The file is in "Files" but NOT yet in "Tables" - we'll convert it using PySpark!

---

# 📝 LAB 2: PySpark - Medallion Architecture (90 minutes)

This is the main part! You'll create Bronze → Silver → Gold layers using PySpark.

---

## Step 2.1: Create Bronze Layer Notebook

**ACTIONS:**
1. In your lakehouse, click **Open notebook** dropdown (top menu)
2. Select **New notebook**
3. Notebook opens with empty cell

**✅ What you'll see:** A blank notebook with Python as default language

**Rename Notebook:**
1. Click on the notebook name at the top (probably "Notebook 1")
2. Rename to: `01_Bronze_Layer`
3. Press Enter

---

## Step 2.2: Bronze Layer - Raw Data Ingestion

**ACTIONS:**
Copy and paste this code into **Cell 1**:

```python
# ========================================
# BRONZE LAYER: Raw Data Ingestion Example
# ========================================
# 💡 This notebook reads a raw CSV file into a DataFrame, adds metadata columns,
#    and displays the data and schema.
#
# The goal: Move data from the "Files" area into a Delta Lake table (Bronze).
#           No filtering or cleaning yet - just load everything "as is".
#
#    +-----------------------+
#    |  sales_data.csv file  |
#    +-----------------------+
#               |
#           [Read CSV]
#               |
#    +-----------------------+
#    | df_raw (Raw DataFrame)|
#    +-----------------------+
#               |
#     [Add Metadata Columns]
#               |
#    +-----------------------+
#    |df_bronze (Bronze Data)|
#    +-----------------------+
#
# Columns added: ingestion_timestamp, source_file, layer
# ========================================

# ==============================================
# PYTHON BASICS: Importing libraries
# ==============================================
# What is "import"?
#   - "import" brings in pre-written code (functions) that we can use
#   - Like borrowing tools from a toolbox instead of building them yourself
#
# What is "from X import Y"?
#   - We're saying "from the X library, give me only Y and Z"
#   - This makes the code cleaner - we can write current_timestamp() 
#     instead of pyspark.sql.functions.current_timestamp()
# ==============================================
from pyspark.sql.functions import current_timestamp, lit
# current_timestamp(): a function that gets current date & time (like NOW() in Excel)
# lit(): stands for "literal" - creates a column with a fixed value for every row

# ==============================================
# PYTHON BASICS: print() function
# ==============================================
# print(): displays text on screen (like console.log in JavaScript or echo in PHP)
# The "f" before the quotes means "f-string" - lets you insert variables into text
# "=" * 50 means: repeat the "=" character 50 times (creates a line separator)
# ==============================================
print("🔹 BRONZE LAYER - Raw Data Ingestion")
print("=" * 50)

# ==============================================
# 1️⃣ Read the CSV file into a DataFrame (df_raw)
# ==============================================
# PYSPARK BASICS: What is a DataFrame?
#   - A DataFrame is like an Excel spreadsheet in memory
#   - It has rows and columns with data
#   - You can perform operations on it (filter, sort, calculate, etc.)
#
# PYTHON BASICS: What is a variable?
#   - "df_raw" is a variable name (like a box with a label)
#   - The "=" sign means "store the result in this variable"
#   - We can now use "df_raw" to refer to our data
#
# PYSPARK BASICS: What is "spark"?
#   - "spark" is a pre-loaded object in Fabric notebooks
#   - It's your connection to the Spark engine (the data processing system)
#   - Think of it as your remote control for data operations
#
# PYTHON BASICS: What is the backslash "\"?
#   - The backslash "\" at the end of each line means "this line continues on next line"
#   - It makes long code easier to read
#   - You could write it all in one line, but it would be very long!
#
# PYTHON BASICS: What is dot notation (.)?
#   - The dot "." means "use a function/method that belongs to this object"
#   - spark.read means "use the read function that belongs to spark"
#   - Like saying "car.drive()" - the drive action belongs to the car
#
# CODE BREAKDOWN:
# - spark.read: Start reading data
# - .format("csv"): Tell Spark the file type is CSV (comma-separated values)
# - .option("header", "true"): First row contains column names (not data)
# - .option("inferSchema", "true"): Let Spark guess data types (text, numbers, dates)
#     * Without this, everything would be treated as text
# - .load("Files/sales_data.csv"): The actual file path to read
#
# After this step, df_raw contains all your CSV data as a DataFrame
# ==============================================
# A DataFrame holds a chunk or a complete set of data in memory, similar to an Excel table (not just a pointer).
df_raw = spark.read.format("csv") \
    .option("header", "true") \
    .option("inferSchema", "true") \
    .load("Files/sales_data.csv")

# ==============================================
# 2️⃣ Add metadata columns for lineage (where, when, what)
# ==============================================
# PYSPARK BASICS: What is "withColumn"?
#   - .withColumn() adds a new column to your DataFrame
#   - Syntax: .withColumn("new_column_name", what_value_to_put)
#   - It doesn't change df_raw - it creates a NEW DataFrame (df_bronze)
#
# PYSPARK BASICS: Transformations create new DataFrames
#   - In PySpark, operations don't change the original data
#   - They create a NEW DataFrame with the changes
#   - This is called "immutability" - original stays unchanged
#
# Why add these columns?
#   - Data lineage: tracking where data came from and when
#   - Debugging: helps you trace back if something goes wrong
#   - Auditing: who loaded what and when
#
# CODE BREAKDOWN:
# - Start with df_raw (the original data)
# - .withColumn("ingestion_timestamp", current_timestamp()): 
#       Add a column named "ingestion_timestamp" with the current date/time
# - .withColumn("source_file", lit("sales_data.csv")):
#       Add a column named "source_file" with the value "sales_data.csv" in every row
#       lit() creates a "literal" - same value for all rows
# - .withColumn("layer", lit("bronze")):
#       Add a column named "layer" with value "bronze" to mark this as bronze layer
#
# The result is stored in a new variable: df_bronze
# ==============================================
df_bronze = df_raw \
    .withColumn("ingestion_timestamp", current_timestamp()) \
    .withColumn("source_file", lit("sales_data.csv")) \
    .withColumn("layer", lit("bronze"))

# ==============================================
# 3️⃣ Show how many records were loaded
# ==============================================
# PYSPARK BASICS: What is count()?
#   - .count() is an ACTION that counts the number of rows
#   - It actually executes the data reading (up till now, nothing ran yet!)
#   - Returns a number (integer)
#
# PYTHON BASICS: f-strings (formatted strings)
#   - The "f" before quotes means you can embed variables
#   - {df_bronze.count()} - the curly braces {} run the code and insert the result
#   - Example: f"Hello {name}" where name="John" → prints "Hello John"
#   - "\n" means "new line" (press Enter)
# ==============================================
print(f"\n✅ Loaded {df_bronze.count()} records from CSV")

# ==============================================
# 4️⃣ Display a sample of the Bronze data as a table
# ==============================================
# FABRIC SPECIFIC: What is display()?
#   - display() is a special Fabric notebook function
#   - It shows your DataFrame as a nice formatted table with columns
#   - Like viewing an Excel spreadsheet in the notebook
#   - You can't use display() outside of Fabric notebooks
# ==============================================
print("\nBronze Data Sample:")
display(df_bronze)

# ==============================================
# 5️⃣ Show the schema (column names & types)
# ==============================================
# PYSPARK BASICS: What is printSchema()?
#   - Shows the structure of your DataFrame
#   - Lists all columns with their data types (string, integer, date, etc.)
#   - Like viewing "Field Properties" in a database
#   - Helps you understand what kind of data each column holds
# ==============================================
print("\nBronze Table Schema:")
df_bronze.printSchema()
```


**Run the cell:**
1. Click the **▶ Run cell** button (or press Shift+Enter)
2. Wait for execution (10-15 seconds)

**✅ What you'll see:** 
- "Loaded 10 records from CSV"
- A table showing your 10 rows with new columns: ingestion_timestamp, source_file, layer
- Schema printout

---

**ACTIONS:**
Add a **new cell** below (click **+ Code** button), and paste:

```python
# ==============================================
# 6️⃣ Write Bronze data to Delta table
# ==============================================
# PYSPARK BASICS: What is write()?
#   - .write is how you SAVE a DataFrame to storage
#   - Up until now, df_bronze only exists in memory (RAM)
#   - Writing saves it permanently so you can use it later
#
# PYSPARK BASICS: What is mode("overwrite")?
#   - mode() tells Spark what to do if table already exists
#   - "overwrite": delete old data and replace with new data
#   - Other options: "append" (add to existing), "ignore" (do nothing if exists)
#
# DELTA LAKE BASICS: What is format("delta")?
#   - Delta is a special storage format (better than just CSV or Parquet)
#   - Benefits: ACID transactions, time travel, schema enforcement, faster reads
#   - It's the recommended format for Fabric lakehouses
#
# PYSPARK BASICS: What is saveAsTable()?
#   - Saves the DataFrame as a table in the lakehouse
#   - "bronze_sales" becomes the table name you can query later
#   - The table appears in the "Tables" section of your lakehouse
# ==============================================
print("💾 Writing Bronze layer to Delta table...")

df_bronze.write.mode("overwrite") \
    .format("delta") \
    .saveAsTable("bronze_sales")

print("✅ Bronze layer created: bronze_sales")
print(f"   Location: Tables/bronze_sales")
print(f"   Record count: {spark.table('bronze_sales').count()}")

# ==============================================
# 7️⃣ Verify the table exists using SQL
# ==============================================
# PYSPARK + SQL: What is spark.sql()?
#   - You can run SQL queries using spark.sql()
#   - SQL is a database query language (SELECT, FROM, WHERE, etc.)
#   - Even though we're using Python, we can write SQL inside it!
#
# SQL BASICS: What is this query doing?
#   - SELECT * FROM bronze_sales: Get all columns (*) from bronze_sales table
#   - LIMIT 5: Only show first 5 rows (useful for large tables)
#
# PYTHON BASICS: What are quotes inside quotes?
#   - The outer quotes (") define the string
#   - We use single quotes (') for the SQL query inside
#   - This is called "nested quotes" or "quote escaping"
# ==============================================
print("\n📊 Verifying bronze_sales table:")
display(spark.sql("SELECT * FROM bronze_sales LIMIT 5"))
```

**Run this cell:**

**✅ What you'll see:** 
- Success message
- In the left panel under "Tables", `bronze_sales` appears!
- A preview of the table

**🎉 BRONZE LAYER COMPLETE!**

---

## Step 2.3: Create Silver Layer Notebook

**ACTIONS:**
1. Click **Home** tab at the top
2. Click **New notebook** button
3. Rename this notebook to: `02_Silver_Layer`

---

## Step 2.4: Silver Layer - Data Cleaning & Transformation

**ACTIONS:**
Copy and paste this code into **Cell 1**:

```python
# ========================================
# SILVER LAYER: Cleaned and validated data
# ========================================
# Purpose: Data quality, cleaning, standardization, add business logic
# In the Medallion Architecture:
#   - Bronze = Raw data (exactly as received)
#   - Silver = Cleaned, validated, standardized data (THIS LAYER!)
#   - Gold = Business aggregates for reporting

# ==============================================
# PYTHON BASICS: Import with asterisk (*)
# ==============================================
# What is "from X import *"?
#   - The asterisk (*) means "import EVERYTHING from this library"
#   - We get access to ALL PySpark functions (col, trim, upper, sum, avg, etc.)
#   - This is convenient but makes code less clear about which functions you're using
#   - For learning: it's okay; for production: better to list specific functions
# ==============================================
from pyspark.sql.functions import *

print("🔹 SILVER LAYER - Data Cleaning & Transformation")
print("=" * 50)

# ==============================================
# 1️⃣ Read data from Bronze layer table
# ==============================================
# PYSPARK BASICS: What is spark.table()?
#   - spark.table("table_name") reads an existing table into a DataFrame
#   - We created "bronze_sales" in the previous notebook
#   - Now we're loading it into memory to transform it
#   - Like opening an Excel file you saved earlier
# ==============================================
df_bronze = spark.table("bronze_sales")

print(f"✅ Read {df_bronze.count()} records from Bronze layer")

# ==============================================
# 2️⃣ Show a sample of the raw data (before cleaning)
# ==============================================
# PYSPARK BASICS: What is .select()?
#   - .select() chooses which columns to display
#   - Like hiding columns in Excel - you only see the ones you want
#   - Syntax: .select("col1", "col2", "col3")
#
# PYSPARK BASICS: Method chaining
#   - We chain multiple operations: .select().limit()
#   - Each method returns a DataFrame that the next method works on
#   - Like a pipeline: data flows from left to right
# ==============================================
print("\nBronze Data (before transformation):")
display(df_bronze.select("order_id", "customer_name", "region", "total_amount").limit(3))
```

**Run the cell**

---

**Add new cell:**

```python
# ========================================
# SILVER TRANSFORMATIONS - Data Cleaning
# ========================================

# ==============================================
# 1️⃣ Clean string columns (standardization)
# ==============================================
# BUSINESS REASON: Why clean strings?
#   - "Seattle", " seattle ", "SEATTLE" should all be the same
#   - Trim removes extra spaces at beginning/end
#   - Upper makes everything uppercase for consistency
#   - This prevents duplicate counts in reports
#
# PYSPARK BASICS: What is col()?
#   - col("column_name") references a column in the DataFrame
#   - Like saying "give me the customer_name column"
#   - We need col() when using PySpark functions on columns
#
# PYSPARK BASICS: Nested functions
#   - trim(upper(col("customer_name"))) - functions inside functions
#   - Read from inside-out: 
#     1. Get customer_name column
#     2. Make it uppercase
#     3. Remove extra spaces
#   - Like Excel: =TRIM(UPPER(A2))
#
# PYTHON BASICS: Why new column names?
#   - We keep original columns (customer_name) unchanged
#   - Create new cleaned columns (customer_name_clean)
#   - This way we can compare before/after if needed
# ==============================================
df_silver = df_bronze \
    .withColumn("customer_name_clean", trim(upper(col("customer_name")))) \
    .withColumn("region_clean", trim(upper(col("region")))) \
    .withColumn("product_category_clean", trim(upper(col("product_category"))))

# ==============================================
# 2️⃣ Convert order_date string to actual date type
# ==============================================
# PYSPARK BASICS: Why convert date types?
#   - CSV files store dates as text: "2024-01-15" is just a string
#   - We need real DATE type to do date math (calculate days between, extract year, etc.)
#   - to_date() converts text to date
#
# PYSPARK BASICS: What is "yyyy-MM-dd"?
#   - This is the date format pattern
#   - yyyy = 4-digit year (2024)
#   - MM = 2-digit month (01)
#   - dd = 2-digit day (15)
#   - Must match how your date looks in the CSV!
# ==============================================
df_silver = df_silver \
    .withColumn("order_date", to_date(col("order_date"), "yyyy-MM-dd"))

# ==============================================
# 3️⃣ Extract date components for analytics
# ==============================================
# BUSINESS REASON: Why extract date parts?
#   - Reports often need to group by year, month, quarter
#   - "What were Q1 sales?" needs quarter extracted
#   - "What day of week sells most?" needs dayofweek
#   - Extract once here, use many times in Gold layer
#
# PYSPARK DATE FUNCTIONS:
#   - year(): extracts year (2024)
#   - month(): extracts month number (1 for January)
#   - quarter(): extracts quarter (1, 2, 3, or 4)
#   - dayofweek(): extracts day number (1=Sunday, 2=Monday, etc.)
#   - date_format(): formats date as text in any pattern you want
#     * "MMMM" = full month name (January, February, etc.)
# ==============================================
df_silver = df_silver \
    .withColumn("year", year(col("order_date"))) \
    .withColumn("month", month(col("order_date"))) \
    .withColumn("quarter", quarter(col("order_date"))) \
    .withColumn("day_of_week", dayofweek(col("order_date"))) \
    .withColumn("month_name", date_format(col("order_date"), "MMMM"))

# ==============================================
# 4️⃣ Add business logic columns
# ==============================================
# PYSPARK BASICS: What is when().otherwise()?
#   - This is like IF-THEN-ELSE logic (like Excel IF function)
#   - when(condition, value_if_true).otherwise(value_if_false)
#   - Can chain multiple when() for multiple conditions
#
# PYTHON BASICS: What is True and False?
#   - These are Boolean values (true/false, yes/no, 1/0)
#   - Used for flags: is_high_value is either True or False
#   - Capital T and F are required in Python!
#
# BUSINESS LOGIC EXAMPLE 1: High value orders
#   - If total_amount > 100, mark as True (high value)
#   - Otherwise, mark as False
#
# BUSINESS LOGIC EXAMPLE 2: Price categories
#   - Multiple when() chained together
#   - Read top to bottom: first match wins
#   - If >= 150: "Premium"
#   - Else if >= 80: "Medium"  
#   - Else: "Standard"
# ==============================================
df_silver = df_silver \
    .withColumn("is_high_value", when(col("total_amount") > 100, True).otherwise(False)) \
    .withColumn("price_category", 
        when(col("total_amount") >= 150, "Premium")
        .when(col("total_amount") >= 80, "Medium")
        .otherwise("Standard")
    )

# ==============================================
# 5️⃣ Calculate derived metrics (calculated fields)
# ==============================================
# BUSINESS REASON: Calculated columns
#   - Create new columns based on existing columns
#   - Like Excel formula columns
#   - Calculate once here, reuse in Gold layer
#
# PYSPARK BASICS: Column math
#   - col("total_amount") - col("discount_amount"): subtract columns
#   - Works just like Excel: =A2-B2
#   - Result is a new column with the calculation
#
# PYSPARK BASICS: What is round()?
#   - round(value, decimal_places) rounds numbers
#   - round(col("unit_price"), 2) → round to 2 decimal places
#   - 19.999 becomes 20.00
# ==============================================
df_silver = df_silver \
    .withColumn("discount_amount", lit(0.0)) \
    .withColumn("net_amount", col("total_amount") - col("discount_amount")) \
    .withColumn("unit_price_rounded", round(col("unit_price"), 2))

print("\n✅ Transformations applied!")
print("\nSilver Data (after transformation):")
display(df_silver.select(
    "order_id", "customer_name_clean", "region_clean", 
    "year", "quarter", "is_high_value", "price_category"
).limit(5))
```

**Run the cell**

**✅ What you'll see:** Cleaned data with new calculated columns

---

**Add new cell:**

```python
# ========================================
# DATA QUALITY CHECKS
# ========================================
# BUSINESS REASON: Why check data quality?
#   - Bad data = bad reports = bad business decisions
#   - Catch problems early (null values, negative amounts, etc.)
#   - Document what checks were performed
#   - Prevent garbage data from reaching Gold layer

print("\n🔍 DATA QUALITY CHECKS")
print("=" * 50)

# ==============================================
# CHECK 1: Find null (missing) values
# ==============================================
# PYSPARK BASICS: What is isNull()?
#   - isNull() checks if a value is missing/empty
#   - Returns True if null, False if has value
#   - Like Excel ISBLANK()
#
# PYTHON BASICS: What is a list comprehension?
#   - [expression for item in list] - creates a list using a loop
#   - Example: [x*2 for x in [1,2,3]] → [2,4,6]
#   - Here: for each column name, count nulls
#
# PYSPARK BASICS: What is .alias()?
#   - .alias("name") renames a column
#   - The calculated column gets the original column name
#   - Makes output easier to read
#
# PYSPARK BASICS: What is .show()?
#   - .show() displays DataFrame in text format
#   - Similar to display() but simpler formatting
#   - Often used for quick checks
# ==============================================
null_counts = df_silver.select([
    count(when(col(c).isNull(), c)).alias(c) 
    for c in ["order_id", "customer_name_clean", "total_amount"]
])
print("\nNull value counts:")
null_counts.show()

# ==============================================
# CHECK 2: Find negative amounts (data error)
# ==============================================
# PYSPARK BASICS: What is filter()?
#   - .filter() keeps only rows that match a condition
#   - Like Excel AutoFilter or SQL WHERE clause
#   - col("total_amount") < 0 means "where total_amount is negative"
#   - Then .count() counts how many rows matched
# ==============================================
negative_amounts = df_silver.filter(col("total_amount") < 0).count()
print(f"Orders with negative amounts: {negative_amounts}")

# ==============================================
# CHECK 3: Find zero or negative quantities
# ==============================================
zero_qty = df_silver.filter(col("quantity") <= 0).count()
print(f"Orders with zero/negative quantity: {zero_qty}")

# ==============================================
# FILTER OUT INVALID RECORDS
# ==============================================
# PYSPARK BASICS: Multiple conditions with & and |
#   - & means "AND" (both conditions must be True)
#   - | means "OR" (at least one condition must be True)
#   - Must wrap each condition in parentheses: (condition1) & (condition2)
#
# PYSPARK BASICS: What is isNotNull()?
#   - Opposite of isNull()
#   - Returns True if value exists (not missing)
#
# BUSINESS LOGIC: What are we filtering?
#   - Keep only rows where total_amount > 0 (positive sales)
#   - AND quantity > 0 (actually ordered something)
#   - AND customer_id is not null (must have a customer)
#   - This removes any bad/corrupt records
# ==============================================
df_silver_clean = df_silver.filter(
    (col("total_amount") > 0) & 
    (col("quantity") > 0) &
    (col("customer_id").isNotNull())
)

print(f"\n✅ Records after quality checks: {df_silver_clean.count()}")
print("   (All records passed validation ✓)")
```

**Run the cell**

---

**Add new cell:**

```python
# ==============================================
# Select final Silver columns (column selection)
# ==============================================
# PYTHON BASICS: What is a list?
#   - A list is a collection of items in square brackets []
#   - Items separated by commas
#   - Like an array in other languages
#   - Can contain strings, numbers, or anything
#
# BUSINESS REASON: Why select specific columns?
#   - Remove temporary/helper columns we don't need
#   - Keep only cleaned versions (not original dirty versions)
#   - Organize columns in logical order
#   - Reduce storage size
# ==============================================
silver_columns = [
    "order_id", "order_date", "customer_id", "customer_name_clean",
    "product_category_clean", "product_name", "quantity", 
    "unit_price_rounded", "total_amount", "net_amount",
    "region_clean", "city", "year", "month", "quarter", 
    "day_of_week", "month_name", "is_high_value", "price_category",
    "ingestion_timestamp", "source_file"
]

# ==============================================
# PYTHON BASICS: What is the asterisk (*)?
# ==============================================
# The asterisk (*) "unpacks" a list
#   - *silver_columns unpacks the list into individual items
#   - Instead of: .select(silver_columns[0], silver_columns[1], ...)
#   - We write: .select(*silver_columns)
#   - Much cleaner and easier to maintain!
# ==============================================
df_silver_final = df_silver_clean.select(*silver_columns)

# ==============================================
# Write to Silver Delta table WITH PARTITIONING
# ==============================================
# DELTA LAKE BASICS: What is partitioning?
#   - Partitioning organizes data into folders by column values
#   - Example: year=2024/quarter=1/data.parquet
#   - Benefits:
#     * Queries with year/quarter filters are much faster
#     * Spark skips reading irrelevant partitions
#     * Like organizing files by date in Windows Explorer
#
# PYSPARK BASICS: What is partitionBy()?
#   - .partitionBy("col1", "col2") creates partitions
#   - Data physically stored in separate folders
#   - Choose columns commonly used in WHERE clauses
#   - Don't over-partition: too many small files is slow
#
# BEST PRACTICE: When to partition?
#   - Large tables (millions of rows)
#   - Columns with limited distinct values (year, month, region)
#   - Columns frequently used in filters
# ==============================================
print("\n💾 Writing Silver layer to Delta table (with partitioning)...")

df_silver_final.write.mode("overwrite") \
    .format("delta") \
    .partitionBy("year", "quarter") \
    .saveAsTable("silver_sales")

print("✅ Silver layer created: silver_sales")
print(f"   Partitioned by: year, quarter")
print(f"   Record count: {spark.table('silver_sales').count()}")

# ==============================================
# Verify the final Silver table
# ==============================================
print("\n📊 Final Silver Data:")
display(spark.table("silver_sales").limit(5))
```

**Run the cell**

**✅ What you'll see:** 
- `silver_sales` table appears in left panel under Tables
- Data is now clean and enriched!

**🎉 SILVER LAYER COMPLETE!**

---

## Step 2.5: Create Gold Layer Notebook

**ACTIONS:**
1. Click **Home** tab
2. Click **New notebook**
3. Rename to: `03_Gold_Layer`

---

## Step 2.6: Gold Layer - Business Aggregates

**ACTIONS:**
Copy and paste this into **Cell 1**:

```python
# ========================================
# GOLD LAYER: Business-level aggregates for analytics
# ========================================
# Purpose: Create aggregated tables optimized for reporting
#
# MEDALLION ARCHITECTURE - GOLD LAYER:
#   - Bronze: Raw data (all records, as-is)
#   - Silver: Cleaned data (all records, cleaned)
#   - Gold: Aggregated data (THIS LAYER! - summarized for business use)
#
# GOLD LAYER CHARACTERISTICS:
#   - Pre-calculated metrics (sum, average, count)
#   - Denormalized (wide tables, optimized for reading)
#   - Business-friendly column names
#   - Fast for reporting and dashboards
#   - Multiple Gold tables for different use cases

from pyspark.sql.functions import *

print("🔹 GOLD LAYER - Business Aggregates")
print("=" * 50)

# ==============================================
# Read data from Silver layer
# ==============================================
# We're starting from Silver (not Bronze) because:
#   - Silver is already cleaned and validated
#   - Has calculated columns we need (year, quarter, etc.)
#   - Ready for aggregation
# ==============================================
df_silver = spark.table("silver_sales")

print(f"✅ Read {df_silver.count()} records from Silver layer")
print(f"   Columns available: {len(df_silver.columns)}")
```

**Run the cell**

---

**Add new cell - Gold Table 1: Daily Sales Summary:**

```python
# ========================================
# GOLD TABLE 1: Daily Sales Summary
# ========================================
# BUSINESS PURPOSE: Answer these questions:
#   - How much revenue did we make each day?
#   - How many orders per day?
#   - What's the average order value?
#   - How many unique customers per day?

print("\n📊 Creating Gold Table 1: Daily Sales Summary")
print("-" * 50)

# ==============================================
# PYSPARK BASICS: What is groupBy()?
# ==============================================
# groupBy() groups rows with same values together
#   - Like Excel Pivot Table's "Row Labels"
#   - Like SQL "GROUP BY"
#   - Example: groupBy("order_date") puts all Jan 15 orders together
#
# Why group by multiple columns?
#   - We group by order_date, year, quarter, month_name
#   - This keeps those columns in the result
#   - Useful for filtering and sorting in reports
#
# ==============================================
# PYSPARK BASICS: What is agg()?
# ==============================================
# agg() performs aggregation functions (calculations across grouped rows)
#   - Think: "for each group, calculate..."
#   - Must come after groupBy()
#
# AGGREGATION FUNCTIONS:
#   - count("col"): count how many rows (non-null values)
#   - sum("col"): add up all values
#   - avg("col"): calculate average (mean)
#   - countDistinct("col"): count unique values
#
# PYSPARK BASICS: What is .alias()?
#   - Renames the calculated column
#   - Without alias: column would be named "count(order_id)"
#   - With alias: column is named "total_orders" (much better!)
#
# ==============================================
gold_daily_sales = df_silver.groupBy("order_date", "year", "quarter", "month_name") \
    .agg(
        count("order_id").alias("total_orders"),
        sum("total_amount").alias("total_revenue"),
        avg("total_amount").alias("avg_order_value"),
        sum("quantity").alias("total_items_sold"),
        countDistinct("customer_id").alias("unique_customers")
    ) \
    .withColumn("avg_order_value", round(col("avg_order_value"), 2)) \
    .withColumn("total_revenue", round(col("total_revenue"), 2)) \
    .orderBy("order_date")

# ==============================================
# PYSPARK BASICS: What is orderBy()?
# ==============================================
# orderBy() sorts the DataFrame
#   - Like Excel "Sort A to Z"
#   - orderBy("order_date") sorts by date (oldest first)
#   - For descending: orderBy(col("order_date").desc())
# ==============================================

# Write to Gold table
gold_daily_sales.write.mode("overwrite") \
    .format("delta") \
    .saveAsTable("gold_daily_sales")

print(f"✅ Created: gold_daily_sales ({gold_daily_sales.count()} records)")
display(gold_daily_sales)
```

**Run the cell**

---

**Add new cell - Gold Table 2: Customer Analytics:**

```python
# ========================================
# GOLD TABLE 2: Customer Analytics
# ========================================
# BUSINESS PURPOSE: Answer these questions:
#   - Who are our best customers?
#   - What is each customer's lifetime value?
#   - How many orders has each customer placed?
#   - When did they first/last purchase?
#   - How many different categories do they buy from?

print("\n📊 Creating Gold Table 2: Customer Analytics")
print("-" * 50)

# ==============================================
# PYSPARK BASICS: groupBy with multiple columns
# ==============================================
# We're grouping by customer_id, customer_name_clean, region_clean
#   - This creates one row per customer
#   - customer_name and region stay in the result
#   - All metrics calculated per customer
#
# Why include name and region in groupBy?
#   - We want them in the final table
#   - They're "dimensions" (descriptive attributes)
#   - Metrics will be calculated for each customer
# ==============================================

# ==============================================
# NEW AGGREGATION FUNCTIONS:
# ==============================================
# max("col"): find the maximum (latest date, highest value)
#   - max("order_date") → most recent purchase date
#
# min("col"): find the minimum (earliest date, lowest value)
#   - min("order_date") → first purchase date
#
# These help us understand customer behavior:
#   - When did they first buy? (first_order_date)
#   - When did they last buy? (last_order_date)
#   - Difference = customer lifespan
# ==============================================

gold_customer_analytics = df_silver.groupBy(
    "customer_id", "customer_name_clean", "region_clean"
) \
    .agg(
        count("order_id").alias("total_orders"),
        sum("total_amount").alias("lifetime_value"),
        avg("total_amount").alias("avg_order_value"),
        sum("quantity").alias("total_items_purchased"),
        max("order_date").alias("last_order_date"),
        min("order_date").alias("first_order_date"),
        countDistinct("product_category_clean").alias("categories_purchased")
    ) \
    .withColumn("avg_order_value", round(col("avg_order_value"), 2)) \
    .withColumn("lifetime_value", round(col("lifetime_value"), 2)) \
    .orderBy(col("lifetime_value").desc())

# ==============================================
# PYSPARK BASICS: Sorting in descending order
# ==============================================
# .orderBy(col("lifetime_value").desc())
#   - .desc() means descending (highest first)
#   - Highest-value customers appear at top
#   - Without .desc(), it would be ascending (lowest first)
# ==============================================

# Write to Gold table
gold_customer_analytics.write.mode("overwrite") \
    .format("delta") \
    .saveAsTable("gold_customer_analytics")

print(f"✅ Created: gold_customer_analytics ({gold_customer_analytics.count()} records)")
display(gold_customer_analytics)
```

**Run the cell**

---

**Add new cell - Gold Table 3: Product Performance:**

```python
# ========================================
# GOLD TABLE 3: Product Performance by Category
# ========================================
# BUSINESS PURPOSE: Answer these questions:
#   - What are our best-selling products?
#   - How much revenue does each product generate?
#   - How many times was each product ordered?
#   - What's the average price point for each product?

print("\n📊 Creating Gold Table 3: Product Performance")
print("-" * 50)

# ==============================================
# GROUP BY PRODUCT CATEGORY AND NAME
# ==============================================
# This creates one row per unique product
#   - Groups all orders for "Laptop" together
#   - Calculates metrics for each product
#   - Keeps category and name in the result
# ==============================================

gold_product_performance = df_silver.groupBy(
    "product_category_clean", "product_name"
) \
    .agg(
        count("order_id").alias("times_ordered"),
        sum("quantity").alias("total_quantity_sold"),
        sum("total_amount").alias("total_revenue"),
        avg("unit_price_rounded").alias("avg_unit_price")
    ) \
    .withColumn("avg_unit_price", round(col("avg_unit_price"), 2)) \
    .withColumn("total_revenue", round(col("total_revenue"), 2)) \
    .orderBy(col("total_revenue").desc())

# Write to Gold table
gold_product_performance.write.mode("overwrite") \
    .format("delta") \
    .saveAsTable("gold_product_performance")

print(f"✅ Created: gold_product_performance ({gold_product_performance.count()} records)")
display(gold_product_performance)
```

**Run the cell**

---

**Add new cell - Gold Table 4: Regional Performance:**

```python
# ========================================
# GOLD TABLE 4: Regional Performance
# ========================================
# BUSINESS PURPOSE: Answer these questions:
#   - Which regions generate the most revenue?
#   - How does performance vary by quarter?
#   - How many customers do we have per region?
#   - What's the average order value by region?

print("\n📊 Creating Gold Table 4: Regional Performance")
print("-" * 50)

# ==============================================
# GROUP BY REGION + TIME DIMENSIONS
# ==============================================
# We're grouping by region, year, and quarter
#   - Creates one row per region per quarter
#   - Allows time-series analysis by region
#   - Example: "North region in Q1 2024"
#
# BUSINESS INSIGHT:
#   - Compare regions side-by-side
#   - Track regional trends over time
#   - Identify high/low performing regions
# ==============================================

gold_regional_performance = df_silver.groupBy(
    "region_clean", "year", "quarter"
) \
    .agg(
        count("order_id").alias("total_orders"),
        sum("total_amount").alias("total_revenue"),
        countDistinct("customer_id").alias("unique_customers"),
        avg("total_amount").alias("avg_order_value"),
        sum("quantity").alias("total_items_sold")
    ) \
    .withColumn("avg_order_value", round(col("avg_order_value"), 2)) \
    .withColumn("total_revenue", round(col("total_revenue"), 2)) \
    .orderBy("region_clean", "year", "quarter")

# ==============================================
# PYSPARK BASICS: Multiple orderBy columns
# ==============================================
# .orderBy("region_clean", "year", "quarter")
#   - Sorts by region first (alphabetically)
#   - Then by year within each region
#   - Then by quarter within each year
#   - Like multi-level sorting in Excel
# ==============================================

# ==============================================
# Write to Gold table WITH PARTITIONING
# ==============================================
# We're partitioning this table by year and quarter
#   - Faster queries when filtering by time
#   - Common pattern for time-series data
# ==============================================
gold_regional_performance.write.mode("overwrite") \
    .format("delta") \
    .partitionBy("year", "quarter") \
    .saveAsTable("gold_regional_performance")

print(f"✅ Created: gold_regional_performance ({gold_regional_performance.count()} records)")
display(gold_regional_performance)
```

**Run the cell**

---

**Add new cell - Verification Summary:**

```python
# ========================================
# MEDALLION ARCHITECTURE SUMMARY
# ========================================
print("\n" + "=" * 60)
print("🏆 MEDALLION ARCHITECTURE COMPLETE!")
print("=" * 60)

# ==============================================
# PYSPARK + PANDAS: Converting to Pandas
# ==============================================
# What is toPandas()?
#   - Converts PySpark DataFrame to Pandas DataFrame
#   - Pandas is a popular Python library for data analysis
#   - Easier to work with for small datasets
#   - Here we use it to list all tables
#
# What is SHOW TABLES?
#   - SQL command that lists all tables
#   - Returns table names and database info
# ==============================================
all_tables = spark.sql("SHOW TABLES").toPandas()

print("\n📊 Tables Created:")
print("-" * 60)

# ==============================================
# PYTHON BASICS: Working with Pandas DataFrames
# ==============================================
# .str.contains('bronze'): check if text contains 'bronze'
#   - Like Excel SEARCH() or SQL LIKE
#   - Returns True/False for each row
#   - Used to filter rows
#
# [condition]: filter DataFrame by condition
#   - all_tables[...] keeps only rows where condition is True
#   - Like Excel AutoFilter
# ==============================================

# Bronze tables
bronze_tables = all_tables[all_tables['tableName'].str.contains('bronze')]
print(f"\n🥉 BRONZE LAYER ({len(bronze_tables)} table):")

# ==============================================
# PYTHON BASICS: What is a for loop?
# ==============================================
# for item in collection: - repeat code for each item
#   - Like VBA For Each loop
#   - .iterrows() loops through DataFrame rows
#   - idx = row number, row = the actual row data
# ==============================================
for idx, row in bronze_tables.iterrows():
    table_name = row['tableName']
    count = spark.table(table_name).count()
    print(f"   • {table_name}: {count} records")

# Silver tables
silver_tables = all_tables[all_tables['tableName'].str.contains('silver')]
print(f"\n🥈 SILVER LAYER ({len(silver_tables)} table):")
for idx, row in silver_tables.iterrows():
    table_name = row['tableName']
    count = spark.table(table_name).count()
    print(f"   • {table_name}: {count} records")

# Gold tables
gold_tables = all_tables[all_tables['tableName'].str.contains('gold')]
print(f"\n🥇 GOLD LAYER ({len(gold_tables)} tables):")
for idx, row in gold_tables.iterrows():
    table_name = row['tableName']
    count = spark.table(table_name).count()
    print(f"   • {table_name}: {count} records")

print("\n" + "=" * 60)
print("✅ Your data pipeline is ready for Power BI reporting!")
print("=" * 60)
```

**Run the cell**

**✅ What you'll see:** 
- Complete summary showing all Bronze, Silver, and Gold tables
- Record counts for each table
- All tables visible in left panel!

**🎉 MEDALLION ARCHITECTURE COMPLETE!**

---

# 📝 LAB 3: Power BI Report from Lakehouse (30 minutes)

## Step 3.1: Access SQL Analytics Endpoint

**ACTIONS:**
1. In your lakehouse, look at the top right corner
2. You'll see "SalesLakehouse" with a dropdown
3. Click the dropdown → Select **SQL analytics endpoint**

**✅ What you'll see:** SQL view of your lakehouse with all tables

---

## Step 3.2: Query Gold Tables with SQL

**ACTIONS:**
1. Click **New SQL query** button
2. Paste this SQL:

```sql
-- Query Gold tables using SQL

-- Daily sales summary
SELECT * FROM gold_daily_sales 
ORDER BY order_date;

-- Top customers by lifetime value
SELECT 
    customer_name_clean,
    lifetime_value,
    total_orders,
    avg_order_value
FROM gold_customer_analytics
ORDER BY lifetime_value DESC;

-- Product performance
SELECT 
    product_category_clean,
    product_name,
    total_revenue,
    total_quantity_sold
FROM gold_product_performance
ORDER BY total_revenue DESC;

-- Regional performance
SELECT 
    region_clean,
    total_orders,
    total_revenue,
    unique_customers
FROM gold_regional_performance
ORDER BY total_revenue DESC;
```

3. Click **Run**

**✅ What you'll see:** Results from all your Gold tables!

---

## Step 3.3: Create Semantic Model from Gold Tables

**ACTIONS:**
1. Click **New semantic model** button (top toolbar)
2. Name it: `SalesGoldSemanticModel`
3. **Check these tables:**
   - ☑ gold_daily_sales
   - ☑ gold_customer_analytics
   - ☑ gold_product_performance
   - ☑ gold_regional_performance
4. Click **Confirm**

**⏳ Wait:** Model creation takes 10-15 seconds

---

## Step 3.4: Build Power BI Report from Gold Layer

**ACTIONS:**
1. When semantic model opens, click **New report** button
2. Power BI editor opens

**Build these visuals:**

---

**Visual 1 - KPI Cards (Top Row):**
1. Add **Card** visual
2. From `gold_daily_sales` table → drag `total_revenue` (it will sum automatically)
3. Format the card title: "Total Revenue"

4. Add another **Card** visual
5. Drag `total_orders` from `gold_daily_sales`
6. Title: "Total Orders"

7. Add third **Card** visual
8. From `gold_customer_analytics` → drag `customer_id` field
9. Change aggregation to **Count (Distinct)**
10. Title: "Unique Customers"

---

**Visual 2 - Revenue Over Time (Line Chart):**
1. Click empty space
2. Select **Line chart** visual
3. From `gold_daily_sales`:
   - Drag `order_date` to **X-axis**
   - Drag `total_revenue` to **Y-axis**
4. Title: "Daily Revenue Trend"

---

**Visual 3 - Revenue by Region (Bar Chart):**
1. Click empty space
2. Select **Clustered bar chart**
3. From `gold_regional_performance`:
   - Drag `region_clean` to **Y-axis**
   - Drag `total_revenue` to **X-axis**
4. Title: "Revenue by Region"

---

**Visual 4 - Top Products (Column Chart):**
1. Click empty space
2. Select **Clustered column chart**
3. From `gold_product_performance`:
   - Drag `product_name` to **X-axis**
   - Drag `total_revenue` to **Y-axis**
4. Title: "Product Performance"

---

**Visual 5 - Customer Lifetime Value (Table):**
1. Click empty space
2. Select **Table** visual
3. From `gold_customer_analytics`, drag:
   - `customer_name_clean`
   - `total_orders`
   - `lifetime_value`
   - `avg_order_value`
4. Title: "Top Customers"

---

**Visual 6 - Category Performance (Donut Chart):**
1. Click empty space
2. Select **Donut chart**
3. From `gold_product_performance`:
   - Drag `product_category_clean` to **Legend**
   - Drag `total_revenue` to **Values**
4. Title: "Revenue by Category"

---

**Save Your Report:**
1. Click **File** → **Save**
2. Name: `Sales Analytics - Gold Layer`
3. Click **Save**

**✅ CHECKPOINT:** You now have a complete Lakehouse with Medallion Architecture and beautiful Power BI reports! 🎉🎉

---

# 📝 LAB 4: Bonus - Advanced PySpark Examples (Optional)

## Create a new notebook: `04_Advanced_PySpark`

**Copy this code to explore more PySpark features:**

```python
# ========================================
# ADVANCED PYSPARK EXAMPLES
# ========================================
# These examples show advanced PySpark techniques:
#   - Window functions (rankings, running totals)
#   - Pivot tables
#   - Complex filtering
#   - Joins between tables

from pyspark.sql.functions import *
from pyspark.sql.window import Window

print("🔹 ADVANCED PYSPARK TRANSFORMATIONS")
print("=" * 60)

# Read silver data
df = spark.table("silver_sales")

# ========================================
# 1️⃣ WINDOW FUNCTIONS - Ranking
# ========================================
# PYSPARK ADVANCED: What are Window functions?
#   - Perform calculations ACROSS rows (not just within a row)
#   - Like "running totals" or "rankings" in Excel
#   - Don't collapse rows like groupBy() does
#
# WINDOW SPEC: What is it?
#   - Defines HOW to calculate the window function
#   - partitionBy(): create separate windows for each group
#   - orderBy(): how to sort rows within each window
#
# EXAMPLE: Rank customers by spending within each region
#   - Each region gets its own ranking (partition)
#   - #1 spender in North, #1 spender in South, etc.
#
# PYSPARK FUNCTION: row_number()
#   - Assigns sequential numbers: 1, 2, 3...
#   - .over(windowSpec) applies it using the window spec
# ========================================
print("\n1️⃣ WINDOW FUNCTIONS - Ranking")
print("-" * 60)

# Create window specification
windowSpec = Window.partitionBy("region_clean").orderBy(col("total_amount").desc())

# Add ranking column
df_ranked = df.withColumn("spending_rank", row_number().over(windowSpec))

print("Top spender in each region:")
display(df_ranked.filter(col("spending_rank") == 1)
    .select("region_clean", "customer_name_clean", "total_amount", "spending_rank"))

# ========================================
# 2️⃣ CUMULATIVE AGGREGATIONS (Running Totals)
# ========================================
# BUSINESS USE CASE: Running total of sales
#   - Day 1: $100 (cumulative: $100)
#   - Day 2: $200 (cumulative: $300)
#   - Day 3: $150 (cumulative: $450)
#
# WINDOW SPEC ADVANCED: rowsBetween()
#   - Window.unboundedPreceding: from the first row
#   - Window.currentRow: to the current row
#   - This creates a "running total" window
#
# HOW IT WORKS:
#   1. Group by order_date and sum sales (daily total)
#   2. Create cumulative sum using window function
#   3. Each row shows total up to that date
# ========================================
print("\n2️⃣ CUMULATIVE AGGREGATIONS")
print("-" * 60)

# Define cumulative window (from start to current row)
windowSpec2 = Window.orderBy("order_date").rowsBetween(Window.unboundedPreceding, Window.currentRow)

# Calculate daily sales and cumulative sales
df_cumsum = df.groupBy("order_date").agg(sum("total_amount").alias("daily_sales")) \
    .withColumn("cumulative_sales", sum("daily_sales").over(windowSpec2)) \
    .orderBy("order_date")

print("Cumulative sales over time:")
display(df_cumsum)

# ========================================
# 3️⃣ PIVOT TABLES
# ========================================
# PYSPARK ADVANCED: What is pivot()?
#   - Transforms rows into columns
#   - Like Excel Pivot Table
#   - Takes unique values from one column and makes them column headers
#
# EXAMPLE: Convert categories to columns
#   Before:                After:
#   Region | Category      Region | Electronics | Clothing | Home
#   North  | Electronics   North  | 1500        | 200      | 300
#   North  | Clothing      South  | 1000        | 500      | 400
#   ...
#
# PYSPARK FUNCTION: fillna(0)
#   - Fills null values with 0
#   - If a region has no sales in a category, show 0 instead of null
# ========================================
print("\n3️⃣ PIVOT TABLES")
print("-" * 60)

# Create pivot: Regions as rows, Categories as columns
df_pivot = df.groupBy("region_clean") \
    .pivot("product_category_clean") \
    .agg(sum("total_amount")) \
    .fillna(0)

print("Sales by Region and Category:")
display(df_pivot)

# ========================================
# 4️⃣ COMPLEX FILTERING
# ========================================
# BUSINESS QUESTION: Which customers buy from multiple categories?
#   - These are "diverse" customers
#   - May indicate customer loyalty or broader interests
#
# HOW IT WORKS:
#   1. Group by customer
#   2. Count distinct categories they purchased from
#   3. Filter for count > 1 (bought from multiple categories)
#   4. Order by count descending (most diverse first)
# ========================================
print("\n4️⃣ COMPLEX FILTERING")
print("-" * 60)

# Find customers who bought from multiple categories
multi_category_customers = df.groupBy("customer_id", "customer_name_clean") \
    .agg(countDistinct("product_category_clean").alias("category_count")) \
    .filter(col("category_count") > 1) \
    .orderBy(col("category_count").desc())

print("Customers who purchased from multiple categories:")
display(multi_category_customers)

# ========================================
# 5️⃣ JOIN SILVER WITH GOLD (Enriching Data)
# ========================================
# PYSPARK ADVANCED: What is join()?
#   - Combines two DataFrames based on a common column
#   - Like Excel VLOOKUP or SQL JOIN
#
# JOIN TYPES:
#   - "left": keep all rows from left table (df)
#   - "inner": keep only matching rows
#   - "outer": keep all rows from both tables
#
# BUSINESS USE CASE:
#   - Silver has individual transactions
#   - Gold has customer summary metrics
#   - Join them to see each transaction WITH customer's lifetime value
#
# PYTHON BASICS: Bracket notation in select()
#   - df["column"]: explicitly reference column from specific DataFrame
#   - Needed when both tables have columns with same name
#   - "lifetime_value": no prefix = can come from either table
# ========================================
print("\n5️⃣ JOIN SILVER WITH GOLD")
print("-" * 60)

# Join transaction data with customer analytics
df_silver_enriched = df.join(
    spark.table("gold_customer_analytics"),
    on="customer_id",
    how="left"
).select(
    df["order_id"],
    df["customer_name_clean"],
    df["total_amount"],
    "lifetime_value",
    "total_orders"
)

print("Transactions enriched with customer metrics:")
display(df_silver_enriched.limit(5))

print("\n✅ Advanced PySpark examples complete!")
```

**Run the notebook to see advanced PySpark in action!**

---

# 🎯 Lab Summary - What You Built

## ✅ Completed Components

### 1️⃣ **Data Lakehouse**
- Raw data in Files section: `sales_data.csv`
- 7 Delta tables created across medallion layers:
  - `bronze_sales` (raw ingestion with metadata)
  - `silver_sales` (cleaned & transformed with partitioning)
  - `gold_daily_sales` (daily aggregates)
  - `gold_customer_analytics` (customer metrics)
  - `gold_product_performance` (product metrics)
  - `gold_regional_performance` (regional metrics)

### 2️⃣ **PySpark Notebooks** (4 notebooks)
- `01_Bronze_Layer` - Raw data ingestion
- `02_Silver_Layer` - Data cleaning & transformation
- `03_Gold_Layer` - Business aggregates
- `04_Advanced_PySpark` - Advanced techniques (optional)

### 3️⃣ **Medallion Architecture**
- 🥉 Bronze: Raw data preservation with lineage tracking
- 🥈 Silver: Cleaned, validated, enriched data with partitioning
- 🥇 Gold: Business-ready aggregates for reporting

### 4️⃣ **Power BI Report**
- Lakehouse report with 6 visuals from Gold layer
- Direct Lake mode for optimal performance
- Real-time analytics dashboard

---

# 📚 Key PySpark Concepts You Learned

## Data Operations
✅ Reading CSV files  
✅ Creating DataFrames  
✅ Writing to Delta tables  
✅ Table partitioning  
✅ Reading from tables  

## Transformations
✅ `withColumn()` - Add calculated columns  
✅ `select()` - Choose columns  
✅ `filter()` - Filter rows  
✅ `groupBy()` - Aggregate data  
✅ `join()` - Combine tables  

## Functions Used
✅ String functions: `trim()`, `upper()`  
✅ Date functions: `year()`, `month()`, `quarter()`  
✅ Aggregate functions: `sum()`, `avg()`, `count()`  
✅ Conditional logic: `when()`, `otherwise()`  
✅ Window functions: `row_number()`, cumulative sums  

## Best Practices
✅ Medallion architecture (Bronze → Silver → Gold)  
✅ Data quality checks  
✅ Partitioning for performance  
✅ Adding metadata columns (ingestion_timestamp, source_file)  
✅ Meaningful table and column names  

---

# 🚀 Next Steps - Continue Learning

## Practice More:
1. **Add more data:** Upload a larger CSV (100+ rows) and run the same pipeline
2. **Incremental loads:** Practice appending new data to Bronze layer
3. **Optimize tables:** Learn about OPTIMIZE and VACUUM commands for Delta tables
4. **More transformations:** Experiment with window functions, pivots, and complex joins
5. **Real-time streaming:** Explore Event Streams for real-time data ingestion
6. **Create more Gold tables:** Build additional business aggregates (monthly trends, product affinity)
7. **Add data quality rules:** Implement more sophisticated validation checks in Silver layer

## Official Resources:
- **Lakehouse Tutorial:** https://learn.microsoft.com/en-us/fabric/data-engineering/tutorial-lakehouse-introduction
- **PySpark Guide:** https://learn.microsoft.com/en-us/fabric/data-science/python-guide/python-overview
- **Medallion Architecture:** https://learn.microsoft.com/en-us/fabric/onelake/onelake-medallion-lakehouse-architecture
- **Delta Lake Optimization:** https://learn.microsoft.com/en-us/fabric/data-engineering/delta-optimization-and-v-order
- **Power BI in Fabric:** https://learn.microsoft.com/en-us/fabric/data-engineering/tutorial-lakehouse-build-report
- **Notebook Guide:** https://learn.microsoft.com/en-us/fabric/data-engineering/how-to-use-notebook
- **MSSparkUtils:** https://learn.microsoft.com/en-us/fabric/data-engineering/microsoft-spark-utilities

---

# 🎉 CONGRATULATIONS!

You've successfully completed a hands-on Microsoft Fabric Lakehouse lab covering:
- ✅ Data Lakehouse creation
- ✅ PySpark transformations (read CSV, transform, write Delta tables)
- ✅ Medallion Architecture implementation (Bronze/Silver/Gold)
- ✅ Data quality checks and validation
- ✅ Table partitioning for performance
- ✅ Power BI reporting from Lakehouse Gold layer

**You now have practical experience building a complete data lakehouse with medallion architecture!**

---

**📝 Save this guide for reference as you continue exploring Microsoft Fabric!**

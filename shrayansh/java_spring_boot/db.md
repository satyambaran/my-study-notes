
### Database Concepts: Foreign Key, Normalization, and Normal Forms

#### **1. Foreign Key**
Ensures that the value in one table corresponds to a valid record in another table, preventing orphan records and maintaining relational integrity.

#### **2. Normalization**
**Normalization** is a process in database design that organizes data to minimize redundancy and dependency. The main goal is to ensure that the database is efficient and avoids anomalies during data operations (insert, update, delete). Normalization divides large tables into smaller ones and defines relationships between them.

There are several normal forms (NF), each aiming to reduce redundancy and improve integrity:

    1. 1NF: All values are atomic; no repeating groups.
	2. 2NF: No partial dependency; separate student data.
	3. 3NF: No transitive dependency; separate course and instructor data.
	4. BCNF: Every determinant is a superkey; further decomposition if necessary.

#### **3. 1NF (First Normal Form)**
- **Definition**: A table is in 1NF if:
  - Each column contains atomic (indivisible) values.
  - Each column contains only one type of data (no repeating groups or arrays).

- **Example**:
  Suppose we have a table with multiple phone numbers for a customer:
  
  | CustomerID | Name      | PhoneNumbers   |
  |------------|-----------|----------------|
  | 1          | Alice     | 123-456, 789-012 |
  
  This is not in 1NF because the `PhoneNumbers` column contains multiple values. To bring it to 1NF, we split it into separate rows:

  | CustomerID | Name      | PhoneNumber   |
  |------------|-----------|---------------|
  | 1          | Alice     | 123-456       |
  | 1          | Alice     | 789-012       |

#### **4. 2NF (Second Normal Form)**
- **Definition**: A table is in 2NF if:
  - It is in 1NF.
  - All non-key attributes are fully functionally dependent on the primary key (i.e., no partial dependency).
  
- **Partial Dependency**: When an attribute depends only on a part of the composite primary key, not the entire key.
  
- **Example**:
  Consider a table with a composite primary key (`OrderID`, `ProductID`), and the `ProductName` depends only on `ProductID`, not `OrderID`:

  | OrderID | ProductID | ProductName | Quantity |
  |---------|-----------|-------------|----------|
  | 1       | 101       | Laptop      | 5        |
  
  `ProductName` depends only on `ProductID`. To normalize this, we can create two separate tables:

  **Orders Table**:
  | OrderID | ProductID | Quantity |
  |---------|-----------|----------|
  | 1       | 101       | 5        |

  **Products Table**:
  | ProductID | ProductName |
  |-----------|-------------|
  | 101       | Laptop      |

#### **5. 3NF (Third Normal Form)**
- **Definition**: A table is in 3NF if:
  - It is in 2NF.
  - There are no transitive dependencies (i.e., non-key attributes do not depend on other non-key attributes).

- **Transitive Dependency**: When a non-key attribute depends on another non-key attribute.

- **Example**:
  Consider a table where `City` depends on `ZipCode`, which depends on `CustomerID`:

  | CustomerID | CustomerName | ZipCode | City  |
  |------------|--------------|---------|-------|
  | 1          | Alice        | 12345   | NY    |

  Here, `City` depends on `ZipCode`. To remove the transitive dependency, we split it into two tables:

  **Customers Table**:
  | CustomerID | CustomerName | ZipCode |
  |------------|--------------|---------|
  | 1          | Alice        | 12345   |

  **ZipCode Table**:
  | ZipCode | City  |
  |---------|-------|
  | 12345   | NY    |

#### **6. BCNF (Boyce-Codd Normal Form)**
- **Definition**: A table is in BCNF if:
  - It is in 3NF.
  - If there are multiple candidate keys and a non-key attribute depends on one candidate key but not on the others, it is not in BCNF. BCNF eliminates such anomalies.

#### **7. Denormalization**
- **Definition**: Denormalization is the process of combining normalized tables to improve read performance at the cost of additional storage and potential data anomalies.
- **Purpose**: Used in scenarios where reading data quickly is more important than writing efficiently, such as in data warehouses or read-heavy applications.
  
- **Example**:
  Instead of having separate `Customers` and `Orders` tables, we might combine them into one table to avoid joins:

  | OrderID | CustomerName | ProductName | Quantity | ZipCode |
  |---------|--------------|-------------|----------|---------|
  | 1       | Alice        | Laptop      | 5        | 12345   |

### **Normalization vs Denormalization:**
| **Normalization**                                         | **Denormalization**                                                   |
|-----------------------------------------------------------|-----------------------------------------------------------------------|
| Reduces redundancy by splitting data into multiple tables | Increases redundancy by combining tables for faster read operations   |
| Helps maintain data integrity and avoids anomalies        | Optimized for fast reads, may introduce anomalies in data             |
| Good for transactional systems (OLTP)                     | Good for analytical systems (OLAP)                                    |
| Joins may be required to retrieve data                    | Fewer joins required, improving read performance                      |

### **Conclusion**
- **Normalization** aims to eliminate redundancy and ensure data integrity.
- **Denormalization** improves read performance at the expense of redundancy and potential anomalies.
- Each normal form (1NF, 2NF, 3NF, BCNF) builds on the previous one to further refine the structure of the database.




Certainly! Let’s create an example to illustrate the concepts of **1NF (First Normal Form)**, **2NF (Second Normal Form)**, **3NF (Third Normal Form)**, and **BCNF (Boyce-Codd Normal Form)** through a step-by-step normalization process.

### Initial Table

Suppose we have a table called `Student_Course_Enrollments`:

| StudentID | StudentName | CourseID | CourseName   | Instructor   |
|-----------|-------------|----------|--------------|--------------|
| 1         | Alice       | CS101    | Computer Sci | Dr. Smith    |
| 1         | Alice       | CS102    | Data Science | Dr. Johnson  |
| 2         | Bob         | CS101    | Computer Sci | Dr. Smith    |
| 2         | Bob         | CS103    | AI           | Dr. Taylor   |
| 3         | Charlie     | CS102    | Data Science | Dr. Johnson  |

- PrimaryKey = composite of studentID and courseID
- 2NF:= StudentName depends only on StudentID, create a student table
- 3NF:= 

If we want to account for scenarios where instructors could teach multiple courses, we can further decompose.

1. **Table 1: Courses**

| CourseID | CourseName   |
|----------|--------------|
| CS101    | Computer Sci |
| CS102    | Data Science |
| CS103    | AI           |

2. **Table 2: Instructors**

| InstructorID | Instructor   |
|--------------|--------------|
| 1            | Dr. Smith    |
| 2            | Dr. Johnson  |
| 3            | Dr. Taylor   |

3. **Table 3: Course_Instructors**

| CourseID | InstructorID |
|----------|--------------|
| CS101    | 1            |
| CS102    | 2            |
| CS103    | 3            |

4. **Table 4: Students**

| StudentID | StudentName |
|-----------|-------------|
| 1         | Alice       |
| 2         | Bob         |
| 3         | Charlie     |

5. **Table 5: Course_Enrollments**

| StudentID | CourseID |
|-----------|----------|
| 1         | CS101    |
| 1         | CS102    |
| 2         | CS101    |
| 2         | CS103    |
| 3         | CS102    |



---

### Key Attributes
- **Prime Attributes**: Attributes that form a candidate key in a table.
- **Non-prime Attributes**: All other attributes that do not form a candidate key.

### Transitive Dependency
- Consider two relations: \( R_1(A, B) \) where \( A \) is the primary key, and \( R_2(B, C) \) where \( B \) is the primary key. 
- In this scenario, \( A \) determines \( C \), indicating that both attributes should not exist in the same table.
### Super Key
- for every functional dependency  A \rightarrow B ,  A  should be a superkey.
- super key uniquely identify rows in the table
### Normal Forms Description

| **Normal Form** | **Description** |
|------------------|-----------------|
| **1NF**          | A table is in 1NF if it contains no repeating groups. It eliminates repeating groups from the table. |
| **2NF**          | A table is in 2NF if it is in 1NF and every non-key attribute is fully dependent on the primary key. Non-prime attributes should not exhibit partial dependency on only some prime attributes. |
| **3NF**          | A table is in 3NF if it is in 2NF and has no transitive dependencies among non-prime attributes. No non-prime attribute should be transitively dependent on another non-prime attribute. |
| **BCNF**         | A table is in BCNF if it is in 3NF and every non-prime attribute is fully dependent on the super keys. |
| **4NF**          | A table is in 4NF if it is in BCNF and has no multi-valued dependencies. |

### Definition of Transitive Dependency
A transitive dependency occurs when a non-prime attribute can be determined by another non-prime attribute.

--- 


## ER Model
	1. Entity:
        - An entity is an object or thing in the real world that can be distinctly identified. It can be a physical object (like a student, employee, or product) or a concept (like a course or a project).
    4. Relationships:
        - Relationships describe how entities are associated with one another. For instance, a Student may Enroll in a Course.
        - Relationships are represented by diamonds in ER diagrams and are connected to the related entities.
    5. Cardinality:
        - Cardinality defines the number of instances of one entity that can or must be associated with each instance of another entity.
## Query optimisation 
1. Indexing
2. Query Rewriting (Use exist instead of in)
3. Join Optimization (place smaller tables first, use indexes as join column)
4. Analyze Query Execution Plans (Using EXPLAIN and Look for Scans vs. Seeks)
5. Limit the Result Set (using limit or top)
6. Caching (query caching)
7. Database Configuration
    - Adjust Configuration Settings: Optimize database settings related to memory allocation, connection pooling, and buffer sizes based on workload patterns.
    - Update Statistics: Regularly update the database statistics so that the query optimizer can make informed decisions.
8. Partitioning
9. Batch Processing

## Performance tuning
- Monitoring and Analysis
    * Performance Monitoring Tools
    * Query Performance Analysis
    * Resource Usage Monitoring
- Index Optimization
    * Index Maintenance
    * Analyzing Index Usage
- Query Optimization
- Schema Design and Normalization
    * Proper Normalization
    * Denormalization
- Database Configuration Tuning
    * Memory Allocation
    * Connection Pooling
    * Timeout Setting
- Caching Strategies
    * Query Caching
    * Application-Level Caching
- Partitioning
    * Table Partitioning
    * Index Partitioning
- Concurrency Control
    * Lock Management
    * Isolation Levels
- Batch Processing
    * Batch Inserts/Updates
    * Stored Procedures
- 
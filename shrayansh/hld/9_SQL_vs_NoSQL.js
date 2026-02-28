/*

	^ SQL vs NoSQL

	* Structure:
		SQL: 
			Pre-determined schema before even start using it
			Relation between different tables
		NoSQL:
			Works on unstructured data
			NoSQL Types:
				Key-Value DB (Dynamo DB)
					Can only search/query based on key not value
				Document DB (MongoDB)
					Can query on value as well
				Column wise DB
					Key vs list<column, value>
					Number of column can be dynamic
				Graph DB
					Node Edge based relation ship
					
					Social networking/recommendation
					Node A is friend of Node B.
					Node B is friend of Node C.
	* Nature
		SQL:
			Concentrated:
				Whole data for a particular should be present in one server
		NoSQL:
			Decentralized:
				Whole data can be distributed in nature even for a particular user
	* Scalability
		SQL:
			Vertical: 
				Increase RAM size and storage capacity
			Horizontal/Sharding:
				Data is distributed across servers but not well supported
		NoSQL:
			Horizontal
	* Property
		SQL:
			ACID:
				Atomicity, Consistency, Isolation, Durability
				Data integrity should be maintained
		NoSQL:
			BASE:
				Basically Available 
					Highly Available due to distributed in nature and replication
				Safe state
					State of data can be changed even without interaction
					To manage the same data across replicas
				Eventual consistency
					Eventually all replicas will have the same data

	* When to use which?
		SQL:
			Flexible query funtionality
				Requirement of complex queries
			Data is relational in nature 
			Data Integrity (ACID)
				Can not loose consistency
		NoSQL:
			Basic search query
				What column we need to search for
			Data size is very big
			Eventually consistent
			Highly available and performant (on the cost of inconsistency)
				It can never go down

	If want to achieve ACID then SQL
		Bigger data size requires Sharding, somewhat affects ACID
	If data size is very big then NoSQL


	^ NoSQL Keys:
		#1. Primary Key
			A unique identifier for a record in a NoSQL database
			#1a. Surrogate Key
				System-generated unique identifier used as a primary key in absence of user defined primary key
				Like UUID
			#1b. Natural Key
				User selected primary key with a real-world meaning
				Like: Email Id
		#2. Shard Key
			Determines the distribution of data across different shards
			Each shard holds a portion of entire database
			Each shard should be SELF-SUFFICIENT to reduce the query
			It reduces load on the server
		#3. Partition Key
			Used to distribute data across different nodes/partitions in a single NoSQL database
			It can be done on each Shard (called Multi-Level Partitioning)
			It improves performance of each server/shard
			It could be a single column or a composite key
		#4. Clustering Key
			Used to sort and determine the order of rows within a partition
			Can be multiple
				userid#dob#orderid...
		#5. Composite Key
			A key composed of multiple columns or fields that together uniquely identify a record
			Used when a single field is not sufficient to uniquely identify a record
				Like: Chat history(two different partition key of user table make partition key of chat history table)
			It might consist of a partition key and one or more clustering keys.
		#6. Foreign Key (not commonly used in NoSQL)
			A column value used to identify record in different table
				Like address_id in user table
		#7. Secondary Key (Secondary Index)
			An additional key that allows queries on fields other than the primary key
			Only in Document like DB
				eg: MongoDB
			Used to speed up queries on those fields
	^ SQL     
		Concurrency Control Mechanisms
			• Database-Level Locks: Use pessimistic or optimistic locking to ensure atomicity at the database level.
			• Distributed Locks: Use tools like Redis with the Redlock algorithm for distributed locking.
			• Atomic Operations: Ensure atomic updates using database operations.
			• Two-Phase Commit (2PC): For transactions spanning multiple services.
			• Eventual Consistency: Implement retry logic for eventual consistency where strict consistency is not required.

			Optimistic Locking 
				• Assumes that conflicts are rare and allows multiple transactions to access the same data simultaneously. 
				• Instead of locking the data, it checks for conflicts only when the transaction is ready to commit.
				• Each transaction reads the data without locking it.
				• When a transaction attempts to update the data, it checks whether the data has been modified by another transaction since it was last read.
				• If the data has been modified (a conflict), the transaction is typically rolled back, and the application can retry the operation.
			Pessimistic Locking
				• When a transaction wants to read or write data, it places a lock on the data.
				• Other transactions that want to read or write the same data must wait until the first transaction releases the lock



		B-tree Index:
		CREATE INDEX idx_name ON table_name(column_name);
		Hash Index:
		CREATE INDEX idx_name ON table_name USING hash (column_name);
		Expression Index:
		CREATE INDEX idx_lower_name ON table_name ((lower(column_name)));
		Partial Index:
		CREATE INDEX idx_active_users ON users (user_id) WHERE active = true;
		CREATE INDEX idx_name ON table_name USING gist (column_name);
		CREATE INDEX idx_name ON table_name USING spgist (column_name);
		CREATE INDEX idx_name ON table_name USING gin (column_name);
		CREATE INDEX idx_name ON table_name USING brin (column_name);
*/
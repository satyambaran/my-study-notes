/*
^   Microservices vs Monolithic(Legacy)

* Monolithic:
	Overload IDE
	Scaling is hard
	Tight Coupled
	High Testing is required
	CI/CD is tough
* Microservices:
	Proper breakage needed
	Scaling is easy and service based
	Loosely Coupled
	High Testing is required
	Latency Issue
	Hard to monitor, can have cascading affect on services
		: If one service breaks, then other using it can break as well
	Transaction management issue
		: Transaction is loosely coupled, so if a transaction is used by couple of services, then it'll be tough to handle
	` Phases of Microservices (patterns)
		: These patterns help us deciding micro-services
		# Decomposition: 
			Decompose by business functionality/compatibility
				? based on management of Order, Account, Login, Product, Billing etc Functionality
			Decompose by sub-domain (DDD: Domain driven design)
				? based on sub-domains
					~ Order management sub-domain: 
						-> it can have multiple microservices inside it like Placing, Tracking
					~ Payment management sub-domain:
						-> Forward payment, refund
		# Database:
			Database per service
			Database shared
		# Communication:
			via API, Events
		# Integration:
			API Gateway, UI, some other application
		# Observability:
		# Monitoring:
	` Design patterns of microservices:
		# Strangler: 
			Helps in factoring of monolithic into micro-services
			Slowly moving monolithic traffic to microservices created
				~ Step 1: A new microservice for handling payment processing is developed.
				~ Step 2: The existing payment processing feature in the legacy system is kept functional but gradually rerouted to the new microservice.
				~ Step 3: As the new payment service becomes stable and feature-complete, the old payment processing feature can be deprecated and eventually removed.
		# SAGA & CQRS:
			Data management in microservice
				? Shared Database
					~ Not so successful, as some microservice might need scaling
					~ Database not very easy alterable
					~ Easy join
					~ ACID property easy
				? Database for each individual microservice (SAGA and CQRS)
					~ Easy individual scaling
					~ No service will touch the DB of other services, will ask for API to other service if needed
					~ Easy to choose DB as per requirement (NoSQL, SQL)
					~ Impossible to join 
						-> solved by CQRS
					~ Local ACID holds, but global transaction is not possible
						-> solved by SAGA
		@ SAGA 
			: Simple API for Grid Applications
			Sequence of local transaction
				~ Each service pushes some events for next services
				~ On failure, service pushes an failure event for previous service to consume and rollback
			Types Of SAGA
				? Choreography
					~ Each service reads data from queue and pushes the data to next queue after processing it
					~ If processing at any service fails, then it will pushed to the queue of previous service to rollback
				? Orchestrator
					~ There will be an Orchestrator who will make sure that each events gets processed by each services sequentially and if it fails at some service, it will make sure to acknowledge the same to previous service
					~ One orchestrator will interact to each micro-service
		@ CQRS
			: Command query request segregation
				~ Create, update and delete ( CUD ) queries will be segregated from read ( R ) queries
				~ All services will do CUD queries on its own DB 
				> But for read queries, there will be a common view DB to join from all the DB
				~ This common DB will update itself by one of following ways
					keep reading events from each DB
					DB trigger/Procedure


	
	



*/


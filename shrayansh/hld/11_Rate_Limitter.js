/*

	Need:
		DDoS attack:
			Unwanted requests just to consume our resourcse
	Algorithms:
		# Token Bucket
			Description:
				Capacity of holding token is fixed
				Refiller keeps adding 'n' token at a fixed rate, any extra token will overflow
				Driven a configuration file, can chane it

			Functioning:
				When a request comes,
				If token is present
					Request will consume that and request will be fulfilled
				Else
					Request will be denied

			Implementation:
				For each user, 3 token per minute
				Refiller 2 requests per minute
					{counter:2, timestamp: "10:01:01"} -> success
					{counter:1, timestamp: "10:01:15"} -> success
					{counter:0, timestamp: "10:01:25"} -> success
					{counter:0, timestamp: "10:01:35"} -> failure 429
						~ refiller~
					{counter:2, timestamp: "10:01:01"}
					{counter:1, timestamp: "10:03:01"} -> success
			Bucket refill rate needs to be carefully configured to match system capacity
			Might create when all the user starts using their bucket at once
		# Leaking Bucket
			Bucket has fixed capacity, any extra request will be thrown 429
			Processes requests at a constant rate
			Implementation:
				Using queue
			Functioning:
				When a request comes,
				If queue is full
					Request will be pushed in the queue and request will be fulfilled
				Else
					Request will be denied
			Problematic in case where a server is known to have traffic fluctuation regulalrly
				Like Netflix will have more traffic in night compare to morning 
			Doesn’t handle bursts well, as it flattens them into a steady rate.
			Can lead to high latencies once the queue becomes full.
		# Fixed Window Counter
			In each fixed window of time(5 min), there will 3 requests counter
				{counter:2, timestamp: "10:01:01"} -> success
				{counter:1, timestamp: "10:02:15"} -> success
				{counter:0, timestamp: "10:03:25"} -> success
				{counter:0, timestamp: "10:04:35"} -> failure 429
					~ after 10:05 ~
				{counter:3, timestamp: "10:05:01"}
				{counter:1, timestamp: "10:06:01"} -> success
			Requests can be very densed around refill time
				All 6 requests can be very close to 10:05
		# Sliding Window Log
			3 req per minute
			Each request’s timestamp is logged
			Once a timestamp is passed one minute, it'll get removed
			Requires more memory and processing power to maintain the log of timestamps
		# Sliding Window Counter
			Fixed Window Counter + Sliding Window Log

			Implementation
				From 8:00:00 to 8:01:00 = 12 requests
				From 8:01:00 to 8:01:45 = 5 requests

				Number of requests in last one minute (8:00:45 to 8:01:45)
					= 15*(12/60) + 5 = 8 requests
			
				Can use this number to rate limit
			Very efficient space and time wise
			Just need to store 2 integers


	Design:
		* Components
			# Counter
				Need to use redis to store because of frequency
			# Config
				Need to use redis to store because of frequency
		* Placement
			API Gateway:
				If it's present then we can rate limitter here only before any auth check
				Then only request will go to server
			No API Gateway:

	Distrubuted:
		For distribute rate limitter, we can use redis
		Redis is centralize data store
		Redis doesn't maintain atomicity
			We can use some pre-existing solution to add atomicity on the cost of latency
			` Lua scripting
*/

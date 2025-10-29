/*

Design a URL Shortener

Requirement Clarifiactions:
    ~ Traffic:
        10M per day => 3.65B per year for 100 years
        total url = 365 B url
        total characters = 26+26+10 = 62 

        62^6 = 56 B
        62^7 = 3.5 Trillion
        62^8 = 218 Trillion
    ~ Short url length? = 8 since some url might not be utilised

Design:
    ~ If we use some MD5(32 chars) or SHA1(40 chars), then we have to trim it but it'll result in collision

    ~ So we'll use Base62 of really large numbers
        1000 base 62 = g8 // 0123456789abcdef...zABCD...Z

        We need real big id and id-generator (that too distributed and collision free)
        # Approach 1
            Centralized ticket server to auto-increment id
                Single point of failure
                Heavy load
        # Approach 2
            Snowflake (twitter)
                1bit timestamp machine_id sequence_no
        # ZooKeeper
           Distributed application can co-ordinate with each other reliably using this 
           ZooKeeper creates n ranges and assigns to each server, losing those ranges if server breaks is fine
           Once server fills or crashes, then we can select new range
           Use padding(with some unused chars like g======) if required

           Helps in generating unique ids in distributed environment







*/

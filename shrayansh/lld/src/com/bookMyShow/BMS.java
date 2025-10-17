package src.com.bookMyShow;

public class BMS {

}

/*
 * User -> city -> movies -> theater-wise shows -> seat selection -> booking ->
 * payment -> ticket
 * 
 * 
 * Objects:
 * User, Movie, City, Theatre, Screen/Halls, Shows, Seats, Booking, Payment
 * 
 * Concurrency:
 * Two user shouldn't be able to book same seat
 * 
 * Once a seat is checked out, it should be unavailable for next 5 minutes,
 * whether payment is success or failed(so that user can do repayment)
 * 
 * Optimistic lock (check version when updating when you read first)
 * Pessimistic lock (lock while reading )
 * 
 * Lock using redis, it'll expire after some time
 */
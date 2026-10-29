/*
    Requirements:
        number of entrances and exits
        type of spot
        payment Strategy (charge per minute, one time charge, mixed)
        parking spot should be nearest to entry
        floors
    Objects:
        Vehicle, ticket, gate(entry and exit), spots
        Vehicle: no., type enum 2, 3, 4 wheeler
        Entrance gate:
            Find parking spot, update spot, generate ticket
        Ticket: Entry time, spot
        Spot: id, vehicle id, price, type, isEmpty
        Exit gate: cost, payemnt,free spot
*/
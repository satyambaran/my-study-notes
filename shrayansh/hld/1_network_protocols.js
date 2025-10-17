/*
^ Network Protocols:

OSI model:  (Open Systems Interconnection)
    : A conceptual framework used to understand and implement networking protocols in seven distinct layers
| layers        | Example protocols | Functions                                     |
|---------------|-------------------|-------------------------------------------    |
| Application   | HTTP, FTP, SMTP   | Network services to end-user applications     |
| Presentation  | SSL/TLS, JPEG     | Data translation, encryption, compression     |
| Session       | RPC               | Session management, establishment, termination|
| Transport     | TCP, UDP          | Reliable data transfer, error correction      |
| Network       | IP, ICMP          | Routing, logical addressing                   |
| Data Link     | Ethernet, Wi-Fi   | MAC addressing, error detection               |
| Physical      | Ethernet cables   | Transmission of raw bitstreams                |

Application Layer:
    * Client-Server:    HTTP, FTP, SMTP(send email), IMAP(read/access mail), Web-Sockets (Web sockets still talks client-server, cant do client client)
    * P2P:              WebRTC (server and each client can talk to each other)
Transport & Network Layer:
    * TCP/IP: 
        : Data packets goes in sequence on Maintained Connection, and gets acknowledged. If not acknowledged, server will send it again.
    * UDP/IP: 
        : Data packets sent on Various Connections parallely. No sequencing but fast.
        : Used in live streaming, cause no one gonna go back
        : used in WebRTC


*/

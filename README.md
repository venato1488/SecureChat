# TCP_chat_room
The TCP chat application is a simple chat application built on top of the TCP protocol. The application allows multiple clients to connect to a central server and exchange messages with each other in real-time. With its basic functionality and easy-to-use interface, it is a great tool for anyone looking to connect with others over a network

## Functionality
When a client connects to the server, they are prompted to enter a username. The username must be at least 3 characters long and cannot contain special characters. Once the user has entered a valid username, they can start sending messages to the chat room.

To send a private message to another connected client, the user can type "/p <Recipient> <Message>". The recipient must be a valid username of a connected client. The message will only be visible to the recipient and the sender.

To see a list of all connected clients, the user can type "/members".

## Classes
The application consists of the following classes:

### Server
The Server class is responsible for starting and running the chat server. It listens for incoming connections from clients and creates a new ClientHandler for each connection. The ClientHandler is responsible for handling communication between the server and the client.

### ClientHandler
The ClientHandler class is responsible for handling communication between the server and the client. It listens for incoming messages from the client and broadcasts them to all other connected clients or sends a private message. It also maintains a list of all connected clients and their respective IP addresses and ports.

### Client
The Client class is responsible for connecting to the chat server and sending and receiving messages. It allows the user to send broadcast message or private messages to other connected clients and also displays a list of all connected clients.

### Message
The Message class represents a message sent by a client. It contains information about the sender, timestamp, and message content.

### PrivateMessage
The PrivateMessage class represents a private message sent by a client to another client. It extends the Message class and also contains information about the recipient.

### Member
The Member class represents a connected client. It contains information about the client's username, IP address, port, and whether or not the client is the coordinator.





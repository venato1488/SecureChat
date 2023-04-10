# SecureChat

This is a multi-threaded chat application that utilizes SSL/TLS encrypted communication between a server and multiple clients. The application consists of a server that listens for incoming client connections, and clients that can send messages to the server. The server is responsible for broadcasting messages to all connected clients and handling private messages between clients.

## Application Workflow:

1. The server is initialized with SSL/TLS configurations and starts listening for client connections on the specified port.
2. Clients connect to the server using SSL/TLS encryption, providing a unique username.
3. The server validates the uniqueness of the client's username and either approves or denies the connection.
4. Once connected, clients can send messages to the server. The server broadcasts messages to all connected clients or handles private messages between clients.
5. Clients receive messages from the server and display them in their chat interface.
6. The first client to join the chat becomes the coordinator, who has the authority to kick other clients from the chat.
7. Clients can disconnect from the server at any time, and the server handles their disconnection by updating the list of connected clients and assigning a new coordinator if necessary.

## Classes Description:
### 1. Server:

• Initializes the SSL/TLS configurations, creates a server socket, and listens for incoming client connections.
• Upon a new client connection, creates a new ClientHandler thread to handle the communication with that client.
• Handles closing the server socket when necessary.

### 2. ClientHandler (implements Runnable):
• Manages the communication with a single client.
• Reads incoming messages from the client, handles private messages, and broadcasts messages to all connected clients.
• Tracks connected clients, manages the member list, and updates clients with the latest member list.
• Handles kicking clients, assigning a new coordinator, and closing connections when necessary.

### 3. Client:

• Establishes a secure SSL/TLS connection to the server.
• Sends and receives messages from the server using SSL/TLS encryption.
• Handles user input, which includes sending messages and processing commands, such as private messaging or exiting the chat.
• Starts two threads: one for receiving messages from the server and another for handling user input.
• Displays the received messages on the client's interface.
• Provides methods to close the client socket and disconnect from the server.

### 4. Member (implements Serializable):
• Represents a connected client with their username, IP address, port, and coordinator status.
• Provides getter and setter methods for its properties.
### 5. Message (implements Serializable, MessageInterface):

• Represents a chat message with its sender, timestamp, and content.
• Provides getter and setter methods for its properties.
### 6. PrivateMessage (extends Message):

• Represents a private chat message between two clients.
• Adds a recipient property to the Message class and provides getter and setter methods for it.
### 7. MessageFactory:

• A utility class that creates messages based on the MessageType enum (USERNAME, BROADCAST, PRIVATE, SERVER).
• Provides a static method, createMessage(), which takes the message type, sender, content, and recipient (for private messages) as arguments, and returns a MessageInterface object.
### 8. MessageInterface:

• An interface implemented by the Message and PrivateMessage classes.
• Provides method signatures for getting and setting content and sender.


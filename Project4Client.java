//******************************************************************************
//
//  Developer:     Cory Munselle
//
//  Project #:     Project 4
//
//  File Name:     Project4Client.java
//
//  Course:        COSC 4301 - Modern Programming
//
//  Due Date:      3/11/2022
//
//  Instructor:    Fred Kumi
//
//  Description:   Connects to the server via opened socket. Once connected,
//                 sends data to the server for processing. Valid data will be
//                 accepted and the sum, mean, and standard deviation of the data
//                 will be returned.
//
//  Notes:         I'm going to be calling a generic IOException quite frequently.
//                 This isn't exactly by choice, but what I see as a necessary evil
//                 due to my inexperience with client/server communications and the
//                 extensive list of possible exceptions that could be thrown in the
//                 event of any issues that arise. They all just happen to be derived
//                 from IOException, so I stuck with that.
//
//******************************************************************************

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Project4Client {
    // initializing socket and input output streams
    private DataOutputStream outStream = null;
    private DataInputStream inStream = null;
    private Socket connection = null;

    private final String address;
    private final int port;

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   String address, int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public Project4Client(String address, int port) {
        this.address = address;
        this.port = port;
        // Establishing connection with server
        establishConnection(address, port);
    }

    //***************************************************************
    //
    //  Method:       main
    //
    //  Description:  The main method of the program
    //
    //  Parameters:   String array
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public static void main(String[] argv) {
        Project4Client client = new Project4Client("127.0.0.1", 4301);
    }

    //***************************************************************
    //
    //  Method:       establishConnection
    //
    //  Description:  Attempts to connect to the server
    //
    //  Parameters:   String address, int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void establishConnection(String address, int port) {
        developerInfo();
        System.out.println("Attempting to establish connection...");
        int connectionAttempts = 0;

        // The server attempts to connect to the client a maximum of five times
        while (connection == null && connectionAttempts < 4) {
            try {
                connection = new Socket(address, port);
                outStream = new DataOutputStream(connection.getOutputStream());
                inStream = new DataInputStream(connection.getInputStream());
            } catch (IOException e) {
                System.err.println("Unable to establish connection. Reattempting...");
            }
            // This is fine specifically because the connection attempts are reset at the
            // beginning of each method call, so the fact that it's still being incremented even
            // during a successful connection doesn't change anything.
            // There is a small possibility that the server connects at the last second and the program
            // still terminates, but reattempting connection multiple times seemed out of the scope
            // of the assignment regardless so I'm not going to worry about it too much.
            finally {
                connectionAttempts += 1;
            }
        }
        if (connectionAttempts > 4) {
            System.err.println("Unable to establish a connection. Terminating...");
            System.exit(-1);
        }
        else {
            System.out.println("Connection established.");
            getInput();
        }

    }

    //***************************************************************
    //
    //  Method:       getInput
    //
    //  Description:  Constantly queries the user for input until "Bye" is supplied
    //
    //  Parameters:   String array
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void getInput() {
        Scanner userInput = new Scanner(System.in);
        System.out.println("Please supply three positive integers separated by spaces. If you wish to quit, type 'Bye'.");
        String str = "";

        // This loop is responsible for the entire execution of the program.
        while (!str.equals("Bye")) {
            str = userInput.nextLine();
            try {
                // Writes string to server
                outStream.writeUTF(str);

                // this is here only because if Bye was typed the program would try to get
                // input from the server when there was none and the program would crash
                if (!str.equals("Bye"))
                    // Read strings coming from the server
                    getResponse(inStream.readUTF());
            }
            catch(IOException e) {
                System.out.println("Failed to send string to the server. Checking connection...");

                // Checks the connection status
                checkConnection();
            }
        }
        // This isn't executed until the user types Bye so there's no logic needed
        closeClient();
    }

    //***************************************************************
    //
    //  Method:       checkConnection
    //
    //  Description:  Verifies that the client can still reach the server
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void checkConnection() {
        try {
            // Checks if the connection IP address is readable with a timeout of 1 second
            // if it ISN'T reachable, set connection to null and try to connect again.
            if (connection.getInetAddress().isReachable(1000)) {
                System.out.println("Lost connection to the server. Attempting reconnection...");
                connection = null;
                establishConnection(this.address, this.port);
            }
        }
        // It shouldn't ever reach here, but just in case print the stack trace and exit
        catch (Exception e) {
            System.err.println("Unknown error occurred. Printing stack trace and terminating...");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //***************************************************************
    //
    //  Method:       getResponse
    //
    //  Description:  Displays any messages sent by the server
    //
    //  Parameters:   String message
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void getResponse(String message) {
        System.out.println(message);
    }

    //***************************************************************
    //
    //  Method:       closeClient
    //
    //  Description:  Gracefully closes the client and all connections
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void closeClient() {
        System.out.println("Closing the client...");
        // I don't know how garbage collection works exactly, so I decided to
        // implement a closeClient method to make sure things were properly closed
        // to the best of my ability
        try {
            connection.close();
            inStream.close();
            outStream.close();
        }
        // Shouldn't get here but in case it does just terminate
        catch (IOException e) {
            System.err.println("Failed to gracefully close connections. Terminating...");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //***************************************************************
    //
    //  Method:       developerInfo (Non Static)
    //
    //  Description:  The developer information method of the program
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void developerInfo()
    {
        System.out.println("Name:    Cory Munselle");
        System.out.println("Course:  COSC 4301 Modern Programming");
        System.out.println("Project: Four\n");

    } // End of the developerInfo method
}
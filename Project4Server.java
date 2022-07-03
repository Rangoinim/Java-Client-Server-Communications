//******************************************************************************
//
//  Developer:     Cory Munselle
//
//  Project #:     Project 4
//
//  File Name:     Project4Server.java
//
//  Course:        COSC 4301 - Modern Programming
//
//  Due Date:      3/11/2022
//
//  Instructor:    Fred Kumi
//
//  Description:   Opens a socket and waits for client connection. Once connected,
//                 valid data sent from the client is processed and sent back
//                 to the client. If the client disconnects the server stays open
//                 and waits for connection.
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Project4Server
{
    private DataInputStream inStream = null;
    private DataOutputStream outStream = null;
    private Socket connection = null;
    private ServerSocket server = null;

    private int number1;
    private int number2;
    private int number3;
    private final ArrayList<Integer> numbers;

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public Project4Server(int port) {
        this.number1 = 0;
        this.number2 = 0;
        this.number3 = 0;

        numbers = new ArrayList<>();

        startServer(port);
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
        Project4Server server = new Project4Server(4301);

        server.developerInfo();

        server.acceptConnection();
    }

    //***************************************************************
    //
    //  Method:       startServer
    //
    //  Description:  Attempts to start the server
    //
    //  Parameters:   int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void startServer(int port) {
        System.out.println("Attempting to start the server...");
        try {
            server = new ServerSocket(port);
        }
        catch (IOException e) {
            System.err.println("Unable to start server. Printing stack trace and terminating...");
            e.printStackTrace();
            closeServer();
        }
        System.out.println("Server started.");
    }

    //***************************************************************
    //
    //  Method:       acceptConnection
    //
    //  Description:  Attempts to open the server to connections
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void acceptConnection() {
        System.out.println("Waiting for a client to connect...");
        try {
            connection = server.accept();
            inStream = new DataInputStream(connection.getInputStream());
            outStream = new DataOutputStream(connection.getOutputStream());
            System.out.println("Connected to client.");
            receiveInput();
        }
        catch (IOException e) {
            System.err.println("Unknown error occurred. Printing stack trace and terminating...");
            e.printStackTrace();
            closeServer();
        }
    }

    //***************************************************************
    //
    //  Method:       checkConnection
    //
    //  Description:  Checks to see if a client is still connected
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void checkConnection() {
        try {
            if (connection.getInetAddress().isReachable(1000)) {
                System.out.println("Lost connection to the client. Waiting for reconnection...");
                acceptConnection();
            }
        }
        catch (IOException e) {
            System.err.println("Unknown error occurred. Printing stack trace and closing the server...");
            e.printStackTrace();
            closeServer();
        }
    }

    //***************************************************************
    //
    //  Method:       receiveInput
    //
    //  Description:  Accepts input from the client
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void receiveInput() {
        String str;
        // I consider this to be a coding crime but I'm honestly not sure how else to
        // keep the server running at all times. The assignment doesn't specify anything
        // that says the server needs to have functionality to close the server gracefully
        // so I didn't include any
        while (true) {
            try {
                str = inStream.readUTF();
                // basic output so anybody viewing the server console (me) knows what is being sent
                System.out.println("The string sent from the client is: " + str);
                // if the provided string was successfully parsed, go ahead and process
                if (parseInput(str)) {
                    processInput();
                }
            }
            // For handling errors
            catch(IOException io) {
                System.err.println("Failed to receive input from client. Checking connection...");
                checkConnection();
            }
        }
    }

    //***************************************************************
    //
    //  Method:       parseInput
    //
    //  Description:  Attempts to split the client provided string
    //                into three separate integers
    //
    //  Parameters:   String line
    //
    //  Returns:      boolean success
    //
    //**************************************************************
    public boolean parseInput(String line) {
        boolean success = false;
        String[] tokens;

        if (line.matches("Bye")) {
            kickClient();
        }
        else {
            // regex that splits a string by any number of spaces to account for extra spacebar presses
            tokens = line.split("\\s+");

            if (tokens.length != 3) {
                sendToClient("Invalid number of digits. Please provide exactly three numbers.");
            }
            else {
                try {
                    // I did three separate int variables instead of an array because it's
                    // easier to work with. I also directly referenced the indexes in the string
                    // array because all the other checks should prevent anything else before this
                    number1 = Integer.parseInt(tokens[0]);
                    number2 = Integer.parseInt(tokens[1]);
                    number3 = Integer.parseInt(tokens[2]);
                    success = true;
                }
                catch (NumberFormatException e) {
                    sendToClient("One or more supplied values is not a number. Please supply three numbers, \nseparated by spaces.");
                }
            }
        }
        return success;
    }

    //***************************************************************
    //
    //  Method:       processInput
    //
    //  Description:  Takes the parsed integers and attempts to build
    //                an ArrayList of integers
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void processInput() {
        if (number1 <= 0 || number2 <= 0 || number3 <= 0) {
            sendToClient("All numbers provided must be greater than zero.");
        }
        else if (number1 >= number2) {
            sendToClient("The first number must be less than the second.");
        }
        else if (number3 != 1 && number3 != 2) {
            sendToClient("The third number must be either 1 or 2.");
        }
        else {
            // ensures the arraylist is clear after each client message
            numbers.clear();
            for (int i = number1; i < number2-number3; i+=2) {
                numbers.add(i + number3);
            }
            // once arraylist of numbers is generated, go to calculate output
            calculateOutput();
        }
    }

    //***************************************************************
    //
    //  Method:       calculateOutput
    //
    //  Description:  Calculates the sum, mean, and standard dev of the
    //                generated arraylist of integers based on client
    //                input
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void calculateOutput() {
        double sum = 0.0;
        double mean = 0.0;
        double stddev = 0.0;

        for (Integer number : numbers) {
            sum += number;
        }
        if (sum != 0) {
            mean = sum / numbers.size();
        }

        for (Integer number : numbers) {
            stddev += Math.pow(number - mean, 2);
        }

        if (stddev != 0) {
            stddev = Math.sqrt(stddev / numbers.size());
        }
        // it is my understanding that all formatting should be left to the client, but the assignment doesn't specify
        // any special formatting for the output so I'm doing it in the server.
        sendToClient(String.format("%s%.0f%n%s%.3f%n%s%.3f",  "Sum: ", sum, "Mean: ", mean, "Standard deviation: ", stddev));
    }

    //***************************************************************
    //
    //  Method:       closeServer
    //
    //  Description:  Attempts to close the server
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void closeServer() {
        // This is my attempt to make sure everything is tidily cleaned up
        // instead of just quitting, but this should not be executed under
        // normal circumstances
        try {
            connection.close();
            inStream.close();
            outStream.close();
            System.exit(-1);
        }
        // In the event that everything fails to close, terminates the program anyway
        catch (IOException | NullPointerException e) {
            System.err.println("Failed to gracefully close the server. Terminating...");
            System.exit(-1);
        }
    }

    //***************************************************************
    //
    //  Method:       kickClient
    //
    //  Description:  Kicks the client off the server
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void kickClient() {
        // similar to closeClient but after closing the server it opens the server
        // for connection again
        try {
            connection.close();
            inStream.close();
            outStream.close();
            acceptConnection();
        }
        catch (IOException | NullPointerException e) {
            System.err.println("Failed to gracefully close the server. Terminating...");
            System.exit(-1);
        }
    }

    //***************************************************************
    //
    //  Method:       sendToClient
    //
    //  Description:  Attempts to send strings to client for display
    //
    //  Parameters:   String strToSend
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void sendToClient(String strToSend) {
        try {
            outStream.writeUTF(strToSend);
        }
        catch (IOException e) {
            System.out.println("Failed to send message to client. Displaying on the server...");
            System.out.println(strToSend);
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
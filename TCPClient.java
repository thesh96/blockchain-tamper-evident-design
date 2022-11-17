import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/*
    Name: Amithesh Ramesh
    Andrew id: amithesr
 */

public class TCPClient {
    //Data Members
    private int serverPort;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner = new Scanner(System.in);
    private int userChoice;
    private int difficulty;
    private String data;
    private int blockID;
    private String clientRequest;

    //Constructor
    public TCPClient() {
        serverPort = 7777;
        clientSocket = null;
    }

    //This method builds the client request message JSON
    public void buildClientRequestMessage() {
        JSONObject obj = new JSONObject();
        switch (userChoice) {
            case 0:
            case 2:
            case 3:
            case 5:
                obj.put("userChoice", userChoice);
                break;
            case 1:
                obj.put("userChoice", userChoice);
                obj.put("difficulty", difficulty);
                obj.put("data", data);
                break;
            case 4:
                obj.put("userChoice", userChoice);
                obj.put("blockID", blockID);
                obj.put("data", data);
                break;
        }
        this.clientRequest = obj.toString();
    }

    //This method displays the menu and takes the input from the user
    public void menuDisplay() {
        System.out.println("0. View basic blockchain status\n1. Add a transaction to the blockchain\n2. Verify the blockchain\n3. View the blockchain\n4. Corrupt the chain\n5. Hide the corruption by repairing the chain\n6. Exit");
        userChoice = Integer.parseInt(scanner.nextLine());
    }

    //This method processes the input entered by the user and gets additional details
    public void menuProcessing() {
        String output;
        switch (userChoice) {
            case 0:
            case 3:
            case 2:
            case 5:
                buildClientRequestMessage();
                output = new JSONObject(communicate()).getString("Response");
                System.out.println(output);
                break;
            case 1:
                System.out.println("Enter difficulty > 0");
                this.difficulty = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter transaction");
                this.data = scanner.nextLine();
                //Makes a call to the method which builds the client request
                buildClientRequestMessage();
                //This parses the response JSON objects and prints the response
                output = new JSONObject(communicate()).getString("Response");
                System.out.println(output);
                break;
            case 4:
                System.out.println("corrupt the Blockchain\nEnter block ID of block to corrupt");
                this.blockID = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter new data for block " + blockID);
                this.data = scanner.nextLine();
                //Makes a call to the method which builds the client request
                buildClientRequestMessage();
                //This parses the response JSON objects and prints the response
                output = new JSONObject(communicate()).getString("Response");
                System.out.println(output);
                break;
        }
    }

    //This method is solely responsible to communicate with the server
    public String communicate() {
        String output = "";
        try {

            //Initialising the client socket for communicating with the server
            clientSocket = new Socket("localhost", serverPort);
            //BufferedReader to read the inputstream from the socket
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //PrintWriter to write a stream of output to the socket
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            out.println(clientRequest);
            out.flush();
            output = in.readLine();

        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    public static void main(String[] args) {
        TCPClient obj = new TCPClient();
        obj.menuDisplay();
        //Displays the menu until Exit is chosen
        while (obj.userChoice != 6) {
            obj.menuProcessing();
            obj.menuDisplay();
        }
        System.out.println("Client side quitting. The remote server is still running");
    }
}

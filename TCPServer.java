import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Scanner;

/*
    Name: Amithesh Ramesh
    Andrew id: amithesr
 */

public class TCPServer {
    private Socket clientSocket;
    private int serverPort;
    private ServerSocket listenSocket;
    private BlockChain blockChain = new BlockChain();
    private JSONObject serverResponse;

    //Constructor
    public TCPServer() {
        clientSocket = null;
        serverPort = 7777;
        listenSocket = null;
    }

    //This method builds the response JSON object
    public void buildServerResponse(String serverOutput) {
        serverResponse = new JSONObject();
        serverResponse.put("Response", serverOutput);
    }

    //This method parses the request JSON object and processes it based on the choice entered by the user
    public void requestProcessing(JSONObject reqObj) {
        int userChoice;
        String serverOutput = "";
        int difficulty;
        String data;
        int blockID;
        long startTime;
        long endTime;
        boolean chainValidity;

        //Parsing the JSON object to find user choice
        userChoice = reqObj.getInt("userChoice");
        switch (userChoice) {
            case 0:
                //View the blockchain status
                System.out.println("Received request from client to view blockchain status");
                serverOutput = String.format("Current size of chain: %d\nDifficulty of most recent block: %d\nTotal difficulty for all blocks: %d\nApproximate hashes per second on this machine: %d\nExpected total hashes required for the whole chain: %.6f\nNonce for most recent block: %d\nChain hash: %s\n", blockChain.getChainSize(), blockChain.getLatestBlock().getDifficulty(), blockChain.getTotalDifficulty(), blockChain.getHashesPerSecond(), blockChain.getTotalExpectedHashes(), blockChain.getLatestBlock().getNonce(), blockChain.getChainHash());
                //Call the method which builds the response JSON object
                buildServerResponse(serverOutput);
                break;
            case 1:
                //Add a new block
                System.out.println("Received request from client to add new block to the chain");
                difficulty = reqObj.getInt("difficulty");
                data = reqObj.getString("data");
                startTime = System.currentTimeMillis();
                blockChain.addBlock(new Block(blockChain.getChainSize(), new Timestamp(System.currentTimeMillis()), data, difficulty));
                endTime = System.currentTimeMillis();
                serverOutput = String.format("Total execution time to add this block was " + (endTime - startTime) + " milliseconds\n");
                //Call the method which builds the response JSON object
                buildServerResponse(serverOutput);
                break;
            case 2:
                //Verify the validity of the chain
                System.out.println("Received request from client to check the validity of the chain");
                startTime = System.currentTimeMillis();
                chainValidity = blockChain.isChainValid();
                endTime = System.currentTimeMillis();
                if (chainValidity == true) {
                    serverOutput = (String.format("\nVerifying entire chain\nChain verification: " + chainValidity + "\nTotal execution time required to verify the chain was " + (endTime - startTime) + " milliseconds\n"));
                } else {
                    serverOutput = (String.format("\nVerifying entire chain\nChain verification: " + chainValidity + "\n" + blockChain.getCorruptNodeDetails() + "\nTotal execution time required to verify the chain was " + (endTime - startTime) + " milliseconds\n"));
                }
                //Call the method which builds the response JSON object
                buildServerResponse(serverOutput);
                break;
            case 3:
                //View the blockchain
                System.out.println("Received request from client to view the blockchain");
                serverOutput = blockChain.toString() + "\n";
                //Call the method which builds the response JSON object
                buildServerResponse(serverOutput);
                break;
            case 4:
                //Corrupt a block
                System.out.println("Received request from client to corrupt a block in the blockchain");
                blockID = reqObj.getInt("blockID");
                data = reqObj.getString("data");
                blockChain.getBlockStorage().get(blockID).setData(data);
                serverOutput = (String.format("\nBlock " + blockID + " now holds " + data + "\n"));
                //Call the method which builds the response JSON object
                buildServerResponse(serverOutput);
                break;
            case 5:
                //Repair the corrupted chain
                System.out.println("Received request from client to repair the chain");
                startTime = System.currentTimeMillis();
                blockChain.repairChain();
                endTime = System.currentTimeMillis();
                serverOutput = String.format("\nRepairing the entire chain\nTotal execution time required to repair the chain was " + (endTime - startTime) + " milliseconds\n");
                //Call the method which builds the response JSON object
                buildServerResponse(serverOutput);
                break;
        }

    }

    //This method is solely responsible to communicate with the client
    public void communicate() {
        Scanner in;
        PrintWriter out;
        JSONObject reqObj;
        String output = "";

        try {
            System.out.println("Server is running");
            //Initialising server socket to listen on server port
            listenSocket = new ServerSocket(serverPort);
            blockChain.initAndGenesisBlock();
            while (true) {
                //Accept client socket connection
                clientSocket = listenSocket.accept();
                //Initialising scanner to read input stream from the client
                in = new Scanner(clientSocket.getInputStream());
                //Initialising PrintWriter to write output stream to the client
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

                while (in.hasNextLine()) {
                    reqObj = new JSONObject(in.nextLine());
                    requestProcessing(reqObj);
                    out.println(serverResponse.toString());
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static void main(String args[]) {
        TCPServer obj = new TCPServer();
        obj.communicate();
    }
}
/*
    Name: Amithesh Ramesh
    Andrew id: amithesr
 */

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

public class Block {

    //Data Members
    private int index;
    private Timestamp timestamp;
    private String data;
    private String previousHash;
    private BigInteger nonce;
    private int difficulty;

    //Default Constructor
    public Block() {

    }

    //Overloaded Constructor
    public Block(int index, Timestamp timestamp, String data, int difficulty) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.previousHash = "";
        this.nonce = BigInteger.ZERO;
    }

    //Member Functions

    //Getters and Setters
    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    /*This method calculates a SHA-256 hash for the concated message containing index, timestamp, data, previousHash, nonce
    and difficulty. It also converts the message digest into its hexa equivalent and returns a hexa string.

    */
    public String calculateHash() {
        String toBeHashed = this.index + this.timestamp.toString() + this.data + this.previousHash + this.nonce + this.difficulty;
        StringBuilder sb = new StringBuilder();
        byte[] hashedMessage = null;
        MessageDigest md = null;
        //Calculating the SHA-256 hash
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(toBeHashed.getBytes());
        hashedMessage = md.digest();
        //Converting the message digest into a hexadecimal byte by byte -> https://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-l
        for (byte b : hashedMessage) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    //This method returns the nonce of the block
    public BigInteger getNonce() {
        return this.nonce;
    }

    //This method checks the validity of an individual block by comparing the difficulty with the leading zeroes in it's hash.
    public boolean blockValidity(String hash) {
        StringBuilder sb;
        String leadingZeroes;
        sb = new StringBuilder();

        //Build a string with the number of zeroes equal to difficulty and use it to compare it with the hash string
        for (int i = 0; i < this.difficulty; i++) {
            sb.append(0);
        }
        leadingZeroes = sb.toString();
        if (hash.substring(0, this.difficulty).equals(leadingZeroes)) {
            return true;
        } else {
            return false;
        }
    }

    /*This method computes the proof of work for a block in the chain. It first computes the hash and check if the block is
    valid, if it isn't the nonce is updated by one and the same process is repeated until the block becomes valid.
    Finally, the correct hash of the block is returned.

     */
    public String proofOfWork() {
        //Local variables
        String hash;
        BigInteger one = new BigInteger("1");
        //Calculate the hash
        hash = calculateHash();
        while (blockValidity(hash) == false) {
            nonce = nonce.add(one);
            hash = calculateHash();
        }
        return hash;
    }

    //This method creates a JSON object of the block parameters and returns the string of the object.
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("index", this.getIndex());
        obj.put("time stamp", this.getTimestamp().toString());
        obj.put("Tx ", this.getData());
        obj.put("PrevHash", this.getPreviousHash());
        obj.put("nonce", this.getNonce());
        obj.put("difficulty", this.getDifficulty());
        return obj.toString();
    }

    public static void main(String[] args) {

    }

}

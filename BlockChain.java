import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;

/*
    Name: Amithesh Ramesh
    Andrew ID: amithesr
 */

public class BlockChain {
    //Data Members
    private ArrayList<Block> blockStorage;
    private String chainHash;
    private int hashesPerSecond;
    private String corruptNodeDetails;

    //Default Constructor
    public BlockChain() {
        this.blockStorage = new ArrayList<>();
        this.chainHash = "";
        this.hashesPerSecond = 0;
    }

    //Member Functions
    /*This method adds a new block to the chain. While doing this, it sets the previous hash of the block added to the
    current chain hash and then computes the correct hash of the new block using proof of work, and finally stores this
    as the current chain hash.
     */
    public void addBlock(Block newBlock) {
        String goodHashOfNewBlock;

        blockStorage.add(newBlock);
        if (newBlock.getIndex() != 0) {
            newBlock.setPreviousHash(chainHash);
        }
        goodHashOfNewBlock = newBlock.proofOfWork();
        chainHash = goodHashOfNewBlock;
    }

    //This method computes the number of hashes that the machine can compute for the default string "oooooooo" per second
    public void computeHashesPerSecond() {
        long startTime;
        long endTime;
        MessageDigest md;
        byte[] digest;
        double seconds = 0.0;

        startTime = System.currentTimeMillis();
        md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 1000000; i++) {
            md.update("oooooooo".getBytes());
            digest = md.digest();
        }
        endTime = System.currentTimeMillis();

        seconds += (endTime - startTime) / 1000.0;

        this.hashesPerSecond = (int) (1000000 / seconds);
    }

    //This method returns the block which has the given index
    public Block getBlock(int i) {
        for (Block b : this.blockStorage) {
            if (b.getIndex() == i) {
                return b;
            }
        }
        return null;
    }

    //This method computes the size of the block chain and returns it
    public int getChainSize() {
        return blockStorage.size();
    }

    //This is a getter for the member variable hashesPerSecond
    public int getHashesPerSecond() {
        return this.hashesPerSecond;
    }

    //This method returns the latest block in the chain
    public Block getLatestBlock() {
        return blockStorage.get(blockStorage.size() - 1);
    }

    //This method returns the current time
    public Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    //This method returns the chain hash of the blockchain
    public String getChainHash() {
        return this.chainHash;
    }

    //This method returns the details of the corrupt node
    public String getCorruptNodeDetails() {
        return this.corruptNodeDetails;
    }

    //This method returns the array list of blocks which contains the chain
    public ArrayList<Block> getBlockStorage() {
        return this.blockStorage;
    }

    //This method returns the total difficulty of all the blocks in the chain
    public int getTotalDifficulty() {
        int result = 0;
        for (Block b : this.blockStorage) {
            result += b.getDifficulty();
        }
        return result;
    }

    /*This method computes the total expected hashes by raising 16 to the power of the block difficulty.
    This is done for all the blocks and the sum is returned
     */
    public double getTotalExpectedHashes() {
        double result = 0.0;
        for (Block b : this.blockStorage) {
            result += Math.pow(16.0, b.getDifficulty());
        }
        return result;
    }

    /*This method checks if the chain is valid based on two conditions ->
    1. If hash value is equal to the previous hash of the next block (if size > 1 and middle block)
    2. If the block is valid(comparing the difficulty with leading zeroes in the hash).

     */
    public boolean isChainValid() {
        String hash;
        int sizeOfChain = getChainSize();
        //Checks if hash equals chain hash where chain size is 1
        if (sizeOfChain == 1) {
            hash = blockStorage.get(0).calculateHash();
            if (blockStorage.get(0).blockValidity(hash) != false) {
                if (hash.equals(chainHash)) {
                    corruptNodeDetails = "";
                    return true;
                } else {
                    corruptNodeDetails = "";
                    return false;
                }

            } else {
                corruptNodeDetails = String.format("..Improper has on node 0 Does not begin with " + "0".repeat(blockStorage.get(0).getDifficulty()));
                return false;
            }
        }
        //Checks the same when chain size > 1
        else if (sizeOfChain > 1) {
            for (int i = 1; i < blockStorage.size(); i++) {
                hash = blockStorage.get(i - 1).calculateHash();
                if (blockStorage.get(i - 1).blockValidity(hash) == false) {
                    corruptNodeDetails = String.format("..Improper has on node " + (i - 1) + " Does not begin with " + "0".repeat(blockStorage.get(i - 1).getDifficulty()));
                    return false;
                } else if (!hash.equals(blockStorage.get(i).getPreviousHash())) {
                    return false;
                }
            }
            //For the last block, it checks if the hash equals the chain hash of the chain
            hash = blockStorage.get(sizeOfChain - 1).calculateHash();
            if (blockStorage.get(sizeOfChain - 1).blockValidity(hash) != false) {
                if (hash.equals(chainHash)) {
                    corruptNodeDetails = "";
                    return true;
                } else {
                    corruptNodeDetails = "";
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    /*This method repairs the corrupt nodes in the chain by recomputing the hashes of the corrupt blocks and
    setting the chain hash and previous hashes accordingly
     */
    public void repairChain() {
        for (Block b : blockStorage) {
            b.proofOfWork();
            if (getChainSize() == 1) {
                chainHash = b.calculateHash();
            } else if (b.getIndex() == getChainSize() - 1) {
                chainHash = b.calculateHash();
            } else {
                blockStorage.get(b.getIndex() + 1).setPreviousHash(b.calculateHash());
            }
        }
    }

    //This method returns an JSON object string of the block chain
    public String toString() {
        JSONObject chain = new JSONObject();
        JSONArray chainArray = new JSONArray();
        //Adding all the block to the JSON array
        for (Block b : blockStorage) {
            chainArray.put(new JSONObject(b));
        }
        chain.put("ds_chain", chainArray);
        chain.put("chainHash", this.chainHash);

        return chain.toString();
    }

    //This method is computes the hashes per second and adds the genesis block
    public void initAndGenesisBlock() {
        Block block;

        block = new Block(0, new Timestamp(System.currentTimeMillis()), "Genesis", 2);
        this.addBlock(block);
        this.computeHashesPerSecond();
    }

    public static void main(String[] args) {

    }
}

/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/
import java.lang.*;
import java.util.*;

public class MyLZW {
    private static final int R = 256;       // number of input chars
    private static int L = 512;             // number of codewords = 2^W
    private static int W = 9;               // codeword width begins with 9 and increases later
    private static char mode;               // Stores mode
    private static float compressedData = 0;
    private static float uncompressedData = 0; 
    private static float oldRatio;          // Defined as uncompressed/compressed
    private static float newRatio;          // Defined as uncompressed/compressed
    private static boolean hasSavedRatio = false;    

    public static void compress() { 
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF

        // Store the mode as the first item in the compressed file
        BinaryStdOut.write(mode, 8);

        // Build the compressed version
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();

            // If in monitor mode, update the compressed/uncompressed values after each addition
            if (mode == 'm') {
                compressedData = compressedData + W;
                uncompressedData = uncompressedData + (8*t);
            }


            if (t < input.length()) {
                // Add as usual
                if (code < L) {
                    st.put(input.substring(0, t + 1), code++);
                }

                // Try to increase code size
                else {
                    // Increase code width if possible
                    if (increaseCodeSize()) {
                        st.put(input.substring(0, t + 1), code++);
                    }

                    // Otherwise, reset codebook according to mode
                    else {
                        // Reset mode
                        if (mode == 'r') {
                            // Create new trie and reinitialize with alphabets
                            st = new TST<Integer>();
                            for (int i = 0; i < R; i++) {
                                st.put("" + (char) i, i);
                            }
                            code = R+1;
                            W = 9;
                            L = 512;

                            st.put(input.substring(0, t + 1), code++);
                        }

                        // Monitor mode
                        if (mode == 'm') {
                            // If a previous ratio exists, check the ratio of ratios against
                            // threshold 1.1
                            if (hasSavedRatio) {
                                newRatio = uncompressedData/compressedData;
                                // If ratio passes threshold, create new trie and reinitialize
                                if (oldRatio/newRatio > 1.1) {
                                    st = new TST<Integer>();
                                    for (int i = 0; i < R; i++) {
                                        st.put("" + (char) i, i);
                                    }
                                    code = R+1;
                                    W = 9;
                                    L = 512;
                                    hasSavedRatio = false;
                                    newRatio = 0;
                                    oldRatio = 0;

                                    st.put(input.substring(0, t + 1), code++);
                                }
                            }
                            // Otherwise, save the current ratio
                            else {
                                oldRatio = uncompressedData/compressedData;
                                hasSavedRatio = true;
                            }
                        }
                    }
                }
            }

            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        // Check the mode the file was compressed with
        mode = BinaryStdIn.readChar();

        // Sring size set to max possible so array doesnt need to be resized (2^16)
        String[] st = new String[65536];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++) {
            st[i] = "" + (char) i;
        }
        st[i++] = "";                       // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true) {
            // If in monitor mode, update the compressed/uncompressed values after each addition
            if (mode == 'm') {
                compressedData = compressedData + W;
                uncompressedData = uncompressedData + (8*val.length());
            }

            // Check if we have run out of codes
            if (i >= L) {
                // If so, try increasing code width
                if (increaseCodeSize()) {
                    // If size increased, proceed as usual
                }
                // Otherwise, reset codebook according to mode
                else {
                    if (mode == 'r') {
                        // Create new trie and reinitialize with alphabet
                        st = new String[65536];
                        for (i = 0; i < R; i++) {
                            st[i] = "" + (char) i;
                        }
                        st[i++] = "";
                        W = 9;
                        L = 512;
                    }

                    if (mode == 'm') {
                        // If a previous ratio exists, check the ratio of ratios against
                        // threshold 1.1
                        if (hasSavedRatio) {
                            newRatio = uncompressedData/compressedData;
                            // If ratio passes threshold, create new trie and reinitialize
                            if (oldRatio/newRatio > 1.1) {
                                // Create new trie and reinitialize with alphabet
                                st = new String[65536];
                                for (i = 0; i < R; i++)
                                    st[i] = "" + (char) i;
                                st[i++] = ""; 
                                W = 9;
                                L = 512;
                                hasSavedRatio = false;
                                newRatio = 0;
                                oldRatio = 0;
                            }
                        }
                        // Otherwise, save the current ratio
                        else {
                            oldRatio = uncompressedData/compressedData;
                            hasSavedRatio = true;
                        }
                    }
                }
            }

            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];

            if (i == codeword) {
                s = val + val.charAt(0);
            }   // special case hack

            if (i < L) {
                st[i++] = val + s.charAt(0);
            }
            val = s;
        }
        BinaryStdOut.close();
    }

    public static boolean increaseCodeSize() {
        if (W < 16) {
            W = W + 1;
            L = (int) Math.pow(2, W);
            return true;
        }
        else {
            return false;
        }
    }

    public static void main(String[] args) {
        if (args[0].equals("-")) {
            mode = args[1].charAt(0);
            compress();
        }
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}

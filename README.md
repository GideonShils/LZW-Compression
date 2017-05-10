# LZW-Compression

Runs as follows:
Compress: java MyLZW - mode < input > output.lzw

Decompress: java MyLZW + <input.lzw > output

Mode options:
- n: do nothing
- r: resets the dictionary once it is full
- m: monitors the compression ratio and resets the dictionary once it the ratio has passed a certain threshold

Code is based off of data structures from Algorithms 4th edition by Robert Sedgewick and Kevin Wayne

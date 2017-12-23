# LZW-Compression

**Invoke as follows:**

Compress: 

```
java MyLZW - mode < input > output.lzw
```

Decompress: 

```
java MyLZW + <input.lzw > output
```


### Mode options:

1. n: do nothing
2. r: resets the dictionary once it is full
3. m: monitors the compression ratio and resets the dictionary once the ratio has passed a certain threshold


Code is based off of data structures from Algorithms 4th edition by Robert Sedgewick and Kevin Wayne

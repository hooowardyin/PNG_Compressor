# PNG_Compressor

an png file compressor and decompressor

PNG compression

Implement method

a) read the PNG file
For reading the PNG file, the process is same as the project 1. method getRGB(x,y) is used to read the RGB data of a pixel (x,y).
to parsing the RGB data of a pixel, first is to understand the return value of getRGB() is a 24-bits data and it is arranged as:
RRRRRRRRGGGGGGGGBBBBBBBB
Left 8-bit are color-red’s data, followed by 8-bit green data and then 8-bit blue data.
To separate them out, we do shifting then “AND” with one byte 0xFF as following (pseudo code):
red= (getRGB(x,y) >> 16) & 0xFF
green= (getRGB(x,y) >> 8) & 0xFF
blue= (getRGB(x,y)) & 0xFF

b) compress the PNG file
After get RGB data from the original file, there are 6 steps to do to get the compressed file.

1. Convert RGB to YUV
2. Doa2DDCTof8x8block
3. Do the Quantization
4. Store DC values by DPCM
5. Store AV values by using ZigZag scan and run length code
6. Entropy coding

(1) covert RGB to YUV
to convert RGB to YUV, we use the matrix from the lecture slide:
and then we do a YUV 420. By using this method, we save every value of Y channel, but only one U, V value for every 4 Y values. This will help us reduce the size of U, V array by 75%.

(2) Do a 2D DCT of 8x8 block
Same as we did in programming assignment 3. We calculate the DCT matrix first using N=8. where,
     
after the matrix T is confirmed, we can get the transpose of T by switch Ci,j and Cj,i after that, we do the DCT transfer for every 8x8 block of the U, and V matrix.

(3) do the quantization
We use the quantization table from the lecture slide to divide the corresponding DCT coefficients for U and V matrix:
  this step helps us reduce the DCT coefficient and finally save some space in compression.
Note: this step is considered as lossly part in this program, because we need to round the F[]/Q[].

(4) store the DC value by DPCM
DC value is the first element of a 8x8 block. This value is often large, so we can do a differential pulse code modulation by only saving the difference from the previous values.
We make the increment to 8 for our i and j to locate DC.

(5) store the AC value by using ZigZag scan and run length code
due to the AC values are always close to zero, we use ZigZag scan to combine most of the “0” together. After that, we code the matrix as list of pairs. (skipped zero’s , value)
   the above example from the lecture slide gives the idea of connecting “0’s” together and saving space.

(6) entropy coding
Huffman coding is used in this step.
Huffman coding can use very few bits to represent the information. It average length is between the [entropy] and [entropy+1]
An external library is used to complete this step.
(I originally implemented Huffman coding by myself, however, I found if I can’t store the Huffman code in bit-level, I won’t achieve what I want. The final size will be even larger than the original file. That is reason I choose to use the external library on this step)

c) decompress
The order of decompress is the reverse of the compress.
Do the entropy decode first, and then fill the U and V matrix by their DC and AC values, followed by inverse DCT. After that, apply the quantization table to every 8x8 block as multiplicator. Finally, convert YUV to RGB. We will get our goals.

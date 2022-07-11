package huffman;

import java.io.*;


// Decompresses an input file that was compressed with HuffmanCompress, to an output file.
public final class huffDe {

    public static void main(String[] args) throws IOException {
        // Show what command line arguments to use
        if (args.length == 0) {
            System.err.println("Usage: java HuffmanDecompress InputFile OutputFile");
            System.exit(1);
            return;
        }

        // Otherwise, decompress

        doHuffmanDe(args[0],args[1] );

    }

    public static void doHuffmanDe(String inFile, String outFile) throws IOException {
        File inputFile = new File(inFile);
        File outputFile = new File(outFile);

        BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            CanonicalCode canonCode = readCode(in);
            CodeTree code = canonCode.toCodeTree();
            decompress(code, in, out);
        } finally {
            out.close();
            in.close();
        }
    }


    public static CanonicalCode readCode(BitInputStream in) throws IOException {
        int[] codeLengths = new int[257];
        for (int i = 0; i < codeLengths.length; i++) {
            // For this file format, we read 8 bits in big endian
            int val = 0;
            for (int j = 0; j < 8; j++)
                val = val << 1 | in.readNoEof();
            codeLengths[i] = val;
        }
        return new CanonicalCode(codeLengths);
    }


    static void decompress(CodeTree code, BitInputStream in, OutputStream out) throws IOException {
        HuffmanDecoder dec = new HuffmanDecoder(in);
        dec.codeTree = code;
        while (true) {
            int symbol = dec.read();
            if (symbol == 256)  // EOF symbol
                break;
            out.write(symbol);
        }
    }

}
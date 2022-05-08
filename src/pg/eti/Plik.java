package pg.eti;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Plik {
    protected int liczbaZapisow = 0;
    protected int liczbaOdczytow = 0;
    protected int rozmiarStrony = 4;
    protected int stronPrimary = 1;
    protected int stronOverflow = 1;
    protected FileChannel fci;
    protected FileChannel fcd;
    protected String fileName;
    public static final String DATA_EXT = ".dat";
    public static final String INDEX_EXT = ".idx";
    public static final int INDEX_SIZE = Long.SIZE / 8;

    public Plik(String fileName)
            throws IOException {
        File plik = new File(fileName + Plik.INDEX_EXT);
        plik.delete();
        plik = new File(fileName + Plik.DATA_EXT);
        plik.delete();

        this.fileName = fileName;
        fci = new RandomAccessFile(fileName + Plik.INDEX_EXT, "rw").getChannel();
        fci.force(true);
        fcd = new RandomAccessFile(fileName + Plik.DATA_EXT, "rw").getChannel();
        fcd.force(true);
    }

    public String getFileName() {
        return fileName;
    }

    public void close()
            throws IOException {
        fcd.close();
        fci.close();
    }

    public void usun() {
        File plik = new File(fileName + Plik.INDEX_EXT);
        plik.delete();
        plik = new File(fileName + Plik.DATA_EXT);
        plik.delete();
    }

    public long appendEntry(Strona strona)
            throws IOException {

        // Calculate the data index for append to data
        // file and append its value to the index file.
        long byteOffset = fci.size();
        long index = byteOffset / (long) Plik.INDEX_SIZE;
        long dataOffset = (int) fcd.size();
        ByteBuffer bb = ByteBuffer.allocate(Plik.INDEX_SIZE);
        bb.putLong(dataOffset);
        bb.flip();
        fci.position(byteOffset);
        fci.write(bb);

        // Append serialized object data to the data file.
        byte[] se = Strona.serialize(strona);
        fcd.position(dataOffset);
        fcd.write(ByteBuffer.wrap(se));

        liczbaZapisow++;

        return index;
    }

    public Strona readEntry(long index)
            throws IOException, ClassNotFoundException {

        // Get the data index and -length from the index file.
        long byteOffset = index * (long) Plik.INDEX_SIZE;
        ByteBuffer bbi = ByteBuffer.allocate(Plik.INDEX_SIZE);
        fci.position(byteOffset);
        if (fci.read(bbi) == -1) {
            throw new IndexOutOfBoundsException("Specified index is out of range");
        }
        bbi.flip();
        long dataOffset = bbi.getLong();
        bbi.rewind();
        long dataOffsetNext;
        if (fci.read(bbi) == -1) {
            dataOffsetNext = fcd.size();
        } else {
            bbi.flip();
            dataOffsetNext = bbi.getLong();
        }
        int dataSize = (int) (dataOffsetNext - dataOffset);

        // Get the serialized object data in a byte array.
        byte[] se = new byte[dataSize];
        fcd.position(dataOffset);
        fcd.read(ByteBuffer.wrap(se));

        liczbaOdczytow++;

        // Deserialize the byte array into an instantiated object.
        return Strona.deserialize(se);
    }


    public int getLiczbaZapisow() {
        return liczbaZapisow;
    }

    public int getLiczbaOdczytow() {
        return liczbaOdczytow;
    }

    public int getRozmiarStrony() {
        return rozmiarStrony;
    }

    public int getStronPrimary() {
        return stronPrimary;
    }

    public int getStronOverflow() {
        return stronOverflow;
    }
}

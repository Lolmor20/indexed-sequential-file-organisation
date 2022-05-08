package pg.eti;

import java.io.*;
import java.util.List;

public class Strona implements Serializable {
    private byte[] rekordy;
    private int numerStrony;

    public Strona(int numerStrony, byte[] rekordy) {
        this.numerStrony = numerStrony;
        this.rekordy = rekordy;
    }

    public void setRekordy(byte[] rekordy) {
        this.rekordy = rekordy;
    }

    public byte[] getRekordy() {
        return rekordy;
    }

    public int getNumerStrony() {
        return numerStrony;
    }

    public void setNumerStrony(int numerStrony) {
        this.numerStrony = numerStrony;
    }

    public static byte[] serialize(Strona strona)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(strona);
        oos.flush();

        return baos.toByteArray();
    }

    public static Strona deserialize(byte[] byteArray)
            throws IOException, ClassNotFoundException {

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteArray));
        Strona strona = (Strona) ois.readObject();

        return strona;
    }

    @Override
    public String toString() {
        try {
            StringBuilder s = new StringBuilder();
            s.append("Strona nr: ").append(numerStrony).append("\n");
            List<Rekord> lista = SBDUtils.bajtyNaListe(rekordy);
            for (Rekord rekord : lista
            ) {
                s.append(rekord.toString());
                s.append("\n");
            }
            return s.toString();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}

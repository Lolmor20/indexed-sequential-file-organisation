package pg.eti;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class SBDUtils {

    static public void wypelnijPustymiRekordami(PlikZDanymi plik) {
        for (int j = 0; j < plik.getStronPrimary() + plik.getStronOverflow(); j++) {
            List<Rekord> rekordy = new ArrayList<>();
            for (int i = 0; i < plik.getRozmiarStrony(); i++) {
                rekordy.add(new Rekord());
            }
            try {
                plik.appendEntry(new Strona(j, listaNaBajty(rekordy)));
            } catch (IOException e) {
                System.out.println("blad");
            }
        }
    }

    static public int ktoraStronaOverflow(int klucz, PlikZDanymi dane)
            throws IOException, ClassNotFoundException {
        for (int i = dane.getStronPrimary(); i < dane.getStronPrimary() + dane.getStronOverflow(); i++) {
            Strona s = dane.readEntry(i);
            List<Rekord> rekordy = bajtyNaListe(s.getRekordy());
            for (Rekord r : rekordy
            ) {
                if (r.getKlucz() == klucz) {
                    return i;
                }
            }
        }
        return -1;
    }

    static public void wypelnijPlikTestowy(File plik)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(plik, true));
        List<Integer> klucze = new ArrayList<>();

        //dodanie rekordu
        for (int i = 0; i < 100; i++) {
            StringBuilder str = new StringBuilder();
            str.append("D ");
            for (int j = 0; j < 5; j++) {
                str.append(ThreadLocalRandom.current().nextInt(1, 10)).append(" ");
            }
            int klucz = ThreadLocalRandom.current().nextInt(1, 10000);
            str.append(klucz).append("\n");
            klucze.add(klucz);
            writer.append(str);
        }

        //aktualizacja rekordu
        for (int i = 0; i < 30; i++) {
            StringBuilder str = new StringBuilder();
            str.append("A ");
            str.append(klucze.get(ThreadLocalRandom.current().nextInt(0, klucze.size() - 1))).append(" ");
            for (int j = 0; j < 5; j++) {
                str.append(ThreadLocalRandom.current().nextInt(1, 10)).append(" ");
            }
            str.append(ThreadLocalRandom.current().nextInt(1, 10000)).append("\n");
            writer.append(str);
        }

        //usuniecie rekordu
        for (int i = 0; i < 40; i++) {
            StringBuilder str = new StringBuilder();
            str.append("U ");
            str.append(klucze.remove(ThreadLocalRandom.current().nextInt(0, klucze.size() - 1))).append("\n");
            writer.append(str);
        }

        //odczytanie rekordu
        for (int i = 0; i < 80; i++) {
            StringBuilder str = new StringBuilder();
            str.append("O ");
            str.append(klucze.get(ThreadLocalRandom.current().nextInt(0, klucze.size() - 1))).append("\n");
            writer.append(str);
        }

        writer.close();
    }

    static public <T> byte[] listaNaBajty(List<T> lista)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(lista);
        return bos.toByteArray();
    }

    static public <T> List<T> bajtyNaListe(byte[] bajty)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bajty);
        ObjectInputStream ois = new ObjectInputStream(bis);
        ArrayList<T> list = (ArrayList<T>) ois.readObject();
        return list;
    }
}

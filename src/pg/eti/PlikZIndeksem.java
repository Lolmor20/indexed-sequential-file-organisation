package pg.eti;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class PlikZIndeksem extends Plik {
    public PlikZIndeksem(String fileName)
            throws IOException {
        super(fileName);
    }

    public void wyswietlIndeks()
            throws IOException, ClassNotFoundException {
        List<Integer> indeksy;
        System.out.println("Indeksy:\n");
        for (int i = 0; i < stronPrimary; i++) {
            Strona strona = readEntry(i);
            indeksy = SBDUtils.bajtyNaListe(strona.getRekordy());
            for (int j = 0; j < indeksy.size(); j += 2) {
                if (indeksy.get(j + 1) != -1) {
                    System.out.println("Nr strony: " + indeksy.get(j + 1) + " Klucz: " + indeksy.get(j) + "\n");
                }
            }
        }
    }

    public void stworzIndeks(PlikZDanymi dane)
            throws IOException, ClassNotFoundException {
        close();
        usun();
        fci = new RandomAccessFile(fileName + Plik.INDEX_EXT, "rw").getChannel();
        fci.force(true);
        fcd = new RandomAccessFile(fileName + Plik.DATA_EXT, "rw").getChannel();
        fcd.force(true);
        List<Integer> indeksy = new ArrayList<>();
        int ktoraStrona = 0;
        for (int i = 0; i < dane.getStronPrimary(); i++) {
            Strona strona = dane.readEntry(i);
            List<Rekord> rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
            indeksy.add(rekordy.get(0).getKlucz());
            indeksy.add(i);
            if (indeksy.size() / 2 >= getRozmiarStrony()) {
                appendEntry(new Strona(ktoraStrona, SBDUtils.listaNaBajty(indeksy)));
                ktoraStrona++;
                indeksy.clear();
            }
        }
        if (indeksy.size() > 0) {
            dopelnijListe(indeksy);
            appendEntry(new Strona(ktoraStrona, SBDUtils.listaNaBajty(indeksy)));
        }
        stronPrimary = dane.getStronPrimary() / rozmiarStrony == 0 ? 1 : dane.getStronPrimary() / rozmiarStrony;
    }

    public List<Integer> dopelnijListe(List<Integer> lista) {
        while (lista.size() < rozmiarStrony * 2) {
            lista.add(-1);
        }
        return lista;
    }
}

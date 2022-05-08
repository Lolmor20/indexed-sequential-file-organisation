package pg.eti;

import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlikZDanymi extends Plik {
    int rekordowPrimary = 0;
    int rekordowOverflow = 0;
    int rekordowUsunietych = 0;
    double alfa = 0.5;

    public PlikZDanymi(String fileName)
            throws IOException {
        super(fileName);
    }

    public void odczytajRekord(int klucz, PlikZIndeksem indeks)
            throws IOException, ClassNotFoundException {
        int nrStrony = ktoraStrona(klucz, indeks);
        Strona strona = readEntry(nrStrony);
        Pair<Integer, Integer> wskaznikPoprzedniego = new Pair<>(-1, -1);
        List<Rekord> rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
        for (Rekord rekord : rekordy
        ) {
            if (rekord.getKlucz() == klucz) {
                System.out.println(rekord);
                return;
            } else if (rekord.getKlucz() > klucz) {
                if (wskaznikPoprzedniego.getKey() == -1) {
                    System.out.println("Rekord o podanym kluczu nie istnieje");
                    return;
                } else {
                    Strona stronaOverflow = readEntry(wskaznikPoprzedniego.getKey());
                    List<Rekord> rekordyOverflow = SBDUtils.bajtyNaListe(stronaOverflow.getRekordy());
                    Rekord r;
                    while (true) {
                        if (wskaznikPoprzedniego.getKey() != stronaOverflow.getNumerStrony()) {
                            stronaOverflow = readEntry(wskaznikPoprzedniego.getKey());
                            rekordyOverflow = SBDUtils.bajtyNaListe(stronaOverflow.getRekordy());
                        }
                        r = rekordyOverflow.get(wskaznikPoprzedniego.getValue());
                        if (r.getKlucz() == klucz) {
                            System.out.println(r);
                            return;
                        } else if (r.getWskaznikStrona() != -1) {
                            wskaznikPoprzedniego = r.getStronaPozycja();
                        } else {
                            System.out.println("Rekord o podanym kluczu nie istnieje");
                            return;
                        }
                    }
                }
            }
            wskaznikPoprzedniego = rekord.getStronaPozycja();
        }
        System.out.println("Rekord o podanym kluczu nie istnieje");
    }

    public void wyswietl()
            throws IOException, ClassNotFoundException {
        System.out.println("Primary Area:\n");
        int i;
        Strona strona;
        for (i = 0; i < stronPrimary; i++) {
            strona = readEntry(i);
            System.out.println(strona);
        }
        System.out.println("Overflow Area:\n");
        for (; i < stronPrimary + stronOverflow; i++) {
            strona = readEntry(i);
            System.out.println(strona);
        }
    }

    public void dodajRekord(Rekord rekord, PlikZIndeksem indeks)
            throws IOException, ClassNotFoundException {
        if (rekordowOverflow == stronOverflow * rozmiarStrony) {
            reorganizuj(indeks);
        }

        PlikZDanymi daneTmp = new PlikZDanymi("daneTmp");
        int i, ktoraStrona = ktoraStrona(rekord.getKlucz(), indeks);
        for (i = 0; i < ktoraStrona; i++) {
            daneTmp.appendEntry(readEntry(i));
        }
        Strona strona = readEntry(i);
        List<Rekord> rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
        boolean czyAktualizacjaIndeksu = false;
        // na stronie sa wolne miejsca
        if (rekordy.get(rekordy.size() - 1).getKlucz() == -1) {
            for (int j = 0; j < rekordy.size(); j++) {
                // rekord o danym kluczu istnieje
                if (rekordy.get(j).getKlucz() == rekord.getKlucz()) {
                    if (rekordy.get(j).czyUsuniety()) {
                        rekord.setWskaznik(rekordy.get(j).getStronaPozycja());
                        rekordy.get(j).setKlucz(-1);
                    } else {
                        System.out.println("Rekord z takim kluczem juz istnieje");
                        daneTmp.close();
                        daneTmp.usun();
                        return;
                    }
                }
                if (rekordy.get(j).getKlucz() == -1) {
                    int kluczPierwszego = rekordy.get(0).getKlucz();
                    rekordy.set(j, rekord);
                    rekordy.sort(Rekord::compareTo);
                    rekordowPrimary++;
                    if (kluczPierwszego != rekordy.get(0).getKlucz()) {
                        czyAktualizacjaIndeksu = true;
                    }
                    strona.setRekordy(SBDUtils.listaNaBajty(rekordy));
                    daneTmp.appendEntry(strona);
                    // przepisanie reszty pliku
                    while (true) {
                        i++;
                        try {
                            daneTmp.appendEntry(readEntry(i));
                        } catch (IndexOutOfBoundsException e) {
                            break;
                        }
                    }
                    // nowy plik danych
                    zamienPliki(daneTmp);
                    break;
                }
            }
            if (czyAktualizacjaIndeksu) {
                indeks.stworzIndeks(this);
            }
        }
        // rekord dodawany do overflow
        else {
            int j;
            for (j = 0; j < rekordy.size(); j++) {
                if (rekordy.get(j).getKlucz() == rekord.getKlucz()) {
                    if (rekordy.get(j).czyUsuniety()) {
                        rekord.setWskaznik(rekordy.get(j).getStronaPozycja());
                        rekordy.get(j).setKlucz(-1);
                    } else {
                        System.out.println("Rekord z takim kluczem juz istnieje");
                        daneTmp.close();
                        daneTmp.usun();
                        return;
                    }
                } else if (rekordy.get(j).getKlucz() > rekord.getKlucz()) {
                    break;
                }
            }
            j--;
            if (j < 0) {
                rekordy.add(rekord);
                rekordy.sort(Rekord::compareTo);
                rekord = rekordy.remove(rekordy.size() - 1);
                j = rekordy.size() - 2;
            }
            Pair<Integer, Integer> wskaznikPoprzedniego = rekordy.get(j).getStronaPozycja();
            Pair<Integer, Integer> stronaPozycja = ktoraStronaKtoraPozycja();
            Pair<Integer, Integer> wskaznikNaPoprzedni = new Pair<>(-1, -1);
            if (wskaznikPoprzedniego.getKey() == -1 || getRekord(wskaznikPoprzedniego).getKlucz() > rekord.getKlucz()) {
                rekord.setWskaznik(rekordy.get(j).getStronaPozycja());
                rekordy.get(j).setWskaznik(stronaPozycja);
                wskaznikPoprzedniego = new Pair<>(-1, -1);
            } else {
                Strona s = readEntry(wskaznikPoprzedniego.getKey());
                Rekord r;
                List<Rekord> rekordList = SBDUtils.bajtyNaListe(s.getRekordy());
                while (true) {
                    if (!wskaznikPoprzedniego.getKey().equals(wskaznikNaPoprzedni.getKey())) {
                        s = readEntry(wskaznikPoprzedniego.getKey());
                        rekordList = SBDUtils.bajtyNaListe(s.getRekordy());
                    }
                    r = rekordList.get(wskaznikPoprzedniego.getValue());
                    if (r.getKlucz() == rekord.getKlucz()) {
                        if (rekordy.get(j).czyUsuniety()) {
                            rekord.setWskaznik(rekordy.get(j).getStronaPozycja());
                            rekordy.get(j).setKlucz(-1);
                        } else {
                            System.out.println("Rekord z takim kluczem juz istnieje");
                            daneTmp.close();
                            daneTmp.usun();
                            return;
                        }
                    } else if (r.getKlucz() > rekord.getKlucz()) {
                        rekord.setWskaznik(wskaznikPoprzedniego);
                        wskaznikPoprzedniego = wskaznikNaPoprzedni;
                        break;
                    } else if (r.getWskaznikStrona() == -1) {
                        break;
                    } else {
                        wskaznikNaPoprzedni = wskaznikPoprzedniego;
                        wskaznikPoprzedniego = r.getStronaPozycja();
                    }
                }
            }
            strona.setRekordy(SBDUtils.listaNaBajty(rekordy));
            daneTmp.appendEntry(strona);
            for (i++; i < stronPrimary; i++) {
                daneTmp.appendEntry(readEntry(i));
            }

            while (true) {
                try {
                    strona = readEntry(i);
                    if (i == stronaPozycja.getKey()) {
                        rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
                        rekordy.set(stronaPozycja.getValue(), rekord);
                        strona.setRekordy(SBDUtils.listaNaBajty(rekordy));
                    }
                    if (i == wskaznikPoprzedniego.getKey()) {
                        rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
                        rekordy.get(wskaznikPoprzedniego.getValue()).setWskaznik(stronaPozycja);
                        strona.setRekordy(SBDUtils.listaNaBajty(rekordy));
                    }
                    daneTmp.appendEntry(strona);
                    i++;
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }
            rekordowOverflow++;
            zamienPliki(daneTmp);
        }
    }

    public void zamienPliki(PlikZDanymi daneTmp)
            throws IOException {
        close();
        usun();
        liczbaZapisow += daneTmp.getLiczbaZapisow();
        liczbaOdczytow += daneTmp.getLiczbaOdczytow();
        daneTmp.close();
        File tmp = new File(daneTmp.getFileName() + Plik.DATA_EXT);
        tmp.renameTo(new File(getFileName() + Plik.DATA_EXT));
        tmp = new File(daneTmp.getFileName() + Plik.INDEX_EXT);
        tmp.renameTo(new File(getFileName() + Plik.INDEX_EXT));
        fci = new RandomAccessFile(fileName + Plik.INDEX_EXT, "rw").getChannel();
        fci.force(true);
        fcd = new RandomAccessFile(fileName + Plik.DATA_EXT, "rw").getChannel();
        fcd.force(true);
    }

    public Pair<Integer, Integer> ktoraStronaKtoraPozycja()
            throws IOException, ClassNotFoundException {
        for (int i = stronPrimary; i < stronPrimary + stronOverflow; i++) {
            Strona s = readEntry(i);
            List<Rekord> rekordy = SBDUtils.bajtyNaListe(s.getRekordy());
            for (int j = 0; j < rekordy.size(); j++) {
                if (rekordy.get(j).getKlucz() == -1) {
                    return new Pair<>(i, j);
                }
            }
        }
        return new Pair<>(-1, -1);
    }

    public boolean usun(int klucz, PlikZIndeksem indeks)
            throws IOException, ClassNotFoundException {
        if ((double) rekordowUsunietych / (double) (rekordowPrimary + rekordowOverflow) > 0.1) {
            reorganizuj(indeks);
        }
        PlikZDanymi daneTmp = new PlikZDanymi("daneTmp");
        int i, ktoraStrona = ktoraStrona(klucz, indeks);
        for (i = 0; i < ktoraStrona; i++) {
            daneTmp.appendEntry(readEntry(i));
        }
        Strona strona = readEntry(i);
        List<Rekord> rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
        boolean czyRekordUsuniety = false;
        for (int j = 0; j < rekordy.size(); j++) {
            if (rekordy.get(j).getKlucz() == klucz) {
                rekordy.get(j).setUsuniety(true);
                czyRekordUsuniety = true;
                rekordowPrimary--;
                rekordowUsunietych++;
                strona.setRekordy(SBDUtils.listaNaBajty(rekordy));
                daneTmp.appendEntry(strona);
                while (true) {
                    i++;
                    try {
                        daneTmp.appendEntry(readEntry(i));
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
                break;
            } else if (rekordy.get(j).getKlucz() > klucz) {
                Pair<Integer, Integer> wskaznikPoprzedniego = rekordy.get(j).getStronaPozycja();
                if (wskaznikPoprzedniego.getKey() == -1) {
                    System.out.println("Rekord z takim kluczem nie istnieje");
                    daneTmp.close();
                    daneTmp.usun();
                    return false;
                }
                Strona s = readEntry(wskaznikPoprzedniego.getKey());
                Rekord r;
                List<Rekord> rekordList = SBDUtils.bajtyNaListe(s.getRekordy());
                int poprzedniNrStrony = wskaznikPoprzedniego.getKey();
                while (true) {
                    if (wskaznikPoprzedniego.getKey() == -1) {
                        System.out.println("Rekord z takim kluczem nie istnieje");
                        daneTmp.close();
                        daneTmp.usun();
                        return false;
                    }
                    if (wskaznikPoprzedniego.getKey() != poprzedniNrStrony) {
                        s = readEntry(wskaznikPoprzedniego.getKey());
                        rekordList = SBDUtils.bajtyNaListe(s.getRekordy());
                    }
                    r = rekordList.get(wskaznikPoprzedniego.getValue());
                    if (r.getKlucz() == klucz) {
                        break;
                    } else {
                        wskaznikPoprzedniego = r.getStronaPozycja();
                    }
                    poprzedniNrStrony = r.getWskaznikStrona();
                }
                daneTmp.appendEntry(strona);
                for (i++; i < stronPrimary; i++) {
                    daneTmp.appendEntry(readEntry(i));
                }
                for (; i < stronPrimary + stronOverflow; i++) {
                    strona = readEntry(i);
                    rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
                    if (i == wskaznikPoprzedniego.getKey()) {
                        rekordy.get(wskaznikPoprzedniego.getValue()).setUsuniety(true);
                        rekordowOverflow--;
                        rekordowUsunietych++;
                        czyRekordUsuniety = true;
                        strona.setRekordy(SBDUtils.listaNaBajty(rekordy));
                    }
                    daneTmp.appendEntry(strona);
                }
            }
        }
        for (; i < stronPrimary + stronOverflow; i++) {
            daneTmp.appendEntry(readEntry(i));
        }
        zamienPliki(daneTmp);

        return czyRekordUsuniety;
    }

    public void aktualizuj(PlikZIndeksem indeks, int kluczStaregoRekordu, Rekord nowyRekord)
            throws IOException, ClassNotFoundException {
        if (usun(kluczStaregoRekordu, indeks)) {
            dodajRekord(nowyRekord, indeks);
        } else {
            System.out.println("Rekord z podanym kluczem nie istnieje");
        }
    }

    public void odczytajWszystkieRekordy()
            throws IOException, ClassNotFoundException {
        for (int i = 0; i < stronPrimary; i++) {
            Strona strona = readEntry(i);
            List<Rekord> rekordy = SBDUtils.bajtyNaListe(strona.getRekordy());
            for (Rekord rekord : rekordy
            ) {
                if (rekord.getKlucz() != -1) {
                    System.out.println(rekord);
                }
                if (rekord.getWskaznikStrona() != -1) {
                    Pair<Integer, Integer> wskaznikPoprzedniego = rekord.getStronaPozycja();
                    Rekord r;
                    Strona stronaOverflow = readEntry(wskaznikPoprzedniego.getKey());
                    List<Rekord> rekordyOverflow = SBDUtils.bajtyNaListe(stronaOverflow.getRekordy());
                    while (true) {
                        if (wskaznikPoprzedniego.getKey() != stronaOverflow.getNumerStrony()) {
                            stronaOverflow = readEntry(wskaznikPoprzedniego.getKey());
                            rekordyOverflow = SBDUtils.bajtyNaListe(stronaOverflow.getRekordy());
                        }
                        r = rekordyOverflow.get(wskaznikPoprzedniego.getValue());
                        System.out.println(rekord);
                        if (rekord.getWskaznikStrona() == -1) {
                            break;
                        } else {
                            wskaznikPoprzedniego = r.getStronaPozycja();
                        }
                    }
                }
            }
        }
    }

    public void reorganizuj(PlikZIndeksem indeks)
            throws IOException, ClassNotFoundException {
        PlikZDanymi daneTmp = new PlikZDanymi("daneTmp");
        Strona stronaOverflow, stronaPrimary = new Strona(-1, null);
        List<Rekord> rekordyOdczytane, rekordyOverflow, rekordyDoZapisu = new ArrayList<>();
        int nowychStron = 0;
        for (int i = 0; i < stronPrimary; i++) {
            stronaPrimary = readEntry(i);
            rekordyOdczytane = SBDUtils.bajtyNaListe(stronaPrimary.getRekordy());
            for (Rekord rekord : rekordyOdczytane
            ) {
                if (rekordyDoZapisu.size() >= rozmiarStrony * alfa) {
                    stronaPrimary.setNumerStrony(nowychStron);
                    rekordyDoZapisu.forEach(r -> r.setWskaznik(new Pair<>(-1, -1)));
                    rekordyDoZapisu = dopelnijListe(rekordyDoZapisu);
                    stronaPrimary.setRekordy(SBDUtils.listaNaBajty(rekordyDoZapisu));
                    daneTmp.appendEntry(stronaPrimary);
                    rekordyDoZapisu.clear();
                    nowychStron++;
                }
                if (rekord.getKlucz() != -1 && !rekord.czyUsuniety()) {
                    rekordyDoZapisu.add(rekord);
                }
                if (rekord.getWskaznikStrona() != -1) {
                    Pair<Integer, Integer> wskaznikPoprzedniego = rekord.getStronaPozycja();
                    Rekord r;
                    stronaOverflow = readEntry(wskaznikPoprzedniego.getKey());
                    rekordyOverflow = SBDUtils.bajtyNaListe(stronaOverflow.getRekordy());
                    if (rekordyDoZapisu.size() >= rozmiarStrony * alfa) {
                        stronaPrimary.setNumerStrony(nowychStron);
                        rekordyDoZapisu.forEach(r1 -> r1.setWskaznik(new Pair<>(-1, -1)));
                        rekordyDoZapisu = dopelnijListe(rekordyDoZapisu);
                        stronaPrimary.setRekordy(SBDUtils.listaNaBajty(rekordyDoZapisu));
                        daneTmp.appendEntry(stronaPrimary);
                        rekordyDoZapisu.clear();
                        nowychStron++;
                    }

                    while (true) {
                        if (wskaznikPoprzedniego.getKey() != stronaOverflow.getNumerStrony()) {
                            stronaOverflow = readEntry(wskaznikPoprzedniego.getKey());
                            rekordyOverflow = SBDUtils.bajtyNaListe(stronaOverflow.getRekordy());
                        }
                        r = rekordyOverflow.get(wskaznikPoprzedniego.getValue());
                        if (!r.czyUsuniety()) {
                            rekordyDoZapisu.add(r);
                        }
                        if (r.getWskaznikStrona() == -1) {
                            break;
                        } else {
                            wskaznikPoprzedniego = r.getStronaPozycja();
                        }
                        if (rekordyDoZapisu.size() >= rozmiarStrony * alfa) {
                            stronaPrimary.setNumerStrony(nowychStron);
                            rekordyDoZapisu.forEach(r1 -> r1.setWskaznik(new Pair<>(-1, -1)));
                            rekordyDoZapisu = dopelnijListe(rekordyDoZapisu);
                            stronaPrimary.setRekordy(SBDUtils.listaNaBajty(rekordyDoZapisu));
                            daneTmp.appendEntry(stronaPrimary);
                            rekordyDoZapisu.clear();
                            nowychStron++;
                        }
                    }
                }
            }
        }
        if (rekordyDoZapisu.size() > 0) {
            stronaPrimary.setNumerStrony(nowychStron);
            rekordyDoZapisu.forEach(r -> r.setWskaznik(new Pair<>(-1, -1)));
            rekordyDoZapisu = dopelnijListe(rekordyDoZapisu);
            stronaPrimary.setRekordy(SBDUtils.listaNaBajty(rekordyDoZapisu));
            daneTmp.appendEntry(stronaPrimary);
            rekordyDoZapisu.clear();
            nowychStron++;
        }

        stronPrimary = nowychStron;
        stronOverflow = stronPrimary / 4 == 0 ? 1 : stronPrimary / 4;
        rekordowUsunietych = 0;

        rekordyDoZapisu = dopelnijListe(rekordyDoZapisu);
        for (int i = stronPrimary; i < stronPrimary + stronOverflow; i++) {
            daneTmp.appendEntry(new Strona(i, SBDUtils.listaNaBajty(rekordyDoZapisu)));
        }

        rekordowPrimary += rekordowOverflow;
        rekordowOverflow = 0;
        zamienPliki(daneTmp);

        indeks.stworzIndeks(this);
    }

    public int ktoraStrona(int klucz, PlikZIndeksem indeks)
            throws IOException, ClassNotFoundException {
        int j, ostatniKlucz = 0, ostatniNr = 0;
        List<Integer> indeksy;
        for (int i = 0; i < indeks.getStronPrimary(); i++) {
            Strona strona = indeks.readEntry(i);
            indeksy = SBDUtils.bajtyNaListe(strona.getRekordy());
            for (j = 0; j < indeksy.size(); j += 2) {
                if (indeksy.get(j + 1) != -1) {
                    ostatniKlucz = indeksy.get(j);
                    ostatniNr = indeksy.get(j + 1);
                }
                if (indeksy.get(j) > klucz) {
                    if (indeksy.get(j + 1) == 0) {
                        return 0;
                    } else {
                        return indeksy.get(j + 1) - 1;
                    }
                }
            }
        }
        if (klucz >= ostatniKlucz) {
            return ostatniNr;
        }
        return 0;
    }

    public Rekord getRekordOverflow(int wskaznik)
            throws IOException, ClassNotFoundException {
        Strona s;
        List<Rekord> rekordy;
        for (int i = stronPrimary; i < stronPrimary + stronOverflow; i++) {
            try {
                s = readEntry(i);
                rekordy = SBDUtils.bajtyNaListe(s.getRekordy());
                for (Rekord r : rekordy
                ) {
                    if (r.getKlucz() == wskaznik)
                        return r;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        return null;
    }

    public Rekord getRekord(Pair<Integer, Integer> wskaznik)
            throws IOException, ClassNotFoundException {
        List<Rekord> rekordy = SBDUtils.bajtyNaListe(readEntry(wskaznik.getKey()).getRekordy());
        return rekordy.get(wskaznik.getValue());
    }

    public List<Rekord> dopelnijListe(List<Rekord> lista) {
        while (lista.size() < rozmiarStrony) {
            lista.add(new Rekord());
        }
        return lista;
    }
}

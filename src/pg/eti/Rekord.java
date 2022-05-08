package pg.eti;

import javafx.util.Pair;

import java.io.*;

public class Rekord implements Serializable, Comparable<Rekord> {
    private Pieciokat pieciokat;
    private int klucz = -1;
    private int wskaznikStrona = -1;
    private int wskaznikPozycja = -1;
    private boolean usuniety = false;
    private static final long serialVersionUID = 6529685098267757690L;

    public Rekord(Pieciokat pieciokat) {
        this.pieciokat = pieciokat;
    }

    public Rekord(Pieciokat pieciokat, int klucz) {
        this.klucz = klucz;
        this.pieciokat = pieciokat;
    }

    public Rekord() {
        this.pieciokat = new Pieciokat(-1, -1, -1, -1, -1);
    }

    public Pieciokat getPieciokat() {
        return pieciokat;
    }

    public void setPieciokat(Pieciokat pieciokat) {
        this.pieciokat = pieciokat;
    }

    public void setWskaznik(Pair<Integer, Integer> stronaPozycja) {
        this.wskaznikStrona = stronaPozycja.getKey();
        this.wskaznikPozycja = stronaPozycja.getValue();
    }

    public void setWskaznik(int strona, int pozycja) {
        this.wskaznikStrona = strona;
        this.wskaznikPozycja = pozycja;
    }

    public int getWskaznikStrona() {
        return wskaznikStrona;
    }

    public int getWskaznikPozycja() {
        return wskaznikPozycja;
    }

    public boolean czyUsuniety() {
        return usuniety;
    }

    public void setUsuniety(boolean usuniety) {
        this.usuniety = usuniety;
    }

    public Pair<Integer, Integer> getStronaPozycja() {
        return new Pair<>(wskaznikStrona, wskaznikPozycja);
    }

    public int getKlucz() {
        return klucz;
    }

    public void setKlucz(int klucz) {
        this.klucz = klucz;
    }

    @Override
    public String toString() {
        return "Klucz: " + klucz + " " + pieciokat.toString() + " Strona: "
                + wskaznikStrona + " Pozycja: " + wskaznikPozycja + (czyUsuniety() ? " Usuniety" : "");
    }

    @Override
    public int compareTo(Rekord inny) {
        if (klucz != -1 && inny.getKlucz() != -1) {
            return Integer.compare(klucz, inny.getKlucz());
        } else if (klucz == -1) {
            return 1;
        } else if (inny.getKlucz() == -1) {
            return -1;
        } else {
            return 0;
        }
    }

}
package pg.eti;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Pieciokat implements Serializable {
    private int bok1;
    private int bok2;
    private int bok3;
    private int bok4;
    private int bok5;

    public Pieciokat(int bok1, int bok2, int bok3, int bok4, int bok5) {
        this.bok1 = bok1;
        this.bok2 = bok2;
        this.bok3 = bok3;
        this.bok4 = bok4;
        this.bok5 = bok5;
    }

    public Pieciokat(List<Integer> boki) {
        this.bok1 = boki.get(0);
        this.bok2 = boki.get(1);
        this.bok3 = boki.get(2);
        this.bok4 = boki.get(3);
        this.bok5 = boki.get(4);
    }

    public long getObwod(){
        List<Long> lista = Stream.of(bok1, bok2, bok3, bok4, bok5).map(Integer::longValue).collect(Collectors.toList());
        return lista.stream().mapToLong(Long::longValue).sum();
    }

    public int getBok1() {
        return bok1;
    }

    public void setBok1(int bok1) {
        this.bok1 = bok1;
    }

    public int getBok2() {
        return bok2;
    }

    public void setBok2(int bok2) {
        this.bok2 = bok2;
    }

    public int getBok3() {
        return bok3;
    }

    public void setBok3(int bok3) {
        this.bok3 = bok3;
    }

    public int getBok4() {
        return bok4;
    }

    public void setBok4(int bok4) {
        this.bok4 = bok4;
    }

    public int getBok5() {
        return bok5;
    }

    public void setBok5(int bok5) {
        this.bok5 = bok5;
    }

    public List<Integer> getBoki(){
        return Arrays.asList(getBok1(), getBok2(), getBok3(), getBok4(), getBok5());
    }

    @Override
    public String toString(){
        return "Boki: " + bok1 + " " + bok2 + " " + bok3 + " " + bok4 + " " + bok5 + " Obwod = " + getObwod();
    }

}

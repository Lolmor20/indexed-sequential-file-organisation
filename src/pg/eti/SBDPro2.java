package pg.eti;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SBDPro2 {

    public static void main(String[] args) {

        PlikZDanymi dane;
        PlikZIndeksem indeks;
        try {
            dane = new PlikZDanymi("dane");
            indeks = new PlikZIndeksem("indeks");
            SBDUtils.wypelnijPustymiRekordami(dane);
            indeks.stworzIndeks(dane);
            //indeks.wyswietlIndeks();
            //dane.dodajRekord(new Rekord(new Pieciokat(1, 2, 3, 4, 5), -239), indeks);
            dane.dodajRekord(new Rekord(new Pieciokat(-1, -1, -1, -1, -1), Integer.MIN_VALUE), indeks);
            //dane.dodajRekord(new Rekord(new Pieciokat(1, 2, 3, 4, 5), 256), indeks);
            //dane.dodajRekord(new Rekord(new Pieciokat(1, 2, 3, 4, 5), 77), indeks);
            //List<Rekord> rekordy = new ArrayList<>();
//            for (int i = 0; i < 19; i++) {
//                dane.dodajRekord(new Rekord(new Pieciokat(1, 2, 3, 4, 5), i + 10), indeks);
//            }
            //dane.aktualizuj(indeks, 77, new Rekord(new Pieciokat(1, 2, 3, 4, 5), 99));
            //indeks.wyswietlIndeks();
            //dane.odczytajWszystkieRekordy();
            //System.out.println("Zapisow: " + dane.getLiczbaZapisow() + " Odczytow: " + dane.getLiczbaOdczytow());

            while (true) {
                System.out.println("Wybierz:\n" +
                        "1) odczytaj rekord\n" +
                        "2) dodaj rekord\n" +
                        "3) usuń rekord\n" +
                        "4) aktualizuj rekord\n" +
                        "5) reorganizuj plik\n" +
                        "6) przejrzyj zawartość pliku\n" +
                        "7) przejrzyj zawartość indeksu\n" +
                        "8) odczytaj wszystkie rekordy\n"
                );
                Scanner scanner = new Scanner(System.in);

                int tryb = scanner.nextInt();
                switch (tryb) {
                    case 1:
                        System.out.println("Podaj klucz rekordu do odczytania");
                        dane.odczytajRekord(scanner.nextInt(), indeks);
                        break;
                    case 2:
                        ArrayList<Integer> boki = new ArrayList<>();
                        for (int j = 0; j < 5; j++) {
                            System.out.println("Podaj bok " + (j + 1));
                            boki.add(scanner.nextInt());
                        }
                        System.out.println("Podaj klucz");
                        dane.dodajRekord(new Rekord(new Pieciokat(boki), scanner.nextInt()), indeks);
                        break;
                    case 3:
                        System.out.println("Podaj klucz rekordu do usunięcia");
                        dane.usun(scanner.nextInt(), indeks);
                        break;
                    case 4:
                        System.out.println("Podaj klucz rekordu do aktualizacji");
                        int klucz = scanner.nextInt();
                        boki = new ArrayList<>();
                        System.out.println("Nowy rekord");
                        for (int j = 0; j < 5; j++) {
                            System.out.println("Podaj bok " + (j + 1));
                            boki.add(scanner.nextInt());
                        }
                        System.out.println("Podaj klucz");
                        dane.aktualizuj(indeks, klucz, new Rekord(new Pieciokat(boki), scanner.nextInt()));
                        break;
                    case 5:
                        dane.reorganizuj(indeks);
                        break;
                    case 6:
                        dane.wyswietl();
                        break;
                    case 7:
                        indeks.wyswietlIndeks();
                        break;
                    case 8:
                        dane.odczytajWszystkieRekordy();
                        break;
                    default:
                        System.out.println("Nieprawidlowe wejscie");
                }
            }

//            Scanner scanner = new Scanner(System.in);
//
//            File plik = new File("test.txt");
//            //SBDUtils.wypelnijPlikTestowy(plik);
//            scanner = new Scanner(plik);
//            scanner.useDelimiter(" |\n");
//            int i = 1;
//            dane.dodajRekord(new Rekord(new Pieciokat(-1, -1, -1, -1, -1), Integer.MIN_VALUE), indeks);
//            while (scanner.hasNext()) {
//                System.out.println(i);
//                switch (scanner.next()) {
//                    case "D":
//                        ArrayList<Integer> boki = new ArrayList<>();
//                        for (int j = 0; j < 5; j++) {
//                            boki.add(scanner.nextInt());
//                        }
//                        dane.dodajRekord(new Rekord(new Pieciokat(boki), scanner.nextInt()), indeks);
//                        break;
//                    case "U":
//                        dane.usun(scanner.nextInt(), indeks);
//                        break;
//                    case "A":
//                        int klucz = scanner.nextInt();
//                        boki = new ArrayList<>();
//                        for (int j = 0; j < 5; j++) {
//                            boki.add(scanner.nextInt());
//                        }
//                        dane.aktualizuj(indeks, klucz, new Rekord(new Pieciokat(boki), scanner.nextInt()));
//                        break;
//                    case "O":
//                        dane.odczytajRekord(scanner.nextInt(), indeks);
//                        break;
//                    case "R":
//                        dane.reorganizuj(indeks);
//                        break;
//                    case "W":
//                        dane.wyswietl();
//                        break;
//                    case "I":
//                        indeks.wyswietlIndeks();
//                        break;
//                    case "Q":
//                        dane.odczytajWszystkieRekordy();
//                        break;
//                    default:
//                        System.out.println("Nieprawidlowe wejscie");
//                }
//                i++;
//            }
            //System.out.println("Liczba zapisow: " + dane.getLiczbaZapisow() + "Liczba odczytow: " + dane.getLiczbaOdczytow());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Tworzenie plikow nie powiodlo sie");
        }
    }
}
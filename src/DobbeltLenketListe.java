import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DobbeltLenketListe<T> implements Liste<T> {
    private static final class Node<T> {  // en indre nodeklasse
        // instansvariabler
        private T verdi;
        private Node<T> forrige, neste;

        private Node(T verdi, Node<T> forrige, Node<T> neste)  // konstruktør
        {
            this.verdi = verdi;
            this.forrige = forrige;
            this.neste = neste;
        }

        protected Node(T t, T verdi)  // konstruktør
        {
            this(verdi, null, null);
        }
    } // Node

    // instansvariabler
    private Node<T> hode;          // peker til den første i listen
    private Node<T> hale;          // peker til den siste i listen
    private int antall;            // antall noder i listen
    private int endringer;   // antall endringer i listen

    // hjelpemetode
    private Node<T> finnNode(int indeks)
    {
         if(indeks < (antall/2)) {
             Node<T> p = hode;
             for(int i = 0; i < indeks; i++) p = p.neste;
                 return p;
         } else {
             Node<T> p = hale;
             for (int i = antall-1; i > indeks; i--) p = p.forrige;
             return p;
         }
    }

    // konstruktør
    public DobbeltLenketListe()
    {
        hode = hale = null;
        antall = 0;
        endringer = 0;
    }

    // konstruktør

    public DobbeltLenketListe(T[]a)
        {
            this();

            Objects.requireNonNull(a, "Tabellen a er null!");

            if (a.length == 0) return;
            Node<T> temp = hode;
            for (int i = 0; i < a.length; i++) {
                if (a[i] == null)
                    continue;
                Node<T> newNode = new Node<>(a[i], null, null);
                if (antall == 0) {
                    hode = hale = newNode;
                } else {
                    newNode.forrige = temp;
                    hale.neste = newNode;
                    hale = newNode;
                }
                temp = newNode;
                antall++;
            }
        }
    // fratilKontroll fra kompendiet
    private static void fratilKontroll(int antall, int fra, int til) {
        if (fra < 0)
            throw new IndexOutOfBoundsException
                    ("fra(" + fra + ") er negativ !");
        if (til > antall)
            throw new IndexOutOfBoundsException
                    ("til(" + til + ") > antall(" + antall + ")");
        if (fra > til)
            throw new IllegalArgumentException
                    ("fra(" + fra + ") > til(" + til + ") - ulovlig intervall!");
    }

    // subliste
    public Liste<T> subliste(int fra, int til)
    {
        fratilKontroll(antall, fra, til);
        Liste<T> tmpListe = new DobbeltLenketListe<>();
        for(int i = fra; i < til; i++) {
            tmpListe.leggInn(finnNode(i).verdi);
        }
        return tmpListe;
    }

    @Override
    public int antall()
    {
        return antall;
    }

    @Override
    public boolean tom()
    {
        return antall == 0;
    }

    @Override
    public boolean leggInn(T verdi)
    {

        Objects.requireNonNull(verdi, "Ikke tillat med null-verdier!");

        if(tom()) {
            hode = hale = new Node<>(verdi, null, null);
        } else {
            hale = hale.neste = new Node<>(verdi, hale, null);
        }
        antall++;
        endringer++;
        return true;
    }

    @Override
    public void leggInn(int indeks, T verdi)
    {
        Objects.requireNonNull(verdi, "Ikke tillatt med null-verdier");

        indeksKontroll(indeks, true);

        if(tom()) {
            hode = hale = new Node<>(verdi, null, null);
        } else if (indeks == 0) {
            hode = hode.forrige = new Node<>(verdi, null, hode);
        } else if(indeks == antall) {
            hale = hale.neste = new Node<>(verdi, hale, null);
        } else {
            Node<T> p = hode;

            for(int i = 0; i < indeks; i++) p = p.neste;

            p = new Node<>(verdi, p, p.neste);
            p.neste.forrige = p;
            p.forrige.neste = p;
        }
        antall++;
        endringer++;
    }

    @Override
    public boolean inneholder(T verdi)
    {
        return indeksTil(verdi) != -1;
    }

    @Override
    public T hent(int indeks)
    {
        indeksKontroll(indeks, false);
        return finnNode(indeks).verdi;
    }

    @Override
    public int indeksTil(T verdi)
    {
        if (verdi == null) return -1;

        Node<T> p = hode;

        for (int indeks = 0; indeks < antall ; indeks++)
        {
            if (p.verdi.equals(verdi)) return indeks;
            p = p.neste;
        }
        return -1;
    }

    @Override
    public T oppdater(int indeks, T nyverdi)
    {
        Objects.requireNonNull(nyverdi, "Ikke tillatt med null-verdier!");
        indeksKontroll(indeks, false);
        Node<T> p = finnNode(indeks);
        T gammelVerdi = p.verdi;
        p.verdi = nyverdi;
        endringer++;
        return gammelVerdi;
    }

    @Override
    public boolean fjern(T verdi) {
        if (verdi == null) {
            return false;
        }

        Node<T> l = hode;

        while (l != null) {
            if(l.verdi.equals(verdi)) break;
                l = l.neste;
        }

        if (l == null) {
            return false;
        } else if (l == hode) {
            hode = hode.neste;
            if(antall > 1) {
                hode.forrige = null;
            }
        } else if (l == hale) {
            hale = hale.forrige;
            hale.neste = null;
        } else {
            l.forrige.neste = l.neste;
            l.neste.forrige = l.forrige;
        }

        l.verdi = null;
        antall--;
        endringer++;
        return true;
    }

    @Override
    public T fjern(int indeks)
    {
        indeksKontroll(indeks, false);

        T temp;

        if (indeks == 0) {
            temp = hode.verdi;
            if(antall > 1) {
                hode = hode.neste;
                hode.forrige = null;
            } else {
                hale = null;
                hode.neste = null;
            }
        } else if(indeks == antall - 1) {

            temp = hale.verdi;
            Node<T> tempHale = hale;
            hale = hale.forrige;
            tempHale.forrige = null;
            hale.neste = null;
        } else {
            Node<T> k = finnNode(indeks);
            temp = k.verdi;

            k.forrige.neste = k.neste;
            k.neste.forrige = k.forrige;
        }

        antall--;
        endringer++;
        return temp;
    }

    @Override
    public void nullstill()
    {
        Node<T> l = hode, k;

        while (l != null) {
            k = l.neste;
            l.neste = null;
            l.verdi = null;
            l.forrige = null;
            l = k;
        }

        hode = null;
        hale = hode;
        endringer++;
        antall = 0;

    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();

        s.append('[');

        if (!tom()) {
            Node<T> a = hode;
            s.append(a.verdi);

            a = a.neste;

            while (a != null) {
                s.append(',').append(' ').append(a.verdi);
                a = a.neste;

            }
        }

        s.append(']');

        return s.toString();

    }

    public String omvendtString()
    {
        StringBuilder i = new StringBuilder();

        i.append('[');

        if(!tom()) {
            Node<T> p = hale;
            i.append(p.verdi);

            p = p.forrige;

            while (p != null) {
                i.append(',').append(' ').append(p.verdi);
                p = p.forrige;
            }
        }

        i.append(']');

        return String.valueOf(i);
    }

    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c)
    {
        for (int n = liste.antall(); n > 1; n--) {
            for (int i = 1; i < n; i++) {
                T verdiA = liste.hent(i-1);
                T verdiB = liste.hent(i);
                if(c.compare(verdiA, verdiB) > 0) {
                    liste.oppdater(i-1, verdiB);
                    liste.oppdater(i, verdiA);
                }
            }
        }
    }

    @Override
    public Iterator<T> iterator()
    {
        return new DobbeltLenketListeIterator();
    }

    public Iterator<T> iterator(int indeks)
    {
        indeksKontroll(indeks, false);

        return new DobbeltLenketListeIterator(indeks);
    }

    private class DobbeltLenketListeIterator implements Iterator<T>
    {
        private Node<T> denne;
        private boolean fjernOK;
        private int iteratorendringer;

        private DobbeltLenketListeIterator()
        {
            denne = hode;     // denne starter på den første i listen
            fjernOK = false;  // blir sann når next() kalles
            iteratorendringer = endringer;  // teller endringer
        }

        private DobbeltLenketListeIterator(int indeks)
        {
            denne = finnNode(indeks);
        }

        @Override
        public boolean hasNext()
        {
            return denne != null;  // denne koden skal ikke endres!
        }

        @Override
        public T next()
        {
            if(iteratorendringer != endringer) {
                throw new ConcurrentModificationException("Antall iteratorendringer er ulik antall endringer");
            } else if (!hasNext()) {
                throw new NoSuchElementException("Elementet finnes ikke");
            }

            fjernOK = true;
            T temp = denne.verdi;
            denne = denne.neste;
            return temp;
        }

        @Override
        public void remove()
        {
            if(endringer != iteratorendringer) {
                throw new ConcurrentModificationException("Antall endringer og antall iteratorendringer er ulike");
            } else if (!fjernOK) {
                throw new IllegalStateException("Du har ikke lov å kalle på denne metoden");
            }

            fjernOK = false;

            if(antall == 1) {
                hale = null;
                hode = null;
            } else if (denne == null) {
                Node<T> nyHale = hale;
                hale = hale.forrige;
                hale.neste = null;
                nyHale.forrige = null;
            } else if (denne.forrige == hode) {
                hode = hode.neste;
                hode.forrige = null;
            }  else {
                Node<T> temp = denne.forrige.neste;
                temp.neste = denne;
                denne.forrige = temp;
            }

            antall--;
            endringer++;
            iteratorendringer++;
        }




    } // DobbeltLenketListeIterator



} // DobbeltLenketListe

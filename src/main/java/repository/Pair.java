package repository;

/**
 * Generička klasa koja služi kao spremnik za par vrijednosti različitih tipova.
 * Korisna je za metode koje trebaju vratiti dvije vrijednosti odjednom.
 *
 * @param <T> Tip prve vrijednosti (ključ).
 * @param <R> Tip druge vrijednosti (vrijednost).
 */
public class Pair<T, R> {
    private final T key;
    private final R value;

    /**
     * Konstruktor koji stvara novi par s zadanim vrijednostima.
     * @param key Prva vrijednost.
     * @param value Druga vrijednost.
     */
    public Pair(T key, R value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Dohvaća prvu vrijednost (ključ) iz para.
     * @return Prva vrijednost.
     */
    public T getKey() {
        return key;
    }

    /**
     * Dohvaća drugu vrijednost iz para.
     * @return Druga vrijednost.
     */
    public R getValue() {
        return value;
    }
}
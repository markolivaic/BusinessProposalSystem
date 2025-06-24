package model;

/**
 * Apstraktna bazna klasa za sve entitete u sustavu.
 * Sadrži zajedničko svojstvo 'id' koje jedinstveno identificira svaki entitet.
 * Klasa je zapečaćena (sealed) i dopušta nasljeđivanje samo klasama Client, Proposal i User.
 */
public sealed class Entitiy permits Client, Proposal, User {
    private long id;

    /**
     * Konstruktor za stvaranje entiteta s postojećim ID-jem.
     * @param id Jedinstveni identifikator entiteta.
     */
    protected Entitiy(long id) {
        this.id = id;
    }

    /**
     * Prazan konstruktor za slučajeve kada se ID postavlja naknadno.
     */
    protected Entitiy() {
    }

    /**
     * Dohvaća ID entiteta.
     * @return ID entiteta.
     */
    public long getId() {
        return id;
    }

    /**
     * Postavlja ID entiteta.
     * @param id Novi ID entiteta.
     */
    public void setId(long id) {
        this.id = id;
    }
}
package model;

/**
 * Predstavlja klijenta u sustavu.
 * Nasljeđuje klasu {@link Entitiy} i sadrži podatke specifične za klijenta.
 * Ova klasa je 'non-sealed', što znači da može biti dalje naslijeđena.
 */
public non-sealed class Client extends Entitiy{
    private String name;
    private String email;
    private String phone;
    private String company;

    /**
     * Konstruktor za stvaranje klijenta sa svim podacima, uključujući ID.
     * @param id Jedinstveni identifikator klijenta.
     * @param name Puno ime klijenta.
     * @param email Email adresa klijenta.
     * @param phone Kontakt telefon klijenta.
     * @param company Naziv tvrtke klijenta.
     */
    public Client(long id, String name, String email, String phone, String company) {
        super(id);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    /**
     * Konstruktor za stvaranje novog klijenta bez prethodno definiranog ID-ja.
     * @param name Puno ime klijenta.
     * @param email Email adresa klijenta.
     * @param phone Kontakt telefon klijenta.
     * @param company Naziv tvrtke klijenta.
     */
    public Client(String name, String email, String phone, String company) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    /**
     * Dohvaća ime klijenta.
     * @return Ime klijenta.
     */
    public String getName() {
        return name;
    }

    /**
     * Postavlja ime klijenta.
     * @param name Novo ime klijenta.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Dohvaća email klijenta.
     * @return Email adresa klijenta.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Postavlja email klijenta.
     * @param email Nova email adresa klijenta.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Dohvaća kontakt telefon klijenta.
     * @return Kontakt telefon.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Postavlja kontakt telefon klijenta.
     * @param phone Novi kontakt telefon.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Dohvaća naziv tvrtke klijenta.
     * @return Naziv tvrtke.
     */
    public String getCompany() {
        return company;
    }

    /**
     * Postavlja naziv tvrtke klijenta.
     * @param company Novi naziv tvrtke.
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Vraća string reprezentaciju klijenta, što je njegovo ime.
     * Korisno za prikaz u ComboBox-u i sličnim UI komponentama.
     * @return Ime klijenta.
     */
    @Override
    public String toString() {
        return name;
    }
}
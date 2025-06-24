package model;

import enums.ProposalStatus;

/**
 * Predstavlja poslovni prijedlog u sustavu.
 * Nasljeđuje klasu {@link Entitiy} i implementira sučelje {@link Approver}.
 * Sadrži detalje o prijedlogu, njegov status, te reference na klijenta i korisnika.
 * Koristi Builder pattern za stvaranje instanci.
 */
public non-sealed class Proposal extends Entitiy implements Approver{
    private String title;
    private String description;
    private ProposalStatus status;
    private long clientId;
    private long userId;

    /**
     * Privatni konstruktor koji se poziva putem Buildera.
     * @param id Jedinstveni identifikator prijedloga.
     * @param title Naslov prijedloga.
     * @param description Opis prijedloga.
     * @param status Trenutni status prijedloga (npr. PENDING, APPROVED).
     * @param clientId ID klijenta na kojeg se prijedlog odnosi.
     * @param userId ID korisnika koji je kreirao prijedlog.
     */
    private Proposal(long id, String title, String description, ProposalStatus status, long clientId, long userId) {
        super(id);
        this.title = title;
        this.description = description;
        this.status = status;
        this.clientId = clientId;
        this.userId = userId;
    }

    /**
     * Dohvaća naslov prijedloga.
     * @return Naslov prijedloga.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Postavlja naslov prijedloga.
     * @param title Novi naslov prijedloga.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Dohvaća opis prijedloga.
     * @return Opis prijedloga.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Postavlja opis prijedloga.
     * @param description Novi opis prijedloga.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Dohvaća status prijedloga.
     * @return Status prijedloga.
     */
    public ProposalStatus getStatus() {
        return status;
    }

    /**
     * Postavlja status prijedloga.
     * @param status Novi status prijedloga.
     */
    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    /**
     * Dohvaća ID klijenta povezanog s prijedlogom.
     * @return ID klijenta.
     */
    public long getClientId() {
        return clientId;
    }

    /**
     * Postavlja ID klijenta povezanog s prijedlogom.
     * @param clientId Novi ID klijenta.
     */
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    /**
     * Dohvaća ID korisnika koji je kreirao prijedlog.
     * @return ID korisnika.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Postavlja ID korisnika koji je kreirao prijedlog.
     * @param userId Novi ID korisnika.
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Postavlja status prijedloga na {@link ProposalStatus#APPROVED}.
     */
    @Override
    public void approveProposal() {
        this.status = ProposalStatus.APPROVED;
    }

    /**
     * Postavlja status prijedloga na {@link ProposalStatus#REJECTED}.
     */
    @Override
    public void rejectProposal() {
        this.status = ProposalStatus.REJECTED;
    }

    /**
     * Builder pattern za stvaranje {@link Proposal} objekata.
     */
    public static class Builder{
        private Long id;
        private String title;
        private String description;
        private ProposalStatus status;
        private long clientId;
        private long userId;

        /**
         * Prazan konstruktor za Builder.
         */
        public Builder() {
        }

        /**
         * Konstruktor za Builder koji odmah postavlja ID.
         * @param id Jedinstveni identifikator prijedloga.
         */
        public Builder(Long id) {
            this.id = id;
        }

        /**
         * Postavlja naslov prijedloga.
         * @param title Naslov prijedloga.
         * @return Referenca na ovaj Builder.
         */
        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Postavlja opis prijedloga.
         * @param description Opis prijedloga.
         * @return Referenca na ovaj Builder.
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Postavlja status prijedloga.
         * @param status Status prijedloga.
         * @return Referenca na ovaj Builder.
         */
        public Builder withStatus(ProposalStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Postavlja ID klijenta.
         * @param clientId ID klijenta.
         * @return Referenca na ovaj Builder.
         */
        public Builder withClientId(long clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Postavlja ID korisnika.
         * @param userId ID korisnika.
         * @return Referenca na ovaj Builder.
         */
        public Builder withUserId(long userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Gradi i vraća novi {@link Proposal} objekt.
         * @return Kreirana {@code Proposal} instanca.
         */
        public Proposal build(){
            return new Proposal(id, title, description, status, clientId, userId);
        }
    }

    /**
     * Vraća string reprezentaciju statusa prijedloga.
     * @return Status prijedloga kao String.
     */
    @Override
    public String toString() {
        return "" + status;
    }
}
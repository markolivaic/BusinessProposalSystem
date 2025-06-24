package model;

/**
 * Sučelje koje definira ugovor za entitete koji mogu biti odobreni ili odbačeni.
 * Primarno se koristi za {@link Proposal}.
 */
public interface Approver {
    /**
     * Metoda za odobravanje entiteta.
     */
    void approveProposal();
    /**
     * Metoda za odbacivanje entiteta.
     */
    void rejectProposal();
}
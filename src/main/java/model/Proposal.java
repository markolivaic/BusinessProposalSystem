package model;

import enums.ProposalStatus;

/**
 * Represents a proposal in the system.
 */
public non-sealed class Proposal extends Entitiy implements Approver{
    private String title;
    private String description;
    private ProposalStatus status;
    private long clientId;
    private long userId;

    private Proposal(long id, String title, String description, ProposalStatus status, long clientId, long userId) {
        super(id);
        this.title = title;
        this.description = description;
        this.status = status;
        this.clientId = clientId;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Sets the Proposal status to Approved
     *
     */
    @Override
    public void approveProposal() {
        this.status = ProposalStatus.APPROVED;
    }

    /**
     * Sets the Proposal status to Rejected
     *
     */
    @Override
    public void rejectProposal() {
        this.status = ProposalStatus.REJECTED;
    }

    /**
     * Builder pattern for creating Proposal objects.
     */
    public static class Builder{
        private Long id;
        private String title;
        private String description;
        private ProposalStatus status;
        private long clientId;
        private long userId;

        public Builder() {
        }

        public Builder(Long id) {
            this.id = id;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withStatus(ProposalStatus status) {
            this.status = status;
            return this;
        }

        public Builder withClientId(long clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder withUserId(long userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Builds and returns a new Proposal object.
         * @return The created Proposal instance.
         */
        public Proposal build(){
            return new Proposal(id, title, description, status, clientId, userId);
        }


    }

    @Override
    public String toString() {
        return "" + status;
    }
}

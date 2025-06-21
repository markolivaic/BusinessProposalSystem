package model;

public sealed class Entitiy permits Client, Proposal, User {
    private long id;

    protected Entitiy(long id) {
        this.id = id;
    }

    protected Entitiy() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

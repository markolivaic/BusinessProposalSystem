package model;

public non-sealed class Client extends Entitiy{
    private String name;
    private String email;
    private String phone;
    private String company;

    public Client(long id, String name, String email, String company, String phone) {
        super(id);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    public Client(String name, String email, String company, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return name;
    }
}

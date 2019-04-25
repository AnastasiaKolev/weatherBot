package database;

/**
 * Класс пользователя с полями <b>userId</b>, <b>firstName</b>, <b>language</b>, <b>location</b> и <b>subscribe</b>.
 *
 * @author anastasia.kolevatykh
 * @version 1.0
 */
public class User {
    private Integer userId;
    private String firstName;
    private String language;
    private String location;
    private Boolean subscribe;

    public User(Integer userId, String firstName, String language, String location, Boolean subscribe) {
        this.userId = userId;
        this.firstName = firstName;
        this.language = language;
        this.location = location;
        this.subscribe = subscribe;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getSubscription() {
        return subscribe;
    }

    public void setSubscription(Boolean subscribe) {
        this.subscribe = subscribe;
    }
}

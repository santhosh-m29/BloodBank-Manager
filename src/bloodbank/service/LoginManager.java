package bloodbank.service;
import bloodbank.model.Person;
import bloodbank.utility.Validation;
public final class LoginManager {
    private String username;
    private String personId;
    public boolean authenticateUser(SystemState state, String username, String password) {
        logoutUser();
        for (Person person : state.people.values()) if (person.getUsername().equalsIgnoreCase(username) && person.authenticate(password)) {
            this.username = person.getUsername();
            personId = person.getPersonId();
            return true;
        }
        return false;
    }
    public String getUsername() {
        return username;
    }
    public Person current(SystemState state) {
        Validation.require(personId != null && state.people.containsKey(personId), "Please log in.");
        return state.people.get(personId);
    }
    public void changePassword(SystemState state, String oldPassword, String newPassword) {
        current(state).changePassword(oldPassword, newPassword);
    }
    public void logoutUser() {
        username = null;
        personId = null;
    }
}

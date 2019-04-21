import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс пользователей со свойствами <b>firstName</b>, <b>userId</b>, <b>language</b> и <b>subscribe</b>.
 * @author anastasia.kolevatykh
 * @version 1.0
 */
public class Users {
    /**
     * Облявляем переменную со список пользователей
     */
    private List<User> usersList = new ArrayList<>();

    public Users() {
        this.getFromDB();
    }

    public List<User> getUsers() {
        return usersList;
    }

    public User setUser(Integer id, String firstName, String language, String location, Boolean subscribe) {
        User user = new User( id, firstName, language, location, subscribe);
        this.usersList.add( user );
        this.saveToDB();
        return user;
    }

    public User getUser(Integer id, String firstName, String language, String location, Boolean subscribe) {
        for (Integer i = 0; i < usersList.size(); i++) {
            Integer _id = usersList.get( i ).getUserId();

            if (_id.equals( id )) {
                return usersList.get( i );
            }
        }
        return setUser( id, firstName, language, location, subscribe );
    }

    public List<User> getUsersWithSubscription () {
        List<User> usersWithSubscription = new ArrayList<User>();
        for (int i = 0; i < this.usersList.size(); i++){
            User tempUser = this.usersList.get( i );
            if ( tempUser.getSubscription() ) {
                usersWithSubscription.add( tempUser );
            }
        }
        return usersWithSubscription;
    }

    public void saveToDB() {
        try {
            BufferedWriter br = new BufferedWriter( new FileWriter( "./db/db.txt" ) );

            for (int i = 0; i < this.usersList.size(); i++) {
                User tempUser = this.usersList.get( i );
                String[] strings = {tempUser.getUserId().toString()
                        , tempUser.getFirstName()
                        , tempUser.getLanguage()
                        , tempUser.getLocation()
                        , tempUser.getSubscription().toString()};

                for (int j = 0; j < strings.length; j++) {
                    br.write( strings[j] );
                    if (j < strings.length - 1) {
                        br.write( ";" );
                    }
                }

                br.write( "\n" );
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getFromDB() {
        try {
            BufferedReader reader = new BufferedReader( new FileReader( "./db/db.txt" ) );
            String line;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split( ";" );

                User tempUser = new User( Integer.parseInt( temp[0] )
                        , temp[1]
                        , temp[2]
                        , temp[3]
                        , Boolean.parseBoolean( temp[4] ) );

                usersList.add( tempUser );
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

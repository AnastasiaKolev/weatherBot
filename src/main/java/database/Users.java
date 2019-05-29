package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс пользователей со свойствами <b>firstName</b>, <b>userId</b>, <b>language</b> и <b>subscribe</b>.
 *
 * @author anastasia.kolevatykh
 * @version 1.0
 */
public class Users {
    private static volatile Users instance;

    private List<User> usersList = new ArrayList<>();
    private Connection connection = null;

    public static Users getInstance() {
        Users localInstance = instance;
        if (localInstance == null) {
            synchronized (Users.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Users();
                    instance.init();
                }
            }
        }
        return localInstance;
    }

    private void init() {
        try {
            // create a database connection
            connection = DriverManager.getConnection( "jdbc:sqlite:db/db.db" );

            // Statement - for queries
            Statement statement = connection.createStatement();
            statement.setQueryTimeout( 30 );  // set timeout to 30 sec.
            String sql = "create table if not exists user (userId string, firstName string, language string, location string, subscribe boolean)";
            statement.executeUpdate( sql );
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println( e.getMessage() );
        }

        this.getFromDB();
    }

    private User setUser(Integer id, String firstName, String language, String location, Boolean subscribe) {
        User user = new User( id, firstName, language, location, subscribe );
        this.usersList.add( user );
        this.saveToDB();
        return user;
    }

    public User getUser(Integer id, String firstName, String language, String location, Boolean subscribe) {
        for (User user : usersList) {
            Integer _id = user.getUserId();

            if (_id.equals( id )) {
                return user;
            }
        }

        return setUser( id, firstName, language, location, subscribe );
    }

    public List<User> getUsersWithSubscription() {
        List<User> usersWithSubscription = new ArrayList<>();

        for (User tempUser : this.usersList) {
            if (tempUser.getSubscription()) {
                usersWithSubscription.add( tempUser );
            }
        }

        return usersWithSubscription;
    }

    public void saveToDB() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate( "drop table if exists user" );
            String sql = "create table if not exists user (userId string, firstName string, language string, location string, subscribe boolean)";
            statement.executeUpdate( sql );

            for (User tempUser : this.usersList) {
                String rq = "insert into user values("
                        + "'" + tempUser.getUserId().toString() + "',"
                        + "'" + tempUser.getFirstName() + "',"
                        + "'" + tempUser.getLanguage() + "',"
                        + "'" + tempUser.getLocation() + "',"
                        + "'" + tempUser.getSubscription() + "'"
                        + ")";
                statement.executeUpdate( rq );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getFromDB() {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery( "select * from user" );

            User tempUser;

            while (rs.next()) {
                tempUser = new User(
                        rs.getInt( "userId" ),
                        rs.getString( "firstName" ),
                        rs.getString( "language" ),
                        rs.getString( "location" ),
                        Boolean.parseBoolean( rs.getString( "subscribe" ) )
                );
                usersList.add( tempUser );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

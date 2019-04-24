package credentials;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Credentials {
    CredentialsData credentialsData;

    private static volatile Credentials instance;

    /**
     * Constructor (private due to singleton pattern)
     */
    private Credentials () {

    }

    /**
     * Singleton
     *
     * @return Return the instance of this class
     */
    public static Credentials getInstance() {
        Credentials localInstance = instance;
        if (localInstance == null) {
            synchronized (Credentials.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Credentials();
                }
            }
        }
        return localInstance;
    }

    public void read() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        try {
            this.credentialsData = mapper.readValue(new File("./config/credentials.json"), CredentialsData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBotToken() {
        return this.credentialsData.getToken();
    }

    public String getBotUsername() {
        return this.credentialsData.getUsername();
    }

    public String getAPPID() {
        return this.credentialsData.getAppid();
    }
}

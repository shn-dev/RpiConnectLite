package src.general.config;

import javafx.stage.Stage;
import src.connection.CredentialHandler;

public class Global {

    public static class Constants{
        public static final boolean isProduction = true;

        public static final String DEV_DEFAULT_HOST = "";
        public static final String DEV_DEFAULT_USER = "";
        public static final int DEV_DEFAULT_PORT = 22;
        public static final String DEV_DEFAULT_PASS = "";
        public static final String DEV_DEFAULT_WORKING_DIR= "";
    }

    public static CredentialHandler credentialHandler;
    public static Stage loadingForm;

    private static String remoteFileSeparator;
    public static String getRemoteFileSeparator(){
        if(credentialHandler!=null && credentialHandler.getSFTPWorkingDir()!=null){
            if(remoteFileSeparator==null) {
                remoteFileSeparator = credentialHandler.getSFTPWorkingDir().contains("/") ?
                        "/" : "\\";
            }
            return remoteFileSeparator;
        }
        else{
            throw new NullPointerException("Credentials need to be set.");
        }
    }

}

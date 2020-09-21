package src.connection;

public abstract class CredentialHandler {

    protected String SFTPHost;
    protected String SFTPPort;
    protected String SFTPUser;
    protected String SFTPPass;
    protected String SFTPWorkingDir;
    protected String SFTPPrivateKey;

    public CredentialHandler(){
        bindVariables();
    }

    private void bindVariables(){
        setSFTPHost();
        setSFTPPass();
        setSFTPPort();
        setSFTPPrivateKey();
        setSFTPWorkingDir();
        setSFTPUser();
    }

    public String getSFTPHost() {
        if(SFTPHost==null){
            SFTPHost = setSFTPHost();
        }
        return SFTPHost;
    }

    public String getSFTPPort() {
        if(SFTPPort==null){
            SFTPPort = setSFTPPort();
        }
        return SFTPPort;
    }

    public String getSFTPUser() {
        if(SFTPUser==null){
            SFTPUser = setSFTPUser();
        }
        return SFTPUser;
    }

    public String getSFTPPass() {
        if(SFTPPass==null){
            SFTPPass = setSFTPPass();
        }
        return SFTPPass;
    }

    public String getSFTPWorkingDir() {
        if(SFTPWorkingDir==null){
            SFTPWorkingDir = setSFTPWorkingDir();
        }
        return SFTPWorkingDir;
    }

    public String getSFTPPrivateKey() {
        if(SFTPPrivateKey==null){
            SFTPPrivateKey = setSFTPPrivateKey();
        }
        return SFTPPrivateKey;
    }

    /**
     * Implement this a method that gets the variable and return it.
     * Access the variable in the future using the "get" version.
     * @return
     */
    public abstract String setSFTPHost();
    /**
     * Implement this a method that gets the variable and return it.
     * Access the variable in the future using the "get" version.
     * @return
     */
    public abstract String setSFTPPort();
    /**
     * Implement this a method that gets the variable and return it.
     * Access the variable in the future using the "get" version.
     * @return
     */
    public abstract String setSFTPUser();
    /**
     * Implement this a method that gets the variable and return it.
     * Access the variable in the future using the "get" version.
     * @return
     */
    public abstract String setSFTPPass();
    /**
     * Implement this a method that gets the variable and return it.
     * Access the variable in the future using the "get" version.
     * @return
     */
    public abstract String setSFTPWorkingDir();

    /**
     * Set the directory to the SFTP private key.
     * Implement this a method that gets the variable and return it.
     * Access the variable in the future using the "get" version.
     * @return
     */
    public abstract String setSFTPPrivateKey();
}

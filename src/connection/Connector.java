package src.connection;

import com.jcraft.jsch.*;
import src.general.config.Global;

import java.io.File;

/**
 * Establishes a connection with a remote device via SSH, then provides an interface to
 * work using the connection.
 */
public class Connector {


    protected interface IConnection extends IConnectionStateChange {
        void deviceInteraction(ChannelSftp channel) throws SftpException;
    }

    public interface IConnectionStateChange {
        void onAuthFailure();
        void onGenericException(Exception ex);
        void onSuccess();
    }

    protected static void connect(IConnection connection) {

        if (Global.credentialHandler == null) {
            System.out.println("Credentials were not set. Please ensure these are set before attempting to connect to remote device.");
            return;
        }

        String SFTPHOST = Global.credentialHandler.getSFTPHost();
        int SFTPPORT = Integer.parseInt(Global.credentialHandler.getSFTPPort());
        String SFTPUSER = Global.credentialHandler.getSFTPUser();
        String SFTPPASS = Global.credentialHandler.getSFTPPass();
        String SFTPPRIVATEKEY = Global.credentialHandler.getSFTPPrivateKey(); //"/path/to/xxxxxxxxx.pem";

        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            File privateKey = SFTPPRIVATEKEY != null ? new File(SFTPPRIVATEKEY) : null;
            if (privateKey != null && privateKey.exists() && privateKey.isFile())
                jsch.addIdentity(SFTPPRIVATEKEY);
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();

            connection.deviceInteraction((ChannelSftp)channel);

        }
        catch(JSchException ex){
            if(ex.getMessage().equals("Auth fail")){
                connection.onAuthFailure();
            }
        }
        catch (Exception ex) {
            connection.onGenericException(ex);
        } finally {
            if (session != null) session.disconnect();
            if (channel != null) channel.disconnect();
        }
    }
}

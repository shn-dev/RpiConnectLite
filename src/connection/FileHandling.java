package src.connection;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class FileHandling {


    public static void getFile(String src, String dest, Connector.IConnectionStateChange failure){
        Connector.connect(new Connector.IConnection() {
            @Override
            public void deviceInteraction(ChannelSftp channel){
                try {
                    channel.get(src, dest);
                    failure.onSuccess();
                } catch (SftpException e) {
                    failure.onGenericException(e);
                }
            }

            @Override
            public void onAuthFailure() {
                failure.onAuthFailure();
            }

            @Override
            public void onGenericException(Exception ex) {
                failure.onGenericException(ex);
            }

            @Override
            public void onSuccess() {
                failure.onSuccess();
            }
        });
    }

    public static void sendFile(String src, String dest, Connector.IConnectionStateChange failure){
        Connector.connect(new Connector.IConnection() {
            @Override
            public void deviceInteraction(ChannelSftp channel){
                try {
                    channel.put(src, dest);
                    failure.onSuccess();
                } catch (SftpException e) {
                    failure.onGenericException(e);
                }
            }

            @Override
            public void onAuthFailure() {
                failure.onAuthFailure();
            }

            @Override
            public void onGenericException(Exception ex) {
                failure.onGenericException(ex);
            }

            @Override
            public void onSuccess() {
                failure.onSuccess();
            }
        });
    }
}

package src.connection;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class FileHandling {


    public static void getFile(String src, String dest, Connector.IConnectionStateChange onStateChange){
        Connector.connect(new Connector.IConnection() {
            @Override
            public void deviceInteraction(ChannelSftp channel){
                try {
                    channel.get(src, dest);
                    onStateChange.onSuccess();
                } catch (SftpException e) {
                    onStateChange.onGenericException(e);
                }
            }

            @Override
            public void onAuthFailure() {
                onStateChange.onAuthFailure();
            }

            @Override
            public void onGenericException(Exception ex) {
                onStateChange.onGenericException(ex);
            }

            @Override
            public void onSuccess() {
                onStateChange.onSuccess();
            }
        });
    }

    public static void sendFile(String src, String dest, Connector.IConnectionStateChange onStateChange){
        Connector.connect(new Connector.IConnection() {
            @Override
            public void deviceInteraction(ChannelSftp channel){
                try {
                    channel.put(src, dest);
                    onStateChange.onSuccess();
                } catch (SftpException e) {
                    onStateChange.onGenericException(e);
                }
            }

            @Override
            public void onAuthFailure() {
                onStateChange.onAuthFailure();
            }

            @Override
            public void onGenericException(Exception ex) {
                onStateChange.onGenericException(ex);
            }

            @Override
            public void onSuccess() {
                onStateChange.onSuccess();
            }
        });
    }
}

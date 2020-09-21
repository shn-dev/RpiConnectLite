package src.connection;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.util.function.Consumer;

public abstract class ConnectionHandler implements Connector.IConnection {
    /*
    private Runnable onSuccess;
    private Consumer<ChannelSftp> deviceInteraction;
    public ConnectionHandler(Runnable onSuccess, Consumer<ChannelSftp> deviceInteraction){
        this.onSuccess = onSuccess;
        this.deviceInteraction = deviceInteraction;
    }

    @Override
    public void deviceInteraction(ChannelSftp channel) throws SftpException {
        deviceInteraction.accept(channel);
    }

    @Override
    public void onAuthFailure() {

    }

    @Override
    public void onGenericException(Exception ex) {

    }

    @Override
    public void onSuccess() {
        onSuccess.run();
    }*/
}

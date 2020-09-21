package src.connection;

import com.jcraft.jsch.*;
import src.general.config.Global;

/**
 * Allows the user to list all files in a given directory.
 */
public class FileViewing {

    /**
     * Interface that allows other classes to view files in a directory
     */

    public interface IFileBrowser extends Connector.IConnectionStateChange {
        void IFileExplorer(ChannelSftp channel, String dir) throws SftpException;
    }


    /**
     * Allows the user to list all files in a given directory.
     * @param iFileBrowser An interface that allows the user to browse the files in a given directory.
     */
    public void getFiles(IFileBrowser iFileBrowser) throws SftpException{
        Connector.connect(new Connector.IConnection() {
            @Override
            public void deviceInteraction(ChannelSftp channel){
                try {
                    //Create connection with device
                    String workingDir = Global.credentialHandler.getSFTPWorkingDir();
                    channel.cd(workingDir);

                    //Delegate creation of file tree to Controller
                    iFileBrowser.IFileExplorer(channel, workingDir);
                }
                catch(SftpException ex){
                    iFileBrowser.onSuccess();
                }

            }

            @Override
            public void onAuthFailure() {
                iFileBrowser.onAuthFailure();
            }

            @Override
            public void onGenericException(Exception ex) {
                iFileBrowser.onGenericException(ex);
            }

            @Override
            public void onSuccess() {
                iFileBrowser.onSuccess();
            }
        });
    }
}

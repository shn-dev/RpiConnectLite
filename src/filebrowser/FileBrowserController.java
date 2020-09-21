package src.filebrowser;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import src.connection.Connector;
import src.connection.FileHandling;
import src.connection.FileViewing;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import src.general.config.Global;
import src.general.popup.Popup;
import src.general.utility.GenericHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Vector;

import static src.general.ExceptionHandling.*;

/**
 * Generates a TreeView (file directory browser) by recursively looping through the remote device's directories.
 * It also provides functionality for copying file/directory paths or editing the files remotely.
 */
public class FileBrowserController {

    @FXML
    private TextArea logger;

    @FXML
    private GridPane parentNode;

    @FXML
    private TreeView<AppFile> tree;

    @FXML
    public void initialize(){

        FileTreeItem rootItem = new FileTreeItem();
        //Create a task that will allow threaded recursion over remote device filetree @ workingdir.
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call(){
                try {
                    rootItem.buildTree();
                    return true;
                } catch (Exception ex) {
                    //TODO: add logging
                }
                return false;
            }
        };

        task.setOnSucceeded(workerStateEvent -> {

            if(task.getValue()) {
                //Close the loading window.
                Global.loadingForm.hide();

                tree.setRoot(rootItem);
                rootItem.setExpanded(true);
                tree.setContextMenu(getContextMenu());

                //Update the title of the form
                Stage stage = ((Stage) parentNode.getScene().getWindow());
                stage.setTitle("Viewing Device @ " +
                        Global.credentialHandler.getSFTPHost() +
                        ":" +
                        Global.credentialHandler.getSFTPPort());

                //Data has been obtained, show the file tree scene.
                stage.show();

                log("Successfully connected.");
            }
            else { //task failed
                Global.loadingForm.hide();
                Popup.issuePopup("Issue retrieving files.", "An unknown issue occurred retrieving files from the device.");
            }

        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

    }

    private void log(String text){

        String currDateTime = LocalDateTime.now().toString();
        logger.appendText(currDateTime);
        logger.appendText("\n");
        logger.appendText(text);
        logger.appendText("\n");

    }

    /**
     * Creates the options for when you right click on a FileTreeItem in the TreeView.
     * @return
     */
    private ContextMenu getContextMenu(){
        MenuItem copyItem = new MenuItem("Copy path");
        MenuItem editItem = new MenuItem("Direct edit");

        /**
         * Clicking "copy path" will copy the selected item in the tree view
         */
        copyItem.setOnAction(actionEvent -> {
            FileTreeItem item = (FileTreeItem) tree.getSelectionModel().getSelectedItem();
            String selectedPath = item.getValue().getAbsolutePath();
            GenericHelper.copyToClipboard(selectedPath);
        });

        editItem.setOnAction(actionEvent -> {
            /**
             * When an item is clicked in the filetree,
             * we move it to the current device at path = newPath.
             * We then register a FileWatch object which looks for changes.
             * When a change is detected, we copy the change to the new device.
             */


            FileTreeItem selectedItem = (FileTreeItem) tree.getSelectionModel().getSelectedItem();
            System.out.println("Selected Text : " + selectedItem.getValue());

            String absCopyFrom = selectedItem.getValue().getAbsolutePath();

            String dirCopyTo = System.getProperty("user.dir") + "/";
            String absPathCopyTo = dirCopyTo + selectedItem.getValue().getName();

            //Copies the selected file to the working directory.
            FileHandling.getFile(absCopyFrom, absPathCopyTo, new Connector.IConnectionStateChange() {
                @Override
                public void onAuthFailure() {
                    log("You may have provided incorrect credentials.");
                }

                @Override
                public void onGenericException(Exception ex) {
                    log("An unknown exception occurred." + ex.getMessage());
                }

                @Override
                public void onSuccess() {
                    log("Sucessfully obtained file from device.");
                }
            });


            new Thread(new FileWatcher(Paths.get(dirCopyTo), new FileWatcher.IFileChanges() {
                @Override
                public void onFileCreated(Path relDir) {
                    //TODO: Handle filecreation
                }

                @Override
                public void onFileModified(Path relDir) {
                    FileHandling.sendFile(absPathCopyTo, absCopyFrom, new Connector.IConnectionStateChange() {
                        @Override
                        public void onAuthFailure() {
                            log("You may have provided incorrect credentials.");
                        }

                        @Override
                        public void onGenericException(Exception ex) {
                            log("An unknown exception occurred." + ex.getMessage());
                        }

                        @Override
                        public void onSuccess() {
                            log("Sucessfully modified file at " + absCopyFrom);
                        }
                    });
                }

                @Override
                public void onFileDeleted(Path relDir) {
                    //TODO: Handle file deletion
                }

                @Override
                public void onError(Exception ex) {
                    if (ex instanceof SftpException) {
                        log("You need permissions to write to the file you" +
                                " are trying to edit. Consider using chmod in the terminal window to edit permissions " +
                                "or Google how to change write/read permissions.");
                    }
                }
            })).start();

            try {
                GenericHelper.openFile(absPathCopyTo);
            } catch (IOException e) {
                System.out.println("Could not open " + absCopyFrom + ". Error: " + e.getMessage());
            }
        });

        return new ContextMenu(copyItem, editItem);
    }

    /**
     * A regular java File that also includes whether it is a leaf or not.
     */
    private static class AppFile extends File {

        boolean isLeaf;

        AppFile(String pathname, boolean isLeaf) {
            super(pathname);
            this.isLeaf = isLeaf;
        }

        boolean isLeaf() {
            return isLeaf;
        }
    }

    /**
     * A TreeItem that includes code that generates a file tree from a root object.
     */
    private class FileTreeItem extends TreeItem<AppFile>{

        @Override
        public boolean isLeaf() {
            return getValue().isLeaf();
        }

        /**
         * Creates a file tree from a working directory. The working directory
         * is specified in the initial GUI by the user.
         */
        public void buildTree(){
            final FileTreeItem item = this;
            FileViewing lrs = new FileViewing();
            //getFileBrowser gets the files from the remote device and includes IO error handling.
            try {
                lrs.getFiles(new FileViewing.IFileBrowser() {
                    @Override
                    public void IFileExplorer(ChannelSftp channel, String dir){
                        addNode(channel, dir, item);
                    }

                    @Override
                    public void onAuthFailure() {
                        log("You may have provided incorrect credentials.");
                    }

                    @Override
                    public void onGenericException(Exception ex) {
                        log("An unknown exception occurred." + ex.getMessage());
                    }

                    @Override
                    public void onSuccess() {
                        log("Finished generating file tree.");
                    }
                });
            }
            catch(SftpException ex){
                log("Something went wrong trying to view the files at " +
                        Global.credentialHandler.getSFTPWorkingDir() +
                        ". " +
                        ex.getMessage());
            }
        }

        /**
         * Recursively go through all node within working directory.
         * @param channel SFTPChannel object maintaining connection with remote device.
         * @param dir The working directory to recurse through.
         * @param fti The current node in the tree.
         * @throws SftpException
         */
        private void addNode(ChannelSftp channel, String dir, FileTreeItem fti){
            try{
                Vector filelist = channel.ls(dir);
                filelist.forEach(obj -> {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
                    String path = dir + "/" + entry.getFilename();
                    if (entry.getFilename().equals(".")) {
                        //this is the root. Set the root and continue.
                        fti.setValue(new AppFile(path, false));
                        return;
                    } else if (entry.getFilename().equals("..")) {
                        return;
                    }

                    FileTreeItem newItem = new FileTreeItem();
                    fti.getChildren().add(newItem);
                    if (!entry.getAttrs().isDir()) {
                        newItem.setValue(new AppFile(path, true));
                    } else {
                        addNode(channel, dir + "/" + ((ChannelSftp.LsEntry) obj).getFilename(), newItem);
                    }

                });
            }
            catch(SftpException ex){ //this error is called if "ls" fails
                log("Something went wrong when finding files in " + dir);
                log("More detailed description of the error: " + ex.getMessage());
            }
        }
    }
}
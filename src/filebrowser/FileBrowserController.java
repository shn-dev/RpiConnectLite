package src.filebrowser;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
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
import java.nio.file.Files;
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

    private Path tempDirectory;

    @FXML
    public void initialize() {

        FileTreeItem rootItem = new FileTreeItem();
        //Create a task that will allow threaded recursion over remote device filetree @ workingdir.
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    //Create a temporary dir for copied files to be stored.
                    tempDirectory = Files.createTempDirectory("RpiConnectLiteTmp");
                    //begin building the file tree for the UI
                    rootItem.buildTree();
                    return true;
                } catch (Exception ex) {
                    //TODO: add logging
                }
                return false;
            }
        };

        task.setOnSucceeded(workerStateEvent -> {

            if (task.getValue()) {
                try {
                    //Close the loading window.
                    Global.loadingForm.hide();

                    //Add tree items to the tree view.
                    tree.setRoot(rootItem);

                    //Items in the tree will be expanded.
                    rootItem.setExpanded(true);

                    //Setup what happens when an item is right clicked.
                    tree.setContextMenu(getContextMenu());

                    //If an item is double clicked in tree view, enter edit mode.
                    tree.setOnMouseClicked(getDoubleClickProperty());

                    //Update the title of the form
                    Stage stage = ((Stage) parentNode.getScene().getWindow());
                    stage.setTitle("Viewing Device @ " +
                            Global.credentialHandler.getSFTPHost() +
                            ":" +
                            Global.credentialHandler.getSFTPPort());

                    //Data has been obtained, show the file tree scene.
                    stage.show();

                    log("Successfully connected.");
                } catch (Exception ex) {
                    Popup.issuePopup("Connection issue.",
                            "A problem occurred connecting to the device: " + ex.getMessage());
                }
            } else { //task failed
                Global.loadingForm.hide();
                Popup.issuePopup("Issue retrieving files.",
                        "An unknown issue occurred retrieving files from the device.");
            }

        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

    }

    private void log(String text) {

        String currDateTime = LocalDateTime.now().toString();
        logger.appendText(currDateTime);
        logger.appendText("\n");
        logger.appendText(text);
        logger.appendText("\n");

    }

    /**
     * Double clicking a tree item will enter edit mode.
     * @return
     */
    private EventHandler<MouseEvent> getDoubleClickProperty(){
        return event -> {
            if(event.getClickCount() == 2)
            {
                FileTreeItem selectedItem = (FileTreeItem) tree.getSelectionModel().getSelectedItem();
                System.out.println("Selected Text : " + selectedItem.getValue());
                directEdit(selectedItem.getValue().toString());
            }
        };
    }

    /**
     * Creates the options for when you right click on a FileTreeItem in the TreeView.
     *
     * @return
     */
    private ContextMenu getContextMenu() {
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

        /**
         * When an item is clicked in the filetree,
         * we move it to the current device at path = newPath.
         * We then register a FileWatch object which looks for changes.
         * When a change is detected, we copy the change to the new device.
         */
        editItem.setOnAction(actionEvent -> {
            FileTreeItem selectedItem = (FileTreeItem) tree.getSelectionModel().getSelectedItem();
            System.out.println("Selected Text : " + selectedItem.getValue());
            directEdit(selectedItem.getValue().toString());
        });

        return new ContextMenu(copyItem, editItem);
    }

    /**
     * Allows editing of the remote file. Works by first copying the remote file
     * to a temporary directory on the client device. A listener is attached
     * that looks for file changes in the temporary directory. If a modification is
     * detected the new file is sent back to the remote device.
     * @param absCopyFrom The absolute path of the file on the remote device. It is
     *                    important to keep the file separators in the remote device path name
     *                    exactly as they are on the remote device.
     */
    private void directEdit(String absCopyFrom){

        FileTreeItem selectedItem = (FileTreeItem) tree.getSelectionModel().getSelectedItem();
        System.out.println("Selected Text : " + selectedItem.getValue());

        /**
         * IMPORTANT:
         * File separators are different between Linux/Mac ("/") and Windows ("\").
         * File separators for the remote device are retrieved using Global.getRemoteFileSeparator.
         * The local device should use Java's File.separator.
         */

        /**
         * The folder that the copied file will be located.
         */
        String relCopyTo = tempDirectory + File.separator;
        /**
         * The absolute file location where the file from the remote device will be copied to.
         */
        String absPathCopyTo = relCopyTo + selectedItem.getValue().getName();

        //Copies the selected file to the working directory.
        FileHandling.getFile(absCopyFrom, absPathCopyTo, new Connector.IConnectionStateChange() {
            @Override
            public void onAuthFailure() {
                log("You may have provided incorrect credentials.");
            }

            @Override
            public void onGenericException(Exception ex) {
                StringBuilder sb = new StringBuilder("An unknown exception occurred. ");
                sb.append(ex.getMessage());
                sb.append(" Tried to get file from " + absCopyFrom);
                sb.append(". Tried to add file to " + absPathCopyTo);
                log(sb.toString());
            }

            @Override
            public void onSuccess() {
                log(
                        "Sucessfully obtained file from device.");

                System.out.println("Copied file to temporary directory " + absPathCopyTo);
            }
        });


        //Start a thread that monitors changes to the temporary directory the file was copied to.
        new Thread(new FileWatcher(Paths.get(relCopyTo), new FileWatcher.IFileChanges() {
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
                        StringBuilder sb = new StringBuilder("An unknown exception occurred. ");
                        sb.append(ex.getMessage());
                        //The variables below are reversed since we are sending the file instead of getting.
                        sb.append(" Tried to send file to " + absPathCopyTo);
                        sb.append(". Tried to add file to " + absCopyFrom);
                        log(sb.toString());
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

        //Open the file copied from the remote device.
        try {
            GenericHelper.openFile(absPathCopyTo);
        } catch (IOException e) {
            System.out.println("Could not open " + absCopyFrom + ". Error: " + e.getMessage());
        }
    }

    /**
     * A regular java File that also includes whether it is a leaf or not.
     */
    private static class AppFile extends File {

        /**
         * If a file is coming from a Linux -> Windows machine, the file separator
         * will change from "/" to "\." This has the consequence of creating a "No Such File"
         * error when trying to a file from the file tree.
         * unalteredPathname is the path name *as it is described" by the remote device.
         */
        private String unalteredPathname;
        boolean isLeaf;

        AppFile(String pathname, boolean isLeaf) {
            super(pathname);
            this.isLeaf = isLeaf;
            unalteredPathname = pathname;
        }

        @Override
        /**
         * Gives the path name as it is described *on the remote device.*
         */
        public String toString() {
            return unalteredPathname;
        }

        boolean isLeaf() {
            return isLeaf;
        }
    }

    /**
     * A TreeItem that includes code that generates a file tree from a root object.
     */
    private class FileTreeItem extends TreeItem<AppFile> {

        @Override
        public boolean isLeaf() {
            return getValue().isLeaf();
        }

        /**
         * Creates a file tree from a working directory. The working directory
         * is specified in the initial GUI by the user.
         */
        public void buildTree() {
            final FileTreeItem item = this;
            FileViewing lrs = new FileViewing();
            //getFileBrowser gets the files from the remote device and includes IO error handling.
            try {
                lrs.getFiles(new FileViewing.IFileBrowser() {
                    @Override
                    public void IFileExplorer(ChannelSftp channel, String dir) {
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
            } catch (SftpException ex) {
                log("Something went wrong trying to view the files at " +
                        Global.credentialHandler.getSFTPWorkingDir() +
                        ". " +
                        ex.getMessage());
            }
        }

        /**
         * Recursively go through all node within working directory.
         *
         * @param channel SFTPChannel object maintaining connection with remote device.
         * @param dir     The working directory to recurse through.
         * @param fti     The current node in the tree.
         * @throws SftpException
         */
        private void addNode(ChannelSftp channel, String dir, FileTreeItem fti) {
            try {
                Vector filelist = channel.ls(dir);
                filelist.forEach(obj -> {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
                    String path = dir + Global.getRemoteFileSeparator() + entry.getFilename();
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
                        addNode(channel, dir + Global.getRemoteFileSeparator() + ((ChannelSftp.LsEntry) obj).getFilename(), newItem);
                    }

                });
            } catch (SftpException ex) { //this error is called if "ls" fails
                log("Something went wrong when finding files in " + dir);
                log("More detailed description of the error: " + ex.getMessage());
            }
        }
    }
}
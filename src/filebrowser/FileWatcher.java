package src.filebrowser;

import src.connection.Connector;

import java.io.IOException;
import java.nio.file.*;

/**
 * Detects any changes to a file once it is clicked.
 */
class FileWatcher implements Runnable {

    private Path path;
    private IFileChanges eventHandler;

    /**
     * Interface describing what happens when changes are made to a file.
     */
    public interface IFileChanges{
        void onFileCreated(Path relDir);
        void onFileModified(Path relDir);
        void onFileDeleted(Path relDir);
        void onError(Exception ex);
    }

    public FileWatcher(Path path, IFileChanges eventHandler) {
        this.path = path;
        this.eventHandler = eventHandler;
    }

    /**
     * print the events and the affected file
     */

    private void printEvent(WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Path pathCreated = (Path) event.context();
            eventHandler.onFileCreated(path);
            System.out.println("Entry created:" + pathCreated);
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            Path pathDeleted = (Path) event.context();
            eventHandler.onFileDeleted(path);
            System.out.println("Entry deleted:" + pathDeleted);
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            Path pathModified = (Path) event.context();
            eventHandler.onFileModified(path);
            System.out.println("Entry modified:" + pathModified);
        }
    }

    @Override
    /**
     * Watch for changes at a given directory.
     */
    public void run() {
        try {
            WatchService watchService = path.getFileSystem().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            // loop forever to watch directory
            while (true) {
                WatchKey watchKey;
                watchKey = watchService.take(); // this call is blocking until events are present

                // poll for file system events on the WatchKey
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    printEvent(event);
                }

                // if the watched directed gets deleted, get out of run method
                if (!watchKey.reset()) {
                    System.out.println("No longer valid");
                    watchKey.cancel();
                    watchService.close();
                    break;
                }
            }

        } catch (InterruptedException ex) {
            System.out.println("interrupted. Goodbye");
            return;
        } catch (IOException ex) {
            eventHandler.onError(ex);
            return;
        }
    }
}

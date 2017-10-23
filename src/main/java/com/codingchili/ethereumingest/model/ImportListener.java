package com.codingchili.ethereumingest.model;

/**
 * Listener for events emitted by an importer.
 *
 * All methods are optional for listeners.
 */
public interface ImportListener {

    /**
     * Called when importing of an item has succeeded.
     *
     * @param hash   the hash of the imported item.
     * @param number the index of the imported item.
     */
    default void onImported(String hash, Long number) {}

    /**
     * Called when the queue size has been changed.
     *
     * @param queued the number of items in the queue.
     */
    default void onQueueChanged(int queued) {}

    /**
     * Called when all import tasks has been finished by the importer.
     * Will only be called once, after which the importer stops itself.
     */
    default void onFinished() {}

    /**
     * Called when an error has occured during import.
     *
     * @param e the cause of the exception.
     * @return true if the importing should cancel.
     */
    default boolean onError(Throwable e) {
        throw new RuntimeException(e);
    }
}

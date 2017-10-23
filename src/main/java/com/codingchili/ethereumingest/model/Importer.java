package com.codingchili.ethereumingest.model;

import com.codingchili.core.listener.CoreService;

/**
 * Implemented by importer implementations.
 */
public interface Importer extends CoreService {

    /**
     * @param listener listener to be added to the importer.
     */
    Importer setListener(ImportListener listener);
}

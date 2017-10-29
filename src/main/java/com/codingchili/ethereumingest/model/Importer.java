package com.codingchili.ethereumingest.model;

import com.codingchili.core.listener.CoreService;

/**
 * Implemented by importer implementations.
 */
public interface Importer extends CoreService {

    /**
     * @param listener listener to be set on the importer.
     * @return fluent.
     */
    Importer setListener(ImportListener listener);
}

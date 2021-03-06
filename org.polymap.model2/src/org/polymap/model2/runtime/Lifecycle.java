/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.model2.runtime;

import org.polymap.model2.Entity;

/**
 * An {@link Entity} can implement this interface in order to get lifecycle events.
 * <p/>
 * EXPERIMENTAL: This is work in progress. Not well tested. API may change.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Lifecycle {

    public enum State {
        BEFORE_PREPARE,
        AFTER_PREPARE,
        BEFORE_COMMIT,
        AFTER_COMMIT,
        BEFORE_ROLLBACK,
        AFTER_ROLLBACK,
        /** @deprecated Yet to be supported by the engine. */
        AFTER_LOADED,
        /** @deprecated Yet to be supported by the engine. */
        AFTER_CREATED,
        /** @deprecated Yet to be supported by the engine. */
        BEFORE_REMOVED
    }
    
    public void onLifecycleChange( State state );

}

/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.model2.runtime.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.polymap.model2.runtime.EntityRepository;

/**
 * Provides a way to define {@link EntityRepository} configurations via the extension
 * point {@value #EXTENSION_POINT}.
 * 
 * @deprecated Work in progress.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class EntityRepositoryAssembler {

    private static Log log = LogFactory.getLog( EntityRepositoryAssembler.class );

    public static final String          EXTENSION_POINT = "org.polymap.model2.entityAssemblers";
    
    
    /**
     * Attempts to find the given assembler type defined as extension and use it to
     * create a new {@link EntityRepository}.
     *
     * @param type The type of the assembler to find from extension and then use to
     *        create the {@link EntityRepository}
     * @return Newly created {@link EntityRepository}.
     * @throws RuntimeException If the given assembler type was not defined by any
     *         extension.
     */
    public static EntityRepository create( Class<? extends EntityRepositoryAssembler> type ) {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor( EXTENSION_POINT );
        for (IConfigurationElement ext : extensions) {
            try {
                EntityRepositoryAssembler assembler = (EntityRepositoryAssembler)ext.createExecutableExtension( "class" );
                if (type.isAssignableFrom( assembler.getClass() )) {
                    return assembler.assemble();                    
                }
            }
            catch (Exception e) {
                log.error( "Error while initializing module: " + ext.getName(), e );
            }
        }
        throw new RuntimeException( "Assembler type was not defined by any extension: " + type );
    }
    
    
    // instance *******************************************
    
    public abstract EntityRepository assemble();
    
}

/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.model2.runtime;

import java.util.function.Supplier;

import javax.cache.Cache;

import org.polymap.model2.Entity;
import org.polymap.model2.engine.EntityRepositoryImpl;
import org.polymap.model2.store.StoreSPI;

/**
 * Configuration API.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityRepositoryConfiguration {

    protected StoreSPI              store;
    
    protected Class[]               entities;
    
    protected Supplier<Cache>       cacheFactory;
    
    
    /**
     * 
     */
    protected EntityRepositoryConfiguration() {
        cacheFactory = new Supplier<Cache>() {
            public Cache get() {
                return CacheManager.instance().newCache( CacheConfig.DEFAULT
                        .concurrencyLevel( 4 )
                        .initSize( 1024 )
                        .defaultElementSize( 1024 ) );
            }
        };
    }

    public EntityRepository create() {
        return new EntityRepositoryImpl( this );
    }
    
    public StoreSPI getStore() {
        return store;
    }
    
    public EntityRepositoryConfiguration setStore( StoreSPI store ) {
        this.store = store;
        return this;
    }

    public Class<Entity>[] getEntities() {
        return entities;
    }
    
    public EntityRepositoryConfiguration setEntities( Class... entities ) {
        this.entities = entities;
        return this;
    }
    
    public <K,V> Cache<K,V> newCache() {
        return cacheFactory.get();
    }
    
    /**
     * Specifies the factory to create caches for {@link Entity} and state
     * instances.
     */
    public EntityRepositoryConfiguration setCacheFactory( Supplier<Cache> cacheFactory ) {
        this.cacheFactory = cacheFactory;
        return this;
    }

}

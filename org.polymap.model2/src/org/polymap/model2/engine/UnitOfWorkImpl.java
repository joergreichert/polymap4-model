/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.model2.engine;

import static org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus.CREATED;
import static org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus.MODIFIED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import java.io.IOException;

import javax.cache.Cache.Entry;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;

import org.polymap.model2.Composite;
import org.polymap.model2.Entity;
import org.polymap.model2.engine.LoadingCache.Loader;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.ResultSet;
import org.polymap.model2.query.grammar.BooleanExpression;
import org.polymap.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.model2.runtime.ModelRuntimeException;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.model2.store.CloneCompositeStateSupport;
import org.polymap.model2.store.CompositeState;
import org.polymap.model2.store.CompositeStateReference;
import org.polymap.model2.store.StoreUnitOfWork;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UnitOfWorkImpl
        implements UnitOfWork {

    protected static final Exception        PREPARED = new Exception( "Successfully prepared for commit." );
    
    private static AtomicInteger            idCount = new AtomicInteger( (int)Math.abs( System.currentTimeMillis() ) );
    
    protected EntityRepositoryImpl          repo;
    
    /** Only set if this is the root UnitOfwork, or null if this is a nested instance. */
    protected StoreUnitOfWork               storeUow;
    
    protected LoadingCache<Object,Entity>   loaded;
    
    protected LoadingCache<String,Composite> loadedMixins;
    
    /** Strong reference to Entities that must not be GCed from {@link #loaded} cache. */
    protected ConcurrentMap<Object,Entity>  modified;
    
    protected volatile Exception            prepareResult;

    
    protected UnitOfWorkImpl( EntityRepositoryImpl repo, StoreUnitOfWork suow ) {
        this.repo = repo;
        this.storeUow = suow;
        assert repo != null : "repo must not be null.";
        assert suow != null : "suow must not be null.";

        MutableConfiguration cacheConfig = new MutableConfiguration();
        CacheManager cacheManager = repo.getConfig().cacheManager.get();
        this.loaded = LoadingCache.create( cacheManager, cacheConfig );
        this.loadedMixins = LoadingCache.create( cacheManager, cacheConfig );
        this.modified = new ConcurrentHashMap( 1024, 0.75f, 4 );
        
//        // check evicted entries and re-insert if modified
//        this.loaded.addEvictionListener( new CacheEvictionListener<Object,Entity>() {
//            public void onEviction( Object key, Entity entity ) {
//                // re-insert if modified
//                if (entity.status() != EntityStatus.LOADED) {
//                    loaded.putIfAbsent( key, entity );
//                }
//                // mark entity as evicted otherwise
//                else {
//                    EntityRuntimeContext entityContext = UnitOfWorkImpl.this.repo.contextOfEntity( entity );
//                    entityContext.raiseStatus( EntityStatus.EVICTED );
//                }
//            }
//        });
    }

    
    /**
     * Raises the status of the given Entity. Called by {@link ConstraintsPropertyInterceptor}.
     */
    protected void raiseStatus( Entity entity) {
        if (entity.status() == EntityStatus.MODIFIED
                || entity.status() == EntityStatus.REMOVED) {
            modified.putIfAbsent( entity.id(), entity );
        }        
    }


    @Override
    public <T extends Entity> T createEntity( Class<T> entityClass, Object id, ValueInitializer<T>... initializers ) {
        checkOpen();
        // build id; don't depend on store's ability to deliver id for newly created state
        id = id != null ? id : entityClass.getSimpleName() + "." + idCount.getAndIncrement();

        CompositeState state = storeUow.newEntityState( id, entityClass );
        assert id == null || state.id().equals( id );
        
        T result = repo.buildEntity( state, entityClass, this );
        repo.contextOfEntity( result ).raiseStatus( EntityStatus.CREATED );

        boolean ok = loaded.putIfAbsent( id, result );
        if (!ok) {
            throw new ModelRuntimeException( "ID of newly created Entity already exists." );
        }
        modified.put( id, result );
        
        // initializer
        try {
            if (initializers != null) {
                for (ValueInitializer<T> initializer : initializers) {
                    initializer.initialize( result );
                }
            }
        }
        catch (Exception e) {
            throw new IllegalStateException( "Error while initializing.", e );
        }
        
        return result;
    }


    @Override
    public <T extends Entity> T entity( final Class<T> entityClass, final Object id ) {
        return entity( entityClass, id, null );
    }


    protected <T extends Entity> T entity( final Class<T> entityClass, final Object id, 
            final Supplier<CompositeState> preloaded ) {
        assert entityClass != null;
        assert id != null;
        checkOpen();
        T result = (T)loaded.get( id, new Loader<Object,Entity>() {
            public Entity load( Object key ) throws RuntimeException {
                CompositeState supplied = preloaded != null ? preloaded.get() : null;
                CompositeState state = supplied != null
                        ? supplied
                        : storeUow.loadEntityState( id, entityClass );
                return state != null ? repo.buildEntity( state, entityClass, UnitOfWorkImpl.this ) : null;
            }
        });
        return result != null && result.status() != EntityStatus.REMOVED ? result : null;
    }


    @Override
    public <T extends Entity> T entityForState( final Class<T> entityClass, Object state ) {
        checkOpen();
        
        final CompositeState compositeState = storeUow.adoptEntityState( state, entityClass );
        final Object id = compositeState.id();
        
        // modified
        if (modified.containsKey( id ) && state != compositeState.getUnderlying()) {
            throw new RuntimeException( "Entity is already modified in this UnitOfWork." );
        }
        // build Entity instance
        return (T)loaded.get( id, new Loader<Object,Entity>() {
            public Entity load( Object key ) throws RuntimeException {
                return repo.buildEntity( compositeState, entityClass, UnitOfWorkImpl.this );
            }
        });
    }

    
    public <T extends Composite> T mixin( final Class<T> mixinClass, final Entity entity ) {
        assert mixinClass != null : "mixinClass must not be null.";
        assert entity != null : "entity must not be null.";
        checkOpen();
        
        String key = Joiner.on( '_' ).join( entity.id().toString(), mixinClass.getName() );
        return (T)loadedMixins.get( key, new Loader<String,Composite>() {
            public Composite load( String _key ) throws RuntimeException {
                return repo.buildMixin( entity, mixinClass, UnitOfWorkImpl.this );
            }
        });
    }


    @Override
    public void removeEntity( Entity entity ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T extends Entity> Query<T> query( final Class<T> entityClass ) {
        checkOpen();
        return new Query( entityClass ) {
            public ResultSet<T> execute() {
                // actual execute the query just once against the backend store, result ids
                // are cached for subsequent calls to iterator(); however, creating and filtering
                // entities is done on-the-fly to avoid loading all Entities in memory at same time

                // XXX without this copy the collection is empty on second access;
                // iterate instead of addAll() to avoid call of size()
                final Collection<CompositeStateReference> refs = new ArrayList( 1024 );
                for (CompositeStateReference ref : storeUow.executeQuery( this ) ) {
                    refs.add( ref );
                }
                
                return new ResultSet<T>() {
                    
                    /** The cached size; not synchronized */
                    private int size = -1;

                    @Override
                    public Iterator<T> iterator() {
                        // unmodified
                        Stream<T> foundUnmodified = refs.stream()
                                .map( ref -> entity( entityClass, ref.id(), ref ) )
                                .filter( entity -> {
                                    EntityStatus status = entity.status();
                                    assert status != EntityStatus.CREATED; 
                                    return status == EntityStatus.LOADED;                            
                                });
                        // modified
                        // XXX not cached, done for every call to iterator()
                        Stream<T> foundModified = (Stream<T>)modified.values().stream()
                                .filter( entity -> {
                                    if (entity.getClass().equals( entityClass ) 
                                            && (entity.status() == CREATED || entity.status() == MODIFIED )) {
                                        if (expression == null) {
                                            return true;
                                        }
                                        else if (expression instanceof BooleanExpression) {
                                            return ((BooleanExpression)expression).evaluate( entity );
                                        }
                                        else {
                                            return storeUow.evaluate( entity.state(), expression );
                                        }
                                    }
                                    return false;
                                });
                        return Stream.concat( foundUnmodified, foundModified ).iterator();
                    }
                    @Override
                    public int size() {
                        if (size == -1) {
                            // avoid iterating if no modifications
                            size = modified.isEmpty() ? refs.size() : Iterators.size( iterator() );
                        }
                        return size;
                    }
                    @Override
                    public void close() {
                    }
                };
            }
        };
    }


    @Override
    public UnitOfWork newUnitOfWork() {
        checkOpen();
        if (storeUow instanceof CloneCompositeStateSupport) {
            return new UnitOfWorkNested( repo, (CloneCompositeStateSupport)storeUow, this );
        }
        else {
            throw new UnsupportedOperationException( "The current store backend does not support cloning states (nested UnitOfWork): " + storeUow );
        }
    }


    @Override
    public void prepare() throws IOException, ConcurrentEntityModificationException {
        checkOpen();
        try {
            prepareResult = null;
            storeUow.prepareCommit( modified.values() );
            prepareResult = PREPARED;
        }
        catch (ModelRuntimeException e) {
            prepareResult = e;
            throw e;
        }
        catch (IOException e) {
            prepareResult = e;
            throw e;
        }
        catch (Exception e) {
            prepareResult = e;
            throw new ModelRuntimeException( e );
        }
    }


    @Override
    public void commit() throws ModelRuntimeException {
        checkOpen();
        // prepare if not yet done
        if (prepareResult == null) {
            try {
                prepare();
            }
            catch (ModelRuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new ModelRuntimeException( e );
            }
        }
        if (prepareResult != PREPARED) {
            throw new ModelRuntimeException( "UnitOfWork was not successfully prepared for commit." );
        }
        // commit store
        storeUow.commit();
        prepareResult = null;
        
        // reset Entity status
        for (Entry<Object,Entity> entry : loaded) {
            repo.contextOfEntity( entry.getValue() ).resetStatus( EntityStatus.LOADED );
        }
        modified.clear();
    }


    @Override
    public void rollback() throws ModelRuntimeException {
        checkOpen();
        // rollback store
        storeUow.rollback();
        prepareResult = null;
        
        // discard modified Entities
        modified.clear();
        loaded.clear();
    }


    public void close() {
        if (isOpen()) {
            storeUow.close();
            repo = null;
            loaded.clear();
            loaded = null;
            modified.clear();
            modified = null;
        }
    }


    protected void finalize() throws Throwable {
        close();
    }


    public boolean isOpen() {
        return repo != null;
    }

    
    protected final void checkOpen() throws ModelRuntimeException {
        if (!isOpen()) {
            throw new IllegalStateException( "UnitOfWork is closed." );
        }
    }
    
}
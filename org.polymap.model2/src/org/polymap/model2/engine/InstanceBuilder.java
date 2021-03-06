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
package org.polymap.model2.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.cache.configuration.MutableConfiguration;

import org.apache.commons.logging.LogFactory;import org.apache.commons.logging.Log;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.model2.Association;
import org.polymap.model2.AssociationConcern;
import org.polymap.model2.CollectionProperty;
import org.polymap.model2.CollectionPropertyConcern;
import org.polymap.model2.Composite;
import org.polymap.model2.Computed;
import org.polymap.model2.ComputedProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Property;
import org.polymap.model2.PropertyBase;
import org.polymap.model2.PropertyConcern;
import org.polymap.model2.PropertyConcernBase;
import org.polymap.model2.engine.EntityRepositoryImpl.EntityRuntimeContextImpl;
import org.polymap.model2.engine.LoadingCache.Loader;
import org.polymap.model2.runtime.CompositeInfo;
import org.polymap.model2.runtime.EntityRuntimeContext;
import org.polymap.model2.runtime.ModelRuntimeException;
import org.polymap.model2.runtime.PropertyInfo;
import org.polymap.model2.store.CompositeState;
import org.polymap.model2.store.StoreCollectionProperty;
import org.polymap.model2.store.StoreProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class InstanceBuilder {

    private static Log log = LogFactory.getLog( InstanceBuilder.class );

    protected static Field                      contextField;
    
    private static Field                        concernContextField;

    private static Field                        concernDelegateField;
    
    private static Method                       computedPropertyInitMethod;
    
    protected static LoadingCache<Field,List<Class>>   concerns;

    static {
        try {
            SimpleCacheManager cacheManager = new SimpleCacheManager();
            concerns = LoadingCache.create( cacheManager, new MutableConfiguration() );
                    
            contextField = Composite.class.getDeclaredField( "context" );
            contextField.setAccessible( true );

            concernContextField = PropertyConcernBase.class.getDeclaredField( "context" );
            concernContextField.setAccessible( true );
            
            concernDelegateField = PropertyConcernBase.class.getDeclaredField( "delegate" );
            concernDelegateField.setAccessible( true );
            
            computedPropertyInitMethod = ComputedProperty.class.getDeclaredMethod( "init", PropertyInfo.class, Composite.class );
            computedPropertyInitMethod.setAccessible( true );
        }
        catch (Exception e) {
            log.error( "", e );
            throw new RuntimeException( e );
        }
    }
    
    
    // instance *******************************************
    
    private EntityRuntimeContext    context;
    
    
    public InstanceBuilder( EntityRuntimeContext context ) {
        this.context = context;
    }
    
    
    public <T extends Composite> T newComposite( CompositeState state, Class<T> entityClass ) { 
        try {
            // new instance
            Constructor<?> ctor = entityClass.getConstructor( new Class[] {} );
            T instance = (T)ctor.newInstance( new Object[] {} );
            
            // set context
            contextField.set( instance, context );
            
//            // init concerns
//            List<PropertyConcern> concerns = new ArrayList();
//            Concerns concernsAnnotation = entityClass.getAnnotation( Concerns.class );
//            if (concernsAnnotation != null) {
//                for (Class<? extends PropertyConcern> concernClass : concernsAnnotation.value()) {
//                    concerns.add( concernClass.newInstance() );
//                }
//            }

            // init properties
            initProperties( instance, state );
            
            return instance;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ModelRuntimeException( "Error while creating an instance of: " + entityClass, e );
        }
    }
    
    
    /**
     * Initializes all properties of the given Composite, including all super classes.
     * Composite properties are init with {@link CompositePropertyImpl} which comes back to 
     * {@link InstanceBuilder} when the value is accessed.
     */
    protected void initProperties( Composite instance, CompositeState state ) throws Exception {
//        StoreSPI store = context.getRepository().getStore();
        CompositeInfo compositeInfo = context.getRepository().infoOf( instance.getClass() );
        if (compositeInfo == null) {
            log.info( "Mixin type not declared on Entity type: " + instance.getClass().getName() );
            compositeInfo = new CompositeInfoImpl( instance.getClass() );
        }
        assert compositeInfo != null : "No info for Composite type: " + instance.getClass().getName();
        
        Class superClass = instance.getClass();
        while (superClass != null) {
            // XXX cache fields
            for (Field field : superClass.getDeclaredFields()) {
                if (PropertyBase.class.isAssignableFrom( field.getType() )) {
                    field.setAccessible( true );
                    
                    if (field.get( instance ) != null) {
                        log.info( "Property already inistialized, skipping: " + field.getName() );
                        continue;
                    }

                    PropertyInfo info = compositeInfo.getProperty( field.getName() );
                    PropertyBase prop = null;

                    // single property
                    if (Property.class.isAssignableFrom( field.getType() )) {
                        // Computed
                        if (info.isComputed()) {
                            Computed a = ((PropertyInfoImpl)info).getField().getAnnotation( Computed.class );
                            prop = a.value().newInstance();
                            computedPropertyInitMethod.invoke( prop, info, instance );
                            // always check modifications, default value, immutable, nullable
                            prop = new ConstraintsPropertyInterceptor( (Property)prop, (EntityRuntimeContextImpl)context );
                        }
                        else {
                            StoreProperty storeProp = state.loadProperty( info );
                            // Composite
                            if (Composite.class.isAssignableFrom( info.getType() )) {
                                prop = new CompositePropertyImpl( context, storeProp );
                                prop = new ConstraintsPropertyInterceptor( (Property)prop, (EntityRuntimeContextImpl)context );
                            }
                            // primitive type
                            else {
                                prop = new PropertyImpl( storeProp );
                                prop = new ConstraintsPropertyInterceptor( (Property)prop, (EntityRuntimeContextImpl)context );
                            }
                        }
                        // concerns
                        for (PropertyConcernBase concern : fieldConcerns( field, prop )) {
                            prop = concern;
                        }
                    }

                    // Association
                    else if (Association.class.isAssignableFrom( field.getType() )) {
                        assert info.isAssociation();
                        // check Computed
                        if (info.isComputed()) {
                            throw new UnsupportedOperationException( "Computed Association is not supported yet: " + propName( field ));
                        }
                        StoreProperty storeProp = state.loadProperty( info );
                        prop = new AssociationImpl( context, storeProp );
                        prop = new ConstraintsAssociationInterceptor( (Association)prop, (EntityRuntimeContextImpl)context );
                        // concerns
                        for (PropertyConcernBase concern : fieldConcerns( field, prop )) {
                            prop = concern;
                        }
                    }

                    // ManyAssociation
                    else if (ManyAssociation.class.isAssignableFrom( field.getType() )) {
                        assert info.isAssociation();
                        assert info.getMaxOccurs() > 1 : "Field has improper @MaxOccurs: " + propName( field );
                        // check Computed
                        if (info.isComputed()) {
                            throw new UnsupportedOperationException( "Computed ManyAssociation is not supported yet: " + propName( field ));
                        }
                        StoreCollectionProperty storeProp = (StoreCollectionProperty)state.loadProperty( info );
                        prop = new ManyAssociationImpl( context, storeProp );
                        prop = new ConstraintsManyAssociationInterceptor( (ManyAssociation)prop, (EntityRuntimeContextImpl)context );
                        // concerns
                        for (PropertyConcernBase concern : fieldConcerns( field, prop )) {
                            prop = concern;
                        }
                    }

                    // Collection
                    else if (CollectionProperty.class.isAssignableFrom( field.getType() )) {
                        assert info.getMaxOccurs() > 1 : "Field has improper @MaxOccurs: " + propName( field );
                        StoreCollectionProperty storeProp = (StoreCollectionProperty)state.loadProperty( info );
                        // Composite
                        if (Composite.class.isAssignableFrom( info.getType() )) {
                            prop = new CompositeCollectionPropertyImpl( context, storeProp );                            
                        }
                        // primitive type
                        else {
                            prop = new CollectionPropertyImpl( context, storeProp );
                        }
                        if (info.isNullable()) {
                            throw new ModelRuntimeException( "CollectionProperty cannot be @Nullable." );
                        }
                        prop = new ConstraintsCollectionInterceptor( (CollectionProperty)prop, (EntityRuntimeContextImpl)context );
                        // concerns
                        for (PropertyConcernBase concern : fieldConcerns( field, prop )) {
                            prop = concern;
                        }
                    }

                    // set field
                    assert prop != null : "Unable to build property instance for: " + field;
                    field.set( instance, prop );                    
                }
            }
            superClass = superClass.getSuperclass();
        }
    }


    protected String propName( Field field ) {
        return field.getDeclaringClass().getSimpleName() + "#" + field.getName();
    }

    
    protected Iterable<PropertyConcernBase> fieldConcerns( final Field field, final PropertyBase prop ) throws Exception {
        List<Class> concernTypes = concerns.get( field, new Loader<Field,List<Class>>() {
            public List<Class> load( Field key ) {
                List<Class> result = new ArrayList();
                // Class concerns
                Concerns ca = field.getDeclaringClass().getAnnotation( Concerns.class );
                if (ca != null) {
                    result.addAll( Arrays.asList( ca.value() ) );
                }
                // Field concerns
                Concerns fa = field.getAnnotation( Concerns.class );
                if (fa != null) {
                    for (Class<? extends PropertyConcernBase> concern : fa.value()) {
                        result.add( concern );
                    }
                }
                return result;
            }
        });
        
        return Iterables.transform( concernTypes, new Function<Class,PropertyConcernBase>() {
            public PropertyConcernBase apply( Class concernType ) {
                try {
                    // early check concern type
                    if (Property.class.isAssignableFrom( field.getType() )
                            && !PropertyConcern.class.isAssignableFrom( concernType )) {
                        throw new ModelRuntimeException( "Concerns of Property have to extend PropertyConcern: " + concernType.getName() + " @ " + field.getName() );
                    }
                    else if (CollectionProperty.class.isAssignableFrom( field.getType() )
                            && !CollectionPropertyConcern.class.isAssignableFrom( concernType )) {
                        throw new ModelRuntimeException( "Concerns of CollectionProperty have to extend CollectionPropertyConcern: " + concernType.getName() + " @ " + field.getName() );
                    }
                    else if (Association.class.isAssignableFrom( field.getType() )
                            && !AssociationConcern.class.isAssignableFrom( concernType )) {
                        throw new ModelRuntimeException( "Concerns of Association have to extend AssociationConcern: " + concernType.getName() + " @ " + field.getName() );
                    }

                    // create concern
                    PropertyConcernBase concern = (PropertyConcernBase)concernType.newInstance();
                    concernContextField.set( concern, context );
                    concernDelegateField.set( concern, prop );
                    
                    return concern;
                } 
                catch (ModelRuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new ModelRuntimeException( "Error while initializing concern: " + concernType + " (" + e.getLocalizedMessage() + ")", e );
                }
            }
        });
    }
    
}

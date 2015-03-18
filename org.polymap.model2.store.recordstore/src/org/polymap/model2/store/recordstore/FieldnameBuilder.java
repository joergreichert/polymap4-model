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
package org.polymap.model2.store.recordstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.Composite;
import org.polymap.model2.Property;

/**
 * Builder XPAth like fieldnames containing composites and array elements. Such
 * fieldnames are used to map complex {@link Composite} structure to flat Lucene
 * document fields.
 * <p/>
 * <b>Example:</b> 
 * <pre>
 * entity_name/composite/array[x]/another_composite
 * </pre>
 * <p/>
 * Instances actually build fieldnames on demand when {@link #get()} is called,
 * saving memory and cycles for {@link Property}s which are never actually requested.
 * Results are cached to speed up frequently used Properties.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class FieldnameBuilder {

    private static Log log = LogFactory.getLog( FieldnameBuilder.class );
    
    public static final String      COMPOSITE_DELIMITER = "/";
    public static final String      ARRAY_SIZE = "__size__";

    /**
     * 
     */
    public static final FieldnameBuilder EMPTY = new FieldnameBuilder( null ) {
        @Override
        protected StringBuilder build() {
            return new StringBuilder( 128 );
        }
    };
    
    // instance *******************************************
    
    protected FieldnameBuilder      parent;
    
    /**
     * The result of {@link #build()}, cached after first run.
     * <p/>
     * Not synchronized: save memory; concurrent build()s are ok
     */
    private String                  resultCache; 
    

    protected FieldnameBuilder( FieldnameBuilder parent ) {
        this.parent = parent;
    }

    public FieldnameBuilder composite( String part ) {
        return new CompositeBuilder( this, part );
    }
    
    public FieldnameBuilder arrayElement( int index ) {
        return new ArrayBuilder( this, index );
    }
    
    public FieldnameBuilder arraySize() {
        return composite( ARRAY_SIZE );
    }
    
    protected abstract StringBuilder build();
    
    public String get() {
        if (resultCache == null) {
            resultCache = build().toString();
        }
        return resultCache;
    }

    @Override
    public String toString() {
        return get();
    }

    
    /**
     * 
     */
    protected static class CompositeBuilder
            extends FieldnameBuilder {

        private String          compositeName;

        protected CompositeBuilder( FieldnameBuilder parent, String compositeName ) {
            super( parent );
            assert compositeName != null;
            this.compositeName = compositeName;
        }

        @Override
        protected StringBuilder build() {
            StringBuilder buf = parent.build();
            if (buf.length() > 0) {
                buf.append( COMPOSITE_DELIMITER );
            }
            return buf.append( compositeName );
        }
    }

    /**
     * 
     */
    protected static class ArrayBuilder
            extends FieldnameBuilder {

        private int             index;
        
        protected ArrayBuilder( FieldnameBuilder parent, int index ) {
            super( parent );
            this.index = index;
        }

        @Override
        protected StringBuilder build() {
            StringBuilder buf = parent.build();
            return buf.append( '[' ).append( index ).append( ']' );
        }
    }
    
}

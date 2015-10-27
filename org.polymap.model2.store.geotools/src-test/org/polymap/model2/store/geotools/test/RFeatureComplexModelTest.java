/* 
 * polymap.org
 * Copyright (C) 2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.model2.store.geotools.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.rs.RDataStore;
import org.polymap.core.data.rs.lucene.LuceneQueryDialect;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.store.geotools.FeatureStoreAdapter;
import org.polymap.model2.test.Company;
import org.polymap.model2.test.ComplexModelTest;
import org.polymap.model2.test.Employee;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class RFeatureComplexModelTest
        extends ComplexModelTest {

    private static Log log = LogFactory.getLog( RFeatureComplexModelTest.class );

    protected RDataStore                ds;

    private FeatureStoreAdapter         store;
    
    
    public RFeatureComplexModelTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        
        LuceneRecordStore lucenestore = new LuceneRecordStore();
        ds = new RDataStore( lucenestore, new LuceneQueryDialect() );
        store = new FeatureStoreAdapter( ds );
        repo = EntityRepository.newConfiguration()
                .store.set( store )
                .entities.set( new Class[] {Employee.class, Company.class} )
                .create();
        uow = repo.newUnitOfWork();
    }


    @Override
    public void testAssociation() {
        assertTrue( "No Associations yet!", false );
    }


    @Override
    public void testCompositeCollection() {
        assertTrue( "No Composite Collections yet!", false );
    }

    
}

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
package org.polymap.model2.runtime.config;

import java.lang.reflect.Field;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConfigurationFactory {

    /**
     * Creates a new configuration of the given type.
     */
    public static <T> T create( Class<T> cl ) throws ConfigurationException {
        try {
            // create instance
            T instance = cl.newInstance();
            
            // init properties
            for (Field f : cl.getDeclaredFields()) {
                f.setAccessible( true );
                Property prop = new Property() {

                    private Object      value;
                    
                    @Override
                    public Object set( Object newValue ) {
                        this.value = newValue;
                        return instance;
                    }

                    @Override
                    public Object setAndGet( Object newValue ) {
                        throw new RuntimeException( "not yet implemented." );
                    }

                    @Override
                    public Object get() {
                        if (value == null && f.getAnnotation( Mandatory.class ) != null) {
                            throw new ConfigurationException( "Configuration property is @Mandatory: " + f.getName() );
                        }
                        DefaultValue defaultValue = f.getAnnotation( DefaultValue.class );
                        if (value == null && defaultValue != null) {
                            return defaultValue.value();
                        }
                        DefaultDouble defaultDouble = f.getAnnotation( DefaultDouble.class );
                        if (value == null && defaultDouble != null) {
                            return defaultDouble.value();
                        }
                        DefaultInt defaultInt = f.getAnnotation( DefaultInt.class );
                        if (value == null && defaultInt != null) {
                            return defaultInt.value();
                        }
                        DefaultBoolean defaultBoolean = f.getAnnotation( DefaultBoolean.class );
                        if (value == null && defaultBoolean != null) {
                            return defaultBoolean.value();
                        }
                        return value;
                    }
                };
                f.set( instance, prop );
            }
            return instance;
        }
        catch (Exception e) {
            throw new ConfigurationException( e );
        }
    }
    
}

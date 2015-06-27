/*
 * Copyright 2014 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.tamu.tcat.osgi.config;

/**
 * A service API to access some scope of configuration properties.
 */
public interface ConfigurationProperties
{
   /**
    * Evaluate a configuration property as the given type and return it. Some configuration
    * systems store all property values as strings which may be interpreted several ways, such
    * as numeric or deserialized into complex types. As much as this instance is able, it will
    * attempt to interpret the stored value for the named property by the type provided and
    * return a value of the type requested.
    * <p>
    * If the value cannot be converted to the requested type, an exception is thrown.
    * 
    * @param <T> The value type of the property
    * @param name The name of the property to retrieve
    * @param type The type of value to return.
    * @return The type-interpreted value of the property, which may be {@code null}
    * @throws IllegalStateException If there is an error evaluating the property value.
    */
   <T> T getPropertyValue(String name, Class<T> type) throws IllegalStateException;
   
   /**
    * Evaluate a configuration property as the given type and return it. Some configuration
    * systems store all property values as strings which may be interpreted several ways, such
    * as numeric or deserialized into complex types. As much as this instance is able, it will
    * attempt to interpret the stored value for the named property by the type provided and
    * return a value of the type requested.
    * <p>
    * The provided {@code defaultValue} is returned if the property value is undefined or {@code null}.
    * <p>
    * If the value cannot be converted to the requested type, an exception is thrown.
    * 
    * @param <T> The value type of the property
    * @param name The name of the property to retrieve
    * @param type The type of value to return.
    * @param defaultValue The value to return if the property can not be resolved. This value may be {@code null}.
    * @return The type-interpreted value of the property or the provided {@code defaultValue}, both of which may be {@code null}
    */
   <T> T getPropertyValue(String name, Class<T> type, T defaultValue) throws IllegalStateException;
}

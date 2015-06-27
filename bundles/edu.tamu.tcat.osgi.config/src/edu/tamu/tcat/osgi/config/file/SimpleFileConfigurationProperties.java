/*
 * Copyright 2014-2015 Texas A&M Engineering Experiment Station
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

package edu.tamu.tcat.osgi.config.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.internal.Activator;

/**
 * An implementation of the {@link ConfigurationProperties} service API.
 * This implementation is intended to be used as an OSGI Declarative Service with single
 * DS property value defined: {@code props.file.propertyName}. This parameter should be set
 * to a system or OSGI framework property name. The value of that property should be specified
 * in the launch configuration for the application and be application-specific.
 * <p>
 * For example, in an application, add an OSGI DS config which adds a property:
 * {@code props.file.propertyName=edu.tamu.tcat.appname.config.file}.
 * <p>
 * Then, in the launch
 * configuration for the app, define this property to be loaded into JVM System Properties or
 * OSGI Framework Properties: {@code edu.tamu.tcat.appname.config.file=/path/to/app/config.properties}.
 * <p>
 * The two-stage property evaluation allows any application the ability to not only define its own
 * property key for the configuration file to load but also to allow for multiple services each of
 * which can refer to its own configuration file.
 * <p>
 * This implementation also provides some API for manually setting and managing properties not
 * defined by a config file.
 */
public class SimpleFileConfigurationProperties implements ConfigurationProperties
{
   private static final Logger debug = Logger.getLogger("edu.tamu.tcat.osgi.config.file.simple");

   /**
    * The value of this property specifies an application-specific bundle or system property name
    * which has a value identifying the file system path where properties are.
    */
   public static final String PROP_FILE = "props.file.propertyName";

   //@GuardedBy("this")
   private Properties allProps;
   //@GuardedBy("this")
   private Path propsFile;
   private Map<String, Object> params;

   // called by DS
   public void activate(Map<String,Object> params)
   {
      this.params = params;
      String filePropName = (String)params.get(PROP_FILE);
      Objects.requireNonNull(filePropName, "Missing required property '"+PROP_FILE+"'");
      loadProperties(filePropName);
   }

   // called by DS
   public void dispose()
   {
      synchronized (this)
      {
         allProps = null;
      }
   }

   /**
    * Force properties to be reloaded from file. This is typically used when the file was edited
    * outside the scope of the application.
    * @since 1.1
    */
   public void reloadProperties()
   {
      if (params == null || params.isEmpty())
         throw new IllegalStateException("No initialization parameters available");
      String filePropName = (String)params.get(PROP_FILE);
      Objects.requireNonNull(filePropName, "Missing required property '"+PROP_FILE+"'");
      loadProperties(filePropName);
   }

   private void loadProperties(String filePropName)
   {
      try
      {
         BundleContext bc = Activator.getDefault().getContext();
         String propsFileStr = bc.getProperty(filePropName);
         if (propsFileStr == null)
            throw new IllegalStateException("Value of '"+filePropName+"' was ["+propsFileStr+"]");

         Path p = Paths.get(propsFileStr);
         if (!Files.exists(p))
            throw new IllegalStateException("File not found [" + p + "]");

         Properties props = loadProperties(p);
         synchronized (this)
         {
            propsFile = p;
            allProps = props;
            debug.log(Level.INFO, "Loaded ("+allProps.size()+") properties from " + propsFile);
         }
      }
      catch (Exception e)
      {
         debug.log(Level.SEVERE, "Failed loading properties. ", e);
         synchronized (this)
         {
            allProps = new Properties();
         }
      }
   }

   @Override
   public <T> T getPropertyValue(String name, Class<T> type, T defaultValue)
   {
      try
      {
         T val = getPropertyValue(name, type);
         if (val == null)
            return defaultValue;
         return val;
      }
      catch (Exception pe)
      {
         debug.log(Level.WARNING, "Failed processing property value for [" + name + "], returning default", pe);
         return defaultValue;
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T getPropertyValue(String name, Class<T> type)
   {
      Objects.requireNonNull(name, "property name is null");
      Objects.requireNonNull(type, "property type is null");

      String str;
      synchronized (this)
      {
         if (allProps == null)
            throw new IllegalStateException("Not initialized");

         str = allProps.getProperty(name);
      }

      if (str == null)
         return null;
      if (type.isInstance(str))
         return (T)str;

      try
      {
         if (Number.class.isAssignableFrom(type))
         {
            if (Byte.class.isAssignableFrom(type))
               return (T)Byte.valueOf(str);
            if (Short.class.isAssignableFrom(type))
               return (T)Short.valueOf(str);
            if (Integer.class.isAssignableFrom(type))
               return (T)Integer.valueOf(str);
            if (Long.class.isAssignableFrom(type))
               return (T)Long.valueOf(str);
            if (Float.class.isAssignableFrom(type))
               return (T)Float.valueOf(str);
            if (Double.class.isAssignableFrom(type))
               return (T)Double.valueOf(str);

            throw new IllegalStateException("Unhandled numeric type ["+type+"] for property ["+name+"] value ["+str+"]");
         }

         if (Boolean.class.isAssignableFrom(type))
            return (T)Boolean.valueOf(str);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalStateException("Failed converting property ["+name+"] value ["+str+"] to primitive "+type, e);
      }

      try
      {
         if (Path.class.isAssignableFrom(type))
         {
            return (T)Paths.get(str);
         }
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed converting property ["+name+"] value ["+str+"] to OS file system path "+type, e);
      }

      try
      {
         if (URI.class.isAssignableFrom(type))
         {
            return (T)new URI(str);
         }
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed converting property ["+name+"] value ["+str+"] to URI "+type, e);
      }

      throw new IllegalStateException("Unhandled type: " + type.getCanonicalName());
   }

   private Properties loadProperties(Path filePath) throws Exception
   {
      debug.info("Loading properties file from: " + filePath);

      Properties props = new Properties();
      try (InputStream str = Files.newInputStream(filePath))
      {
         props.load(str);
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Unable to load properties file as stream "+filePath, e);
      }

      return props;
   }

   // internal method, not part of public api
   public void setProperties(Map<String, String> newProps)
   {
      //TODO: allow revert if operation failes
      synchronized (this)
      {
         if (propsFile == null)
            throw new IllegalStateException("Properties not specified by file, write is not allowed");

         if (allProps == null)
            throw new IllegalStateException("Not initialized");

         allProps.clear();
         allProps.putAll(newProps);
         debug.info("Writing properties File: "+propsFile);
         try (OutputStream out = Files.newOutputStream(propsFile))
         {
            allProps.store(out, null);
         }
         catch (Exception e)
         {
            debug.log(Level.SEVERE, "Failed writing properties file to ["+ propsFile + "]", e);
         }
      }
   }

   // internal method, not part of public api
   public void setProperty(String k, String v)
   {
      //TODO: allow revert if operation failes
      synchronized (this)
      {
         if (propsFile == null)
            throw new IllegalStateException("Properties not specified by file, write is not allowed");

         if (k == null || k.trim().isEmpty())
            throw new IllegalArgumentException("key is not valid");

         boolean isDelete = (v == null || v.trim().isEmpty());

         if (allProps == null)
            throw new IllegalStateException("Not initialized");

         if (isDelete)
            allProps.remove(k);
         else
            allProps.put(k, v);

         debug.info("Writing properties File: "+propsFile);
         try (OutputStream out = Files.newOutputStream(propsFile))
         {
            allProps.store(out, null);
         }
         catch (Exception e)
         {
            debug.log(Level.SEVERE, "Failed writing properties file to ["+ propsFile + "]", e);
         }
      }
   }

   // internal method, not part of public api
   public Map<String, String> getAllProps()
   {
      Map<String,String> props = new HashMap<String, String>();
      synchronized (this)
      {
         for (Object o : allProps.keySet()) {
            String key = String.valueOf(o);
            props.put(key, allProps.getProperty(key));
         }
      }
      return Collections.unmodifiableMap(props);
   }
}

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

package edu.tamu.tcat.osgi.services.util;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A base implementation to use for bundle activators. This exposes access to
 * framework/system properties and has an accessor for the current {@link BundleContext}
 * which can be used to look up OSGi services.
 * <p>
 * This implementation contains no static fields or methods because they are inherently
 * unsafe in this particular environment of bundle activators; i.e. a static field would
 * be a JVM singleton if multiple activators extend from this same base type.
 */
/*
 * This base class does not maintain any static 'instance' fields.
 * The problem is that subclasses must each define a static accessor for 'instance' not
 * only to avoid warnings about static access to the wrong class but also because the static
 * instance in the base class *is a singleton for the JVM* and is overwritten by the last
 * bundle to load that uses this as a base class.
 * 
 * If a subclass 'Activator' - used internally in a bundle in which it is defined - needs
 * a static accessor to return itslef, it must maintain that static field to refer to
 * the correct instance and a static accessor must be defined explicitly on any subclass
 * on which this functionality is necessary.
 * 
 * The pattern to use should be to declare a static field, a constructor which assigns
 * 'this' to that field, and a static accessor for the field:
 * <pre>
   private static Activator instance;
   public Activator() {
      instance = this;
   }
   public static Activator getDefault() {
      return instance;
   }
 * </pre>
 */
public class ActivatorBase implements BundleActivator
{
   /**
    * @since 1.1
    */
   protected BundleContext context;
   
   public BundleContext getContext()
   {
      return context;
   }

   @Override
   public void start(BundleContext bundleContext) throws Exception
   {
      context = bundleContext;
   }

   @Override
   public void stop(BundleContext bundleContext) throws Exception
   {
      context = null;
   }

   /*
    * Access all properties through the context in case they are defined as bundle properties
    * (will fallback to system properties). See context.getProperty().
    */
   public String getProperty(String name, String defaultValue)
   {
      String value = getContext().getProperty(name);
      return (value != null) ? value : defaultValue;
   }
}

/*******************************************************************************
 * Copyright © 2008-14, All Rights Reserved
 * Texas Center for Applied Technology
 * Texas A&M Engineering Experiment Station
 * The Texas A&M University System
 * College Station, Texas, USA 77843
 *
 * Proprietary information, not for redistribution.
 ******************************************************************************/

package edu.tamu.tcat.osgi.services.util;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A base implementation to use for bundle activators. This exposes access to
 * framework/system properties and has an accessor for the current {@link BundleContext}
 * which can be used to look up OSGi services.
 */
public class ActivatorBase implements BundleActivator
{
   /**
    * @since 1.1
    */
   protected BundleContext context;
   
   /**
    * @since 1.1
    */
   protected static ActivatorBase instance;

   public ActivatorBase()
   {
      instance = this;
   }

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

   /*
    * This method is not public. The problem is that subclasses must each define a static
    * accessor for 'instance' to avoid warnings about static access to the wrong class.
    * 
    * If a subclass 'Activator' - used internally in a bundle in which it is defined - is
    * invoked by 'Activator.getDefault()' intending to invoke this method (which is static in its
    * superclass), the method is not actually on 'Activator', but on this class, 'ActivatorBase'.
    * To alleviate this problem, the static accessor must be defined explicitly on any subclass
    * on which this functionality is necessary.
    * 
    * This method is left protected rather than being removed to retain this notice, and also to
    * allow subclasses to invoke it in implementing their own 'getDefault()' even though 'instance'
    * is 'protected' and directly available to subclasses.
    * 
    * In a subclass implementation, the declared return type may either be this type,
    * 'ActivatorBase', or that specific activator sub-type, which allows access to further methods
    * defined in the sub-type.
    */
   protected static ActivatorBase getDefault()
   {
      return instance;
   }
}

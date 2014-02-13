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

public class ActivatorBase implements BundleActivator
{
   private BundleContext context;
   private static ActivatorBase instance;
   
   public ActivatorBase()
   {
      instance = this;
   }
   
   public BundleContext getContext() {
      return context;
   }

   public void start(BundleContext bundleContext) throws Exception {
      context = bundleContext;
   }

   public void stop(BundleContext bundleContext) throws Exception {
      context = null;
   }
   
   /* We get all properties from the context in case they are defined as bundle properties
    *  (will fallback to system properties). See context.getProperty(). */
   public String getProperty(String name, String defaultValue)
   {
      String value = getContext().getProperty(name);
      return (value != null) ? value : defaultValue;
   }

   public static ActivatorBase getDefault()
   {
      return instance;
   }
}

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A utility to manage accessing an OSGI service. Instantiate with a {@link BundleContext} and query
 * for services.
 * <p>
 * Instances are not thread-safe and should not be shared.<p>
 * Instances must be closed to release OSGI resources. This implies that any objects provided
 * by the service may no longer be available once the lifecycle of this helper ends.
 */
public class ServiceHelper implements AutoCloseable
{
   private Set<ServiceReference<?>> refs;
   private final BundleContext context;

   public ServiceHelper(BundleContext context)
   {
      Objects.requireNonNull(context, "BundleContext is null");
      this.context = context;
      refs = new HashSet<ServiceReference<?>>();
   }

   /**
    * Retrieves an OSGi service. If multiple are registered
    * under the same type, one of them will be returned.
    * 
    * @param type The type of service to retrieve.
    * @return A service registered under the given type. Will not be {@code null}
    * @throws RuntimeException If a service of the given type could not be found.
    */
   public <T> T getService(Class<T> type)
   {
      if (refs == null)
         throw new IllegalStateException("Service helper is disposed.");
      ServiceReference<T> ref = context.getServiceReference(type);
      if (ref != null) {
         refs.add(ref);
         T svc = context.getService(ref);
         if (svc != null)
            return svc;
      }
      throw new IllegalStateException("Service [" + type.getCanonicalName() + "] is not available.");
   }
   
   /**
    * Retrieves an OSGi service using the given filter expression. If multiple are registered
    * under the same type, one of them will be returned.
    * <p>
    * This method will block (with a busy-wait)
    * until the timeout expires or a service is available.
    * 
    * @param type The type of service to retrieve.
    * @param timeout_ms The time in milliseconds to wait before aborting
    * @return A service of the given type. Will not be {@code null}
    * @throws RuntimeException If a service of the given type could not be found with the given criteria.
    */
   public <T> T waitForService(Class<T> type, String filter, long timeout_ms)
   {
      if (refs == null)
         throw new IllegalStateException("Service helper is disposed.");
      Collection<ServiceReference<T>> ref = null;
      long startTime = System.currentTimeMillis();
      do {
         try {
            ref = context.getServiceReferences(type,filter);
         } catch (Exception e) {
            throw new IllegalStateException("Failed accessing service reference ["+type+"]["+filter+"]",e);
         }
         if (ref != null && !ref.isEmpty()) {
            ServiceReference<T> first = ref.iterator().next();
            refs.add(first);
            T svc = context.getService(first);
            if (svc != null)
               return svc;
         }
         try
         {
            Thread.sleep(20);
         }
         catch (InterruptedException e)
         {
            throw new IllegalStateException(e);
         }
         if ((System.currentTimeMillis()-startTime) > timeout_ms)
            break;
      } while (ref == null || ref.isEmpty());
      throw new IllegalStateException("Service [" + type.getCanonicalName() + "] is not available.");
   }
   
   /**
    * Retrieves an OSGi service using the given filter expression. If multiple are registered
    * under the same type, one of them will be returned.
    * <p>
    * This method will block (with a busy-wait)
    * until the timeout expires or a service is available.
    * 
    * @param type The type of service to retrieve.
    * @param timeout_ms The time in milliseconds to wait before aborting
    * @return A service of the given type. Will not be {@code null}
    * @throws RuntimeException If a service of the given type could not be found with the given criteria.
    */
   public <T> T waitForService(Class<T> type, long timeout_ms)
   {
      if (refs == null)
         throw new IllegalStateException("Service helper is disposed.");
      ServiceReference<T> ref = null;
      long startTime = System.currentTimeMillis();
      do {
         ref = context.getServiceReference(type);
         if (ref != null) {
            refs.add(ref);
            T svc = context.getService(ref);
            if (svc != null)
               return svc;
         }
         try
         {
            Thread.sleep(20);
         }
         catch (InterruptedException e)
         {
            throw new IllegalStateException(e);
         }
         if ((System.currentTimeMillis()-startTime) > timeout_ms)
            break;
      } while (ref == null);
      throw new IllegalStateException("Service [" + type.getCanonicalName() + "] is not available.");
   }

   @Override
   public void close()
   {
      if (refs != null)
      {
         for (ServiceReference<?> sr : refs)
         {
            context.ungetService(sr);
         }
      }
      refs = null;
   }
}

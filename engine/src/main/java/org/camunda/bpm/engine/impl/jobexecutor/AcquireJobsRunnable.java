/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AcquireJobsRunnable implements Runnable {

  private static Logger log = Logger.getLogger(AcquireJobsRunnable.class.getName());

  protected final JobExecutor jobExecutor;

  protected volatile boolean isInterrupted = false;
  protected volatile boolean isJobAdded = false;
  protected final Object MONITOR = new Object();
  protected final AtomicBoolean isWaiting = new AtomicBoolean(false);

  public AcquireJobsRunnable(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  protected void suspendAcquisition(long millis) {
    if (millis <= 0) {
      return;
    }

    try {
      log.fine("job acquisition thread sleeping for " + millis + " millis");
      synchronized (MONITOR) {
        if(!isInterrupted) {
          isWaiting.set(true);
          MONITOR.wait(millis);
        }
      }
      log.fine("job acquisition thread woke up");
      isJobAdded = false;
    } catch (InterruptedException e) {
      log.fine("job acquisition wait interrupted");
    } finally {
      isWaiting.set(false);
    }
  }

  public void stop() {
    synchronized (MONITOR) {
      isInterrupted = true;
      if(isWaiting.compareAndSet(true, false)) {
        MONITOR.notifyAll();
      }
    }
  }

  public void jobWasAdded() {
    isJobAdded = true;
    if(isWaiting.compareAndSet(true, false)) {
      // ensures we only notify once
      // I am OK with the race condition
      synchronized (MONITOR) {
        MONITOR.notifyAll();
      }
    }
  }

  public boolean isJobAdded() {
    return isJobAdded;
  }
}

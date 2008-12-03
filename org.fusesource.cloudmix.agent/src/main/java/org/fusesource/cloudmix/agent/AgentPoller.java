/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.cloudmix.agent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A polling spring bean which can poll any of the available agents such
 * as {@link InstallerAgent}
 *
 * @version $Revision: 1.1 $
 */
public class AgentPoller implements InitializingBean, DisposableBean {
    private static final transient Log LOG = LogFactory.getLog(AgentPoller.class);
    private Callable<Object> agent = new InstallerAgent();
    private Timer timer;
    private long pollingPeriod = 1000L;
    private long initialPollingDelay = 500L;

    public AgentPoller() {
    }

    public AgentPoller(Callable<Object> agent) {
        this.agent = agent;
    }

    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws Exception {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                agentPoll();
            }
        }, initialPollingDelay, pollingPeriod);
    }

    public void agentPoll() {
        try {
            agent.call();
        } catch (Exception e) {
            LOG.warn("Caught exception while polling Agent: ", e);
        }
    }

    public void destroy() throws Exception {
        timer.cancel();
    }

    // Properties
    //-------------------------------------------------------------------------

    public long getPollingPeriod() {
        return pollingPeriod;
    }

    public void setPollingPeriod(long pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

    public long getInitialPollingDelay() {
        return initialPollingDelay;
    }

    public void setInitialPollingDelay(long initialPollingDelay) {
        this.initialPollingDelay = initialPollingDelay;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Callable<Object> getAgent() {
        return agent;
    }

    public void setAgent(Callable<Object> agent) {
        this.agent = agent;
    }
}

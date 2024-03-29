/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.server.quorum;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.server.SessionTracker;
import org.apache.zookeeper.server.SessionTrackerImpl;
import org.apache.zookeeper.server.ZooKeeperServerListener;

/**
 * This is really just a shell of a SessionTracker
 * that tracks session activity to be forwarded to the Leader using a PING.
 */
public class LearnerSessionTracker implements SessionTracker {

    SessionExpirer                   expirer;
    /** SessionId - Timeout */
    ConcurrentHashMap<Long, Integer> sessionsWithTimeouts;
    /** SessionId -  */
    HashMap<Long, Integer>           touchTable           = new HashMap<Long, Integer>();

    /** TODO 这里为什么要设置ServerId = 1？ */
    long serverId = 1;
    /** 这里不要理会初始值，在构造方法里，会根据serverId和时间戳来生成 */
    long nextSessionId = 0;

    /** 构造方法 */
    public LearnerSessionTracker(SessionExpirer expirer, ConcurrentHashMap<Long, Integer> sessionsWithTimeouts,
                                 long serverId, ZooKeeperServerListener listener) {
        this.expirer = expirer;
        this.sessionsWithTimeouts = sessionsWithTimeouts;
        this.serverId = serverId;
        nextSessionId = SessionTrackerImpl.initializeNextSession(this.serverId);
    }

    public void shutdown() {  }

    synchronized public void removeSession(long sessionId) {
        sessionsWithTimeouts.remove(sessionId);
        touchTable.remove(sessionId);
    }

    synchronized public void addSession(long sessionId, int sessionTimeout) {
        sessionsWithTimeouts.put(sessionId, sessionTimeout);
        touchTable.put(sessionId, sessionTimeout);
    }

    synchronized public boolean touchSession(long sessionId, int sessionTimeout) {
        touchTable.put(sessionId, sessionTimeout);
        return true;
    }

    synchronized HashMap<Long, Integer> snapshot() {
        HashMap<Long, Integer> oldTouchTable = touchTable;
        touchTable = new HashMap<Long, Integer>();
        return oldTouchTable;
    }

    synchronized public long createSession(int sessionTimeout) {
        return (nextSessionId++);
    }


    public void checkSession(long sessionId, Object owner)  {
        // Nothing to do here. Sessions are checked at the Leader
    }
    
    public void setOwner(long sessionId, Object owner) {
        // Nothing to do here. Sessions are checked at the Leader
    }

    public void dumpSessions(PrintWriter writer) {
    	// the original class didn't have tostring impl, so just
    	// dup what we had before
    	writer.println(toString());
    }

    public void setSessionClosing(long sessionId) {
        // Nothing to do here.
    }
}

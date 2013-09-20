package com.continuuity.common.zookeeper.election;

import com.continuuity.weave.common.Cancellable;
import com.continuuity.weave.zookeeper.NodeChildren;
import com.continuuity.weave.zookeeper.OperationFuture;
import com.continuuity.weave.zookeeper.ZKClient;
import com.continuuity.weave.zookeeper.ZKClients;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Performs leader election as specified in
 * <a href="http://zookeeper.apache.org/doc/trunk/recipes.html#sc_leaderElection">Zookeeper recipes</a>.
 */
public class SimpleLeaderElection implements Cancellable {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleLeaderElection.class);
  static final String NAMESPACE = "/simple_election";

  private final String guid = UUID.randomUUID().toString();

  private final ZKClient zkClient;
  private final String zkFolderPath;
  private final ElectionHandler handler;
  private final AtomicBoolean cancelled = new AtomicBoolean(false);

  private volatile String zkNodePath = null;
  private final AtomicBoolean leader = new AtomicBoolean(false);

  public SimpleLeaderElection(ZKClient zkClient, String prefix, ElectionHandler handler) {
    this.zkClient = ZKClients.namespace(zkClient, NAMESPACE);
    this.zkFolderPath = prefix.startsWith("/") ? prefix : "/" + prefix;
    this.handler = handler;

    LOG.info("Using guid {}", guid);

    register();
    zkClient.addConnectionWatcher(new ConnectionWatcher());
  }

  @Override
  public void cancel() {
    cancelled.set(true);
    deleteNode(true);
  }

  private void register() {
    // Register for election
    final String path = String.format("%s/%s-", zkFolderPath, guid);
    LOG.debug("Registering for election {} with path {}", zkFolderPath, path);

    OperationFuture<String> createFuture =
      zkClient.create(path, null, CreateMode.EPHEMERAL_SEQUENTIAL, true);
    Futures.addCallback(createFuture,
                        new FutureCallback<String>() {
                          @Override
                          public void onSuccess(String result) {
                            LOG.debug("Created zk node {}", result);
                            zkNodePath = result;
                            runElection();
                          }

                          @Override
                          public void onFailure(Throwable t) {
                            LOG.error("Got exception during node creation for folder {}", path, t);
                            error(t);
                          }
                        });
  }

  private void runElection() {
    LOG.debug("Running election for {}", zkNodePath);

    OperationFuture<NodeChildren> childrenFuture = zkClient.getChildren(zkFolderPath);
    Futures.addCallback(childrenFuture,
                        new FutureCallback<NodeChildren>() {
                          @Override
                          public void onSuccess(NodeChildren result) {
                            List<String> childPaths = result.getChildren();
                            long selfSeqId = -1;
                            TreeMap<Long, String> childrenMap = new TreeMap<Long, String>();
                            for (String path : childPaths) {
                              long seqId = getSequenceId(path);
                              LOG.debug("Got child = {}, seqId = {}", path, seqId);
                              childrenMap.put(seqId, zkFolderPath + "/" + path);

                              if (path.startsWith(guid)) {
                                LOG.debug("Self path = {}", path);
                                selfSeqId = seqId;
                                zkNodePath = childrenMap.get(selfSeqId);
                              }
                            }

                            if (selfSeqId == -1) {
                              String message = String.format("Cannot find self path %s", zkFolderPath);
                              LOG.error(message);
                              error(new IllegalStateException(message));
                            }

                            LOG.debug("Current leader is {}", childrenMap.firstEntry().getValue());

                            if (selfSeqId == childrenMap.firstKey()) {
                              // elected leader
                              executeElected();
                              watchNode(zkNodePath, new LeaderWatcher());
                            } else {
                              // watch lower node
                              Map.Entry<Long, String> watchEntry = childrenMap.lowerEntry(selfSeqId);
                              watchNode(watchEntry.getValue(), new LowerNodeWatcher());
                            }
                          }

                          @Override
                          public void onFailure(Throwable t) {
                            LOG.error("Got exception during children fetch for {}", zkFolderPath, t);
                            error(t);
                          }
                        });

  }

  private void executeElected() {
    LOG.debug("Executing elected handler for {}", zkNodePath);

    try {
      handler.elected();
      leader.set(true);
    } catch (Throwable e) {
      LOG.error("Elected handler exception for {}", zkNodePath, e);
      error(e);
    }
  }

  private void watchNode(final String nodePath, Watcher watcher) {
    OperationFuture<Stat> watchFuture =
      zkClient.exists(nodePath, watcher);
    Futures.addCallback(watchFuture,
                        new FutureCallback<Stat>() {
                          @Override
                          public void onSuccess(Stat result) {
                            // Nothing to do
                          }

                          @Override
                          public void onFailure(Throwable t) {
                            LOG.error("Exception while setting watch on node {} for {}",
                                      nodePath, zkNodePath);
                            error(t);
                          }
                        });
  }

  private void deleteNode(final boolean propagateError) {
    if (zkNodePath != null) {
      OperationFuture<String> deleteFuture = zkClient.delete(zkNodePath);
      Futures.addCallback(deleteFuture, new FutureCallback<String>() {
        @Override
        public void onSuccess(String result) {
          // Nothing to do
        }

        @Override
        public void onFailure(Throwable t) {
          LOG.error("Got exception while deleting node {}", zkNodePath, t);
          if (propagateError) {
            handler.error(t);
          }
        }
      });
    }
  }

  private static long getSequenceId(String zkPath) {
    int ind = zkPath.lastIndexOf('-');

    if (ind == zkPath.length() - 1 || ind == -1) {
      String message = String.format("No sequence ID found in zkPath %s", zkPath);
      LOG.error(message);
      throw new IllegalStateException(message);
    }

    return Long.parseLong(zkPath.substring(ind + 1));
  }

  private void error(Throwable t) {
    cancelled.set(true);
    deleteNode(false);
    handler.error(t);
  }

  /**
   * Watches lower node.
   */
  private class LowerNodeWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
      if (event.getType() == Event.EventType.NodeDeleted && !cancelled.get()) {
        LOG.debug("Lower node deleted {} for election {}", event, zkNodePath);
        runElection();
      }
    }
  }

  /**
   * Watches leader.
   */
  private class LeaderWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
      if (event.getType() == Event.EventType.NodeDeleted) {
        LOG.debug("Leader deleted {}", event, zkNodePath);
        handler.unelected();
      }
    }
  }

  /**
   * Watches zookeeper connection.
   */
  private class ConnectionWatcher implements Watcher {
    private final AtomicBoolean expired = new AtomicBoolean(false);
    private final AtomicBoolean disconnect = new AtomicBoolean(false);

    @Override
    public void process(WatchedEvent event) {
      if (event.getState() == Event.KeeperState.Expired) {
        expired.set(true);
        LOG.warn("ZK session expired: {}", zkClient.getConnectString());

        // Give up leadership, if leader
        if (leader.get()) {
          handler.unelected();
        }
      } else if (event.getState() == Event.KeeperState.Disconnected) {
        disconnect.set(true);
        LOG.warn("ZK session disconnected: {}", zkClient.getConnectString());

        // Give up leadership, if leader
        if (leader.get()) {
          handler.unelected();
        }
      } else if (event.getState() == Event.KeeperState.SyncConnected && expired.get() && !cancelled.get()) {
        expired.set(false);
        LOG.info("Reconnected after expiration: {}", zkClient.getConnectString());
        register();
      } else if (event.getState() == Event.KeeperState.SyncConnected && disconnect.get() && !cancelled.get()) {
        disconnect.set(false);
        LOG.info("Reconnected after disconnect: {}", zkClient.getConnectString());
        runElection();
      }
    }
  }
}

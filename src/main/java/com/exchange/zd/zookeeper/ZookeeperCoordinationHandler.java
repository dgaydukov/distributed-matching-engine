package com.exchange.zd.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZookeeperCoordinationHandler implements CoordinationHandler {
    private static final String PRIMARY_NODE = "/Primary";
    private final ZooKeeper zk;

    public ZookeeperCoordinationHandler(String zookeeperHost) {
        try {
            zk = createInstance(zookeeperHost);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ZooKeeper createInstance(String zookeeperHost) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(zookeeperHost, 5000, new Watcher() {
            public void process(WatchedEvent we) {
                if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            }
        });
        latch.await();
        return zooKeeper;
    }




    /**
     * This method would automatically detect if Primary instance exists and running
     */
    public boolean detectPrimaryNode() {
        try {
            Stat stat = zk.exists(PRIMARY_NODE, true);
            if (stat == null){
                return false;
            }
        } catch (KeeperException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    /**
     * This method would promote Secondary instance to Primary
     */
    public boolean promoteToPrimary() {
        try {
            zk.create(PRIMARY_NODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException ex) {
            if (ex instanceof KeeperException.NodeExistsException){
                return false;
            }
            throw new RuntimeException(ex);
        }
        return true;
    }
    public void createNode(String path, byte[] data) throws KeeperException, InterruptedException {
    }
}
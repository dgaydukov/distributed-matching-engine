package com.exchange.zd.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZookeeperCoordinationHandler implements CoordinationHandler{
    private final ZooKeeper zk;

    public ZookeeperCoordinationHandler(String zookeeperHost){
        try{
            zk = createInstance(zookeeperHost);
        } catch (IOException | InterruptedException ex){
            throw new RuntimeException(ex);
        }
    }

    private ZooKeeper createInstance(String zookeeperHost) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(zookeeperHost,5000,new Watcher() {
            public void process(WatchedEvent we) {
                if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            }
        });
        latch.await();
        return zooKeeper;
    }



    public void start(){
        zk.create("/mynode", {});
    }


    /**
     * This method would automatically detect if Primary instance is dead
     */
    public void detectPrimaryCrash(){

    }

    /**
     * This method would promote Secondary instance to Primary
     */
    public void promoteToPrimary(){
    }
}

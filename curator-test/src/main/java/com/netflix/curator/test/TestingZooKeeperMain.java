package com.netflix.curator.test;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class TestingZooKeeperMain extends ZooKeeperServerMain implements ZooKeeperMainFace
{
    private final CountDownLatch        latch = new CountDownLatch(1);

    @Override
    public void kill()
    {
        try
        {
            Field               cnxnFactoryField = ZooKeeperServerMain.class.getDeclaredField("cnxnFactory");
            cnxnFactoryField.setAccessible(true);
            ServerCnxnFactory   cnxnFactory = (ServerCnxnFactory)cnxnFactoryField.get(this);
            cnxnFactory.closeAll();

            Field               ssField = cnxnFactory.getClass().getDeclaredField("ss");
            ssField.setAccessible(true);
            ServerSocketChannel ss = (ServerSocketChannel)ssField.get(cnxnFactory);
            ss.close();

            close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void runFromConfig(QuorumPeerConfig config) throws Exception
    {
        ServerConfig        serverConfig = new ServerConfig();
        serverConfig.readFrom(config);
        latch.countDown();
        super.runFromConfig(serverConfig);
    }

    @Override
    public void blockUntilStarted() throws Exception
    {
        latch.await();
        Thread.sleep(100);
    }

    @Override
    public void close() throws IOException
    {
        shutdown();
    }
}

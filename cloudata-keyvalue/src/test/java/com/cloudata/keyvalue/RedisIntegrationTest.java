package com.cloudata.keyvalue;

import static com.cloudata.TestUtils.buildBytes;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;

public class RedisIntegrationTest extends IntegrationTestBase {
  private static final Logger log = LoggerFactory.getLogger(RedisIntegrationTest.class);

    private Jedis jedis;

    @BeforeClass
    public static void beforeClass() throws Exception {
        resetState();
        startCluster(3);
    }

    @Before
    public void buildJedisClient() {
        HostAndPort redisEndpoint = SERVERS.get(0).getRedisSocketAddress();

        this.jedis = new Jedis(redisEndpoint.getHostText(), redisEndpoint.getPort());
    }

    @After
    public void stopJedisClient() {
      if (this.jedis != null) {
        this.jedis.quit();
        this.jedis = null;
      }
    }

    @Test
    public void testSetAndGet() throws Exception {

        for (int i = 1; i < 100; i++) {
            byte[] key = Integer.toString(i).getBytes();

            byte[] value = buildBytes(i);

            jedis.set(key, value);
            byte[] actual = jedis.get(key);

            Assert.assertArrayEquals(value, actual);
        }
    }

    @Test
    public void testPartitions() throws Exception {
        int n = 100;
        int partitions = 10;

        for (int i = 0; i < n; i++) {
            byte[] key = Integer.toString(i).getBytes();
            byte[] value = buildBytes(i);

            assertOk(jedis.select(i / 10));
            jedis.set(key, value);
        }

        for (int j = 0; j < partitions; j++) {
            for (int i = 0; i < n; i++) {
                byte[] key = Integer.toString(i).getBytes();
                byte[] value = buildBytes(i);

                assertOk(jedis.select(j));

                byte[] found = jedis.get(key);
                if ((i / partitions) == j) {
                    Assert.assertArrayEquals(value, found);
                } else {
                    Assert.assertNull(found);
                }
            }
        }
    }

    private void assertOk(String r) {
        Assert.assertEquals("OK", r);
    }

    @Test
    public void testAppend() throws Exception {
        byte[] key = UUID.randomUUID().toString().getBytes();

        for (int i = 1; i < 100; i++) {
            byte[] value = new byte[] { (byte) i };

            Long newLength = jedis.append(key, value);

            Assert.assertEquals(i, newLength.longValue());
        }

        byte[] finalValue = jedis.get(key);

        for (int i = 1; i < 100; i++) {
            Assert.assertEquals(i, finalValue[i - 1]);
        }
    }

    @Test
    public void testSetAndDelete() throws Exception {
        List<byte[]> allKeys = Lists.newArrayList();
        for (int i = 1; i < 100; i++) {
          log.info("Setting key {}", i);
          
            byte[] key = Integer.toString(i).getBytes();

            byte[] value = buildBytes(i);

            sanityCheck();
            
            jedis.set(key, value);

            allKeys.add(key);
        }

        Collections.shuffle(allKeys);

        for (List<byte[]> partition : Iterables.partition(allKeys, 8)) {
            byte[][] array = partition.toArray(new byte[partition.size()][]);

            Long count = jedis.del(array);
            Assert.assertEquals(partition.size(), count.longValue());
        }
    }

  

    @Test
    public void testIncrement() throws Exception {

        byte[] key = "INCR".getBytes();

        jedis.set(key, "0".getBytes());

        for (int i = 1; i < 100; i++) {
            Long value = jedis.incr(key);

            Assert.assertEquals(i, value.longValue());
        }
    }

    @Test
    public void testIncrementBy() throws Exception {

        long counter = 0;
        Random r = new Random();
        byte[] key = "INCRBY".getBytes();

        jedis.set(key, "0".getBytes());

        for (int i = 1; i < 100; i++) {
            long delta = r.nextInt();

            Long value = jedis.incrBy(key, delta);

            counter += delta;

            Assert.assertEquals(counter, value.longValue());
        }
    }

    @Test
    public void testDecrement() throws Exception {

        byte[] key = "DECR".getBytes();

        jedis.set(key, "0".getBytes());

        for (int i = 1; i < 100; i++) {
            Long value = jedis.decr(key);

            Assert.assertEquals(-i, value.longValue());
        }
    }

    @Test
    public void testDecrementBy() throws Exception {

        long counter = 0;
        Random r = new Random();
        byte[] key = "DECRBY".getBytes();

        jedis.set(key, "0".getBytes());

        for (int i = 1; i < 100; i++) {
            long delta = r.nextInt();

            Long value = jedis.decrBy(key, delta);

            counter -= delta;

            Assert.assertEquals(counter, value.longValue());
        }
    }

    @Test
    public void testExists() throws Exception {

        byte[] key = UUID.randomUUID().toString().getBytes();
        Assert.assertFalse(jedis.exists(key));

        jedis.set(key, key);

        Assert.assertTrue(jedis.exists(key));
    }

}

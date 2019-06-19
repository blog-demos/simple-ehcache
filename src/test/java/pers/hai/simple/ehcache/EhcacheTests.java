package pers.hai.simple.ehcache;

import org.apache.log4j.Logger;
import org.ehcache.*;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.junit.Test;

import java.io.File;

/**
 * TODO
 * <p>
 * Create Time: 2019-06-16 11:34
 * Last Modify: 2019-06-16
 *
 * @author Q-WHai
 * @see <a href="https://github.com/qwhai">https://github.com/qwhai</a>
 */
public class EhcacheTests {

    private final Logger logger = Logger.getLogger(EhcacheTests.class);

    @Test
    public void test1() {
        CacheManager cm = CacheManagerBuilder
                .newCacheManagerBuilder()
                .withCache(
                        "preConfigured", // 缓存别名
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class,
                                String.class,
                                ResourcePoolsBuilder.heap(20) // 设置缓存堆容纳元素个数
                        )
                )
                .build();
        cm.init();

        // 从缓存管理器中获取预定的缓存
        Cache<Long, String> preConfigured = cm.getCache("preConfigured", Long.class, String.class);

        // 直接从缓存管理器创建一个新的缓存
        Cache<Integer, String> myCache = cm.createCache(
                "myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Integer.class,
                        String.class,
                        ResourcePoolsBuilder.heap(20)
                ).build()
        );

        // 向缓存里添加缓存键值
        for (int i = 0; i < 20; i++) {
            myCache.put(i, String.format("@%d", i));
        }

        // 从指定缓存里获取键值
        for (int i = 0; i < 20; i++) {
            logger.info(String.format("%d : %s", i, myCache.get(i)));
        }

        cm.close();
    }

    // 这里测试添加的键值数大于配置的堆大小时的情况
    @Test
    public void test2() {
        CacheManager cm = CacheManagerBuilder
                .newCacheManagerBuilder()
                .withCache(
                        "preConfigured", // 缓存别名
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class,
                                String.class,
                                ResourcePoolsBuilder.heap(10) // 设置缓存堆容纳元素个数
                        )
                )
                .build();
        cm.init();

        // 直接从缓存管理器创建一个新的缓存
        Cache<Integer, String> myCache = cm.createCache(
                "myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Integer.class,
                        String.class,
                        ResourcePoolsBuilder.heap(10)
                ).build()
        );

        // 向缓存里添加缓存键值
        for (int i = 0; i < 20; i++) {
            myCache.put(i, String.format("@%d", i));
        }

        // 从指定缓存里获取键值
        for (int i = 0; i < 20; i++) {
            logger.info(String.format("%d : %s", i, myCache.get(i)));
        }

        cm.close();
    }

    @Test
    public void test3() {
        UserManagedCache<Integer, String> userManagedCache = UserManagedCacheBuilder
                        .newUserManagedCacheBuilder(Integer.class, String.class)
                        .build(false);
        userManagedCache.init();

        for (int i = 0; i <= 20; i++) {
            // 写
            userManagedCache.put(i, String.format("#%d", i));
            // 读
            String val = userManagedCache.get(i);
            logger.info(String.format("%d : %s", i, val));
        }

        userManagedCache.close();
    }

    // 测试三级缓存（PersistentCacheManager）
    @Test
    public void test4() {
        // 缓存路径 -- H:\\myData\\
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(getStoragePath() + File.separator + "myData"))
                .withCache("threeTieredCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, EntryUnit.ENTRIES)  // 堆
                                        .offheap(1, MemoryUnit.MB)    // 堆外
                                        .disk(20, MemoryUnit.GB)      // 磁盘
                        )
                ).build(true);

        Cache<Integer, String> threeTieredCache = persistentCacheManager.getCache("threeTieredCache", Integer.class, String.class);

        // 写
        for (int i = 0; i <= 200000; i++){
            threeTieredCache.put(i, String.format("$%d", i));
        }

        // 读
        for (int i = 0; i <= 200000; i++){
            String value = threeTieredCache.get(i);
            logger.info(String.format("[R] %d : %s", i, value));
        }

        persistentCacheManager.close();
    }

    // 测试持久化磁盘
    @Test
    public void test5() {
        String path = this.getClass().getResource("/").getPath();
        logger.info("----" + path );
        // 对于磁盘层，数据存储在磁盘上。磁盘的速度越快、越专注，访问数据的速度就越快。
        // 要获得一个PersistentCacheManager,其实就是一个普通的通常的CacheManager，但是具有销毁缓存的能力。
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File(path, "myData"))) //提供一个存储数据的位置
                .withCache("cache_1",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                // 为缓存使用的磁盘定义一个资源池。第三个参数是一个布尔值，用于设置磁盘池是否持久。
                                // 当设置为true时，池是持久的。当使用两个参数磁盘(long、MemoryUnit)的版本时，池就不会持久。
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        // 您为堆定义了一个资源池,该堆内只允许存放10个条目
                                        .heap(10, EntryUnit.ENTRIES)
                                        // 您为非堆定义了一个资源池。仍然非常快，而且有点大。该大小为1M
                                        /*.offheap(1, MemoryUnit.MB)*/
                                        // 您为磁盘定义一个持久的资源池。它是持久性的，因为它应该是(最后一个参数是正确的)。
                                        .disk(10, MemoryUnit.MB, true))
                )
                .build(true);

        Cache<Long, String> cache = persistentCacheManager.getCache("cache_1", Long.class, String.class);
        //存储在缓存中的所有值都将在JVM重新启动后可用(假定CacheManager通过调用close()已被干净地关闭了。
        cache.put(1L, "stillAvailableAfterRestart");
        logger.info(String.format("cache, 获取信息：%s", cache.get(1L)));
        persistentCacheManager.close();
        //上面的示例分配了非常少量的磁盘存储。您通常会使用更大的存储空间。
        //持久性意味着缓存将在JVM重新启动后存活。在重新启动JVM并在相同的位置创建一个CacheManager磁盘持久性之后，缓存中的所有内容仍然存在。
        //磁盘层不能在缓存管理器之间共享。一个持久性目录是专门针对一个缓存管理器的。
        //请记住，存储在磁盘上的数据必须被序列化/反序列化，并写入/从磁盘读取/读取，因此比堆和off堆要慢。因此，磁盘存储很有趣:
        //* 你有大量的数据不适合堆在堆里
        //* 你的磁盘比它缓存的存储快得多
        //* 你对持久性感兴趣
    }

    private static String getStoragePath() {
        return "h:";
    }
}

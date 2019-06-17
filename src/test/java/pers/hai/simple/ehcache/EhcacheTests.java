package pers.hai.simple.ehcache;

import org.apache.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.UserManagedCache;
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

        for (int i = 0; i <= 20; i++){
            // 写
            userManagedCache.put(i, String.format("#%d", i));
            // 读
            String val = userManagedCache.get(i);
            logger.info(String.format("%d : %s", i, val));
        }

        userManagedCache.close();
    }

    @Test
    public void test4() {
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

        // 读
        for (int i = 0; i<= 20000; i++){
            threeTieredCache.put(i, String.format("$%d", i));
        }

        // 写
        for (int i = 0; i <= 200000; i++){
            String value = threeTieredCache.get(i);
            logger.info(String.format("%d : %s", i, value));
        }

        persistentCacheManager.close();
    }

    private static String getStoragePath() {
        return "d:";
    }
}

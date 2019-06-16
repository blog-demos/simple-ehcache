package pers.hai.simple.ehcache;

import org.apache.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.junit.Test;

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
                        "preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class,
                                String.class,
                                ResourcePoolsBuilder.heap(10)
                        )
                )
                .build();
        cm.init();

        Cache<Long, String> preConfigured = cm.getCache("preConfigured", Long.class, String.class);

        Cache<Integer, String> myCache = cm.createCache(
                "myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Integer.class,
                        String.class,
                        ResourcePoolsBuilder.heap(10)
                ).build()
        );

        for (int i = 0; i < 20; i++) {
            myCache.put(i, String.format("@%d", i));
            String val = myCache.get(i);

            logger.info(String.format("%d : %s", i, val));
        }
    }
}

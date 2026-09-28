package com.nashtech.learning.redisapp.service;

import com.nashtech.learning.library.dto.EmployeeRequest;
import com.nashtech.learning.library.dto.EmployeeResponse;
import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;
import com.nashtech.learning.redisapp.config.RedisConfig;
import com.nashtech.learning.redisapp.entity.EmployeeEntity;
import com.nashtech.learning.redisapp.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceCacheTest {

    @TestConfiguration
    static class StubRedisTestConfig {

        @Bean
        @Primary
        public RedisTemplate<String, Object> testRedisTemplate() {
            return new RedisTemplate<>() {
                private final Map<String, Object> storage = new ConcurrentHashMap<>();

                @Override
                public void afterPropertiesSet() {
                    // No-op for test stub to bypass connection factory check
                }

                @Override
                public Boolean hasKey(String key) {
                    return storage.containsKey(key);
                }

                @Override
                public Long getExpire(String key, TimeUnit timeUnit) {
                    return storage.containsKey(key) ? 60L : -2L;
                }
            };
        }
    }

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CacheManager cacheManager;

    private Long testEmployeeId;

    @BeforeEach
    void setUp() {
        Cache employeeCache = cacheManager.getCache(RedisConfig.CACHE_EMPLOYEES);
        if (employeeCache != null) {
            employeeCache.clear();
        }
        Cache listCache = cacheManager.getCache(RedisConfig.CACHE_EMPLOYEE_LIST);
        if (listCache != null) {
            listCache.clear();
        }

        employeeRepository.deleteAll();

        EmployeeEntity entity = new EmployeeEntity(
                "ENG-0050",
                "Bruce",
                "Wayne",
                "bruce.wayne.cachetest@nashtechglobal.com",
                Department.ENGINEERING,
                120000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 1)
        );
        EmployeeEntity saved = employeeRepository.save(entity);
        testEmployeeId = saved.getId();
    }

    @Test
    @DisplayName("@Cacheable: First call loads into cache; subsequent read is served from cache")
    void shouldDemonstrateCacheableReadHitAndMiss() {
        Cache employeeCache = cacheManager.getCache(RedisConfig.CACHE_EMPLOYEES);
        assertThat(employeeCache).isNotNull();

        assertThat(employeeCache.get(testEmployeeId)).isNull();

        EmployeeResponse firstCall = employeeService.getEmployeeById(testEmployeeId);
        assertThat(firstCall).isNotNull();
        assertThat(firstCall.getFullName()).isEqualTo("Bruce Wayne");

        Cache.ValueWrapper cachedWrapper = employeeCache.get(testEmployeeId);
        assertThat(cachedWrapper).isNotNull();
        EmployeeResponse cachedResponse = (EmployeeResponse) cachedWrapper.get();
        assertThat(cachedResponse).isNotNull();
        assertThat(cachedResponse.getId()).isEqualTo(testEmployeeId);
        assertThat(cachedResponse.getFullName()).isEqualTo("Bruce Wayne");

        EmployeeResponse secondCall = employeeService.getEmployeeById(testEmployeeId);
        assertThat(secondCall).isNotNull();
        assertThat(secondCall.getEmail()).isEqualTo("bruce.wayne.cachetest@nashtechglobal.com");
    }

    @Test
    @DisplayName("@CachePut: Updating employee updates DB and refreshes cache immediately")
    void shouldDemonstrateCachePutOnUpdate() {
        Cache employeeCache = cacheManager.getCache(RedisConfig.CACHE_EMPLOYEES);
        assertThat(employeeCache).isNotNull();

        employeeService.getEmployeeById(testEmployeeId);
        assertThat(employeeCache.get(testEmployeeId)).isNotNull();

        EmployeeRequest updateRequest = new EmployeeRequest(
                "Bruce",
                "Wayne",
                "bruce.wayne.cachetest@nashtechglobal.com",
                Department.ENGINEERING,
                150000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 1)
        );

        EmployeeResponse updated = employeeService.updateEmployee(testEmployeeId, updateRequest);
        assertThat(updated.getSalary()).isEqualTo(150000.0);

        Cache.ValueWrapper updatedCacheWrapper = employeeCache.get(testEmployeeId);
        assertThat(updatedCacheWrapper).isNotNull();
        EmployeeResponse cachedAfterUpdate = (EmployeeResponse) updatedCacheWrapper.get();
        assertThat(cachedAfterUpdate).isNotNull();
        assertThat(cachedAfterUpdate.getSalary()).isEqualTo(150000.0);

        EmployeeResponse secondRead = employeeService.getEmployeeById(testEmployeeId);
        assertThat(secondRead.getSalary()).isEqualTo(150000.0);
    }

    @Test
    @DisplayName("@CacheEvict: Deleting employee evicts entry from cache and deletes from DB")
    void shouldDemonstrateCacheEvictOnDelete() {
        Cache employeeCache = cacheManager.getCache(RedisConfig.CACHE_EMPLOYEES);
        assertThat(employeeCache).isNotNull();

        employeeService.getEmployeeById(testEmployeeId);
        assertThat(employeeCache.get(testEmployeeId)).isNotNull();

        employeeService.deleteEmployee(testEmployeeId);

        assertThat(employeeCache.get(testEmployeeId)).isNull();
        assertThat(employeeRepository.findById(testEmployeeId)).isEmpty();
    }
}
package com.nashtech.learning.redisapp.service;

import com.nashtech.learning.library.dto.EmployeeRequest;
import com.nashtech.learning.library.dto.EmployeeResponse;
import com.nashtech.learning.library.exception.DuplicateEmployeeException;
import com.nashtech.learning.library.exception.EmployeeNotFoundException;
import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.util.EmployeeUtils;
import com.nashtech.learning.library.validation.EmployeeValidator;
import com.nashtech.learning.redisapp.config.RedisConfig;
import com.nashtech.learning.redisapp.entity.EmployeeEntity;
import com.nashtech.learning.redisapp.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               RedisTemplate<String, Object> redisTemplate) {
        this.employeeRepository = employeeRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @CacheEvict(value = RedisConfig.CACHE_EMPLOYEE_LIST, allEntries = true)
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info(">>> Creating new employee: {} {}", request.getFirstName(), request.getLastName());

        EmployeeValidator.validate(request);

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmployeeException("email", request.getEmail());
        }

        EmployeeEntity entity = new EmployeeEntity(
                "TEMP",
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getDepartment(),
                request.getSalary(),
                request.getStatus(),
                request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now()
        );

        entity = employeeRepository.save(entity);

        String code = EmployeeUtils.generateEmployeeCode(entity.getDepartment(), entity.getId());
        entity.setEmployeeCode(code);
        entity = employeeRepository.save(entity);

        log.info(">>> [DB INSERT] Employee created successfully with ID: {} and Code: {}", entity.getId(), code);
        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_EMPLOYEES, key = "#id")
    public EmployeeResponse getEmployeeById(Long id) {
        log.info(">>> [CACHE MISS / DATABASE FETCH] Querying database for employee ID: {}", id);

        EmployeeEntity entity = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_EMPLOYEE_LIST, key = "'all'")
    public List<EmployeeResponse> getAllEmployees() {
        log.info(">>> [CACHE MISS / DATABASE FETCH] Querying database for all employees");
        return employeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(Department department) {
        log.info(">>> Querying database for employees in department: {}", department);
        return employeeRepository.findByDepartment(department).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(
            put = @CachePut(value = RedisConfig.CACHE_EMPLOYEES, key = "#id"),
            evict = @CacheEvict(value = RedisConfig.CACHE_EMPLOYEE_LIST, allEntries = true)
    )
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info(">>> [CACHE PUT / DATABASE UPDATE] Updating employee ID: {}", id);

        EmployeeValidator.validate(request);

        EmployeeEntity entity = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        if (!entity.getEmail().equalsIgnoreCase(request.getEmail()) &&
                employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmployeeException("email", request.getEmail());
        }

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());
        entity.setDepartment(request.getDepartment());
        entity.setSalary(request.getSalary());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getJoiningDate() != null) {
            entity.setJoiningDate(request.getJoiningDate());
        }

        entity = employeeRepository.save(entity);
        log.info(">>> [DB UPDATED & CACHE PUT] Successfully updated DB and refreshed Redis cache for ID: {}", id);

        return mapToResponse(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = RedisConfig.CACHE_EMPLOYEES, key = "#id"),
            @CacheEvict(value = RedisConfig.CACHE_EMPLOYEE_LIST, allEntries = true)
    })
    public void deleteEmployee(Long id) {
        log.info(">>> [CACHE EVICT / DATABASE DELETE] Evicting cache and deleting employee ID: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }

        employeeRepository.deleteById(id);
        log.info(">>> [DB DELETED & CACHE EVICTED] Record removed from DB and Redis key evicted for ID: {}", id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = RedisConfig.CACHE_EMPLOYEES, allEntries = true),
            @CacheEvict(value = RedisConfig.CACHE_EMPLOYEE_LIST, allEntries = true)
    })
    public void clearAllCache() {
        log.info(">>> [CACHE PURGE] Evicted all entries from 'employees' and 'employeeList' caches");
    }

    @Override
    public Map<String, Object> inspectCache(Long id) {
        Map<String, Object> result = new LinkedHashMap<>();
        String redisKey = RedisConfig.CACHE_EMPLOYEES + "::" + id;

        try {
            Boolean hasKey = redisTemplate.hasKey(redisKey);
            result.put("cacheKey", redisKey);
            result.put("isCachedInRedis", Boolean.TRUE.equals(hasKey));

            if (Boolean.TRUE.equals(hasKey)) {
                Long ttlSeconds = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
                Object rawCachedValue = redisTemplate.opsForValue().get(redisKey);
                result.put("ttlRemainingSeconds", ttlSeconds);
                result.put("isExpired", ttlSeconds != null && ttlSeconds <= 0);
                result.put("cachedPayload", rawCachedValue);
            } else {
                result.put("ttlRemainingSeconds", -2);
                result.put("message", "Key not found in Redis (either expired or not yet queried).");
            }
        } catch (Exception e) {
            log.warn("Redis inspection failed: {}", e.getMessage());
            result.put("error", "Unable to inspect Redis: " + e.getMessage());
        }

        return result;
    }

    private EmployeeResponse mapToResponse(EmployeeEntity entity) {
        String fullName = EmployeeUtils.formatFullName(entity.getFirstName(), entity.getLastName());
        String maskedEmail = EmployeeUtils.maskEmail(entity.getEmail());
        Double estimatedBonus = EmployeeUtils.calculateBonus(entity.getSalary(), entity.getDepartment());

        return new EmployeeResponse(
                entity.getId(),
                entity.getEmployeeCode(),
                fullName,
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                maskedEmail,
                entity.getDepartment(),
                entity.getSalary(),
                estimatedBonus,
                entity.getStatus(),
                entity.getJoiningDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

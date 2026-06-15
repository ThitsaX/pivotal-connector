package com.thitsaworks.mojaloop.coreconnector.listeners.pending_transfer_store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.net.URI;

@Component
public class PendingTransfersStore implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(PendingTransfersStore.class);
    private static final int LOCK_TTL_SECONDS = 60;

    private final CoreConnectorConfiguration.Settings config;
    private final ObjectMapper objectMapper;

    private JedisPool jedisPool;
    private String keyPrefix;
    private String lockPrefix;
    private int ttlSeconds;

    public PendingTransfersStore(CoreConnectorConfiguration.Settings config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterPropertiesSet() {
        String connectorId = config.getConnectorId();
        String redisUrl = config.getRedisUrl();

        this.ttlSeconds = config.getRedisTtlSeconds();
        this.keyPrefix = connectorId + "-connector:pending:";
        this.lockPrefix = connectorId + "-connector:lock:patch:";

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);

        this.jedisPool = new JedisPool(poolConfig, URI.create(redisUrl));
        LOG.info("Connected to Redis at {}", redisUrl);
    }

    @Override
    public void destroy() {
        if (jedisPool != null) {
            jedisPool.close();
            LOG.info("Redis connection pool closed.");
        }
    }

    public void set(String transferId, PendingTransfer value) throws Exception {
        String json = objectMapper.writeValueAsString(value);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(keyPrefix + transferId, ttlSeconds, json);
        }
    }

    public PendingTransfer get(String transferId) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            String raw = jedis.get(keyPrefix + transferId);
            return raw == null ? null : objectMapper.readValue(raw, PendingTransfer.class);
        }
    }

    public void delete(String transferId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(keyPrefix + transferId);
        }
    }

    public boolean acquireLock(String transferId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.set(
                lockPrefix + transferId,
                "1",
                SetParams.setParams().ex(LOCK_TTL_SECONDS).nx()
                                     );
            return "OK".equals(result);
        }
    }

    public void releaseLock(String transferId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(lockPrefix + transferId);
        }
    }
}

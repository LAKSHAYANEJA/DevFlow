package com.devflow.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    // In-memory buckets per user - simple and effective for single-instance
    // In production this would use Bucket4j's Redis backend for distributed limiting

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // 10 requests per minute per user

        Bandwidth limit = Bandwidth.builder()
        .capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build();

        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, k -> createNewBucket());
        return bucket.tryConsume(1);
    }

    public long getAvailableTokens(String userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, k -> createNewBucket());
        return bucket.getAvailableTokens();
    }
    
}

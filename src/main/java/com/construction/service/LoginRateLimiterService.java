package com.construction.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory brute-force guard for the login form: after
 * {@value #MAX_ATTEMPTS} failed attempts from the same IP within the
 * time window, further attempts from that IP are blocked until the
 * window elapses. Tracked per-JVM instance - adequate for a single-node
 * deployment, not a substitute for a shared store if the app is ever
 * horizontally scaled.
 */
@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 15 * 60 * 1000L;

    private static class Attempt {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }

    private final ConcurrentHashMap<String, Attempt> attemptsByIp = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        Attempt a = attemptsByIp.get(ip);
        if (a == null) {
            return false;
        }
        if (System.currentTimeMillis() - a.windowStart > WINDOW_MILLIS) {
            attemptsByIp.remove(ip);
            return false;
        }
        return a.count.get() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String ip) {
        Attempt a = attemptsByIp.computeIfAbsent(ip, k -> new Attempt());
        if (System.currentTimeMillis() - a.windowStart > WINDOW_MILLIS) {
            a.windowStart = System.currentTimeMillis();
            a.count.set(0);
        }
        a.count.incrementAndGet();
    }

    public void recordSuccess(String ip) {
        attemptsByIp.remove(ip);
    }
}

package com.smartiot.qualityinspection.sensor.model;

import com.smartiot.qualityinspection.common.enums.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A virtual IoT sensor instance. Tracks its online/offline health and the time it was
 * last seen (used for heartbeat-based offline detection).
 */
@Entity
@Table(name = "sensor")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique key for this sensor instance, e.g. WEIGHT-1. */
    @Column(nullable = false, unique = true)
    private String sensorKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType sensorType;

    @Column(nullable = false)
    private boolean online;

    private Instant lastSeenAt;

    protected Sensor() {
        // Required by JPA.
    }

    public Sensor(String sensorKey, SensorType sensorType, boolean online, Instant lastSeenAt) {
        this.sensorKey = sensorKey;
        this.sensorType = sensorType;
        this.online = online;
        this.lastSeenAt = lastSeenAt;
    }

    public Long getId() {
        return id;
    }

    public String getSensorKey() {
        return sensorKey;
    }

    public void setSensorKey(String sensorKey) {
        this.sensorKey = sensorKey;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}

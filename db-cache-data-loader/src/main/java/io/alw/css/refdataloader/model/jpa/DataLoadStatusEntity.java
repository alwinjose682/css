package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.ActionStatus;
import io.alw.css.refdataloader.model.DataLoadType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "DATA_LOAD_STATUS")
public class DataLoadStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "LOAD_TYPE")
    @Enumerated(value = EnumType.STRING)
    DataLoadType loadType;

    @Column(name = "START_TIME")
    LocalDateTime startTime;

    @Column(name = "END_TIME")
    LocalDateTime endTime;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    ActionStatus status;

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DataLoadType getLoadType() {
        return loadType;
    }

    public void setLoadType(DataLoadType loadType) {
        this.loadType = loadType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }


    public DataLoadStatusEntity loadType(DataLoadType loadType) {
        this.loadType = loadType;
        return this;
    }

    public DataLoadStatusEntity startTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public DataLoadStatusEntity endTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public DataLoadStatusEntity status(ActionStatus status) {
        this.status = status;
        return this;
    }
}

package io.alw.css.refdataloader.model.jpa;

import io.alw.css.domain.common.YesNo;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ENTITY", schema = "css_refdata")
public non-sealed class EntityEntity implements ReferenceDataJpaEntity {
    @EmbeddedId
    EntityEntityPK entityEntityPK;

    @Column(name = "ENTITY_NAME")
    String entityName;

    @Column(name = "CURR_CODE")
    String currCode;

    @Column(name = "COUNTRY_CODE", length = 2, nullable = false)
    String countryCode;

    @Column(name = "COUNTRY_NAME")
    String countryName;

    @Column(name = "BIC_CODE")
    String bicCode;

    @Column(name = "ACTIVE", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    YesNo active;

    @Column(name = "ENTRY_TIME")
    LocalDateTime entryTime;

    // Associations
    @OneToMany(mappedBy = "entity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<NostroEntity> nostros;

    @OneToMany(mappedBy = "entity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<BookEntity> books;

    public YesNo getActive() {
        return active;
    }

    public void setActive(YesNo active) {
        this.active = active;
    }

    public String getBicCode() {
        return bicCode;
    }

    public void setBicCode(String bicCode) {
        this.bicCode = bicCode;
    }

    public List<BookEntity> getBooks() {
        return books;
    }

    public void setBooks(List<BookEntity> books) {
        this.books = books;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }

    public EntityEntityPK getEntityEntityPK() {
        return entityEntityPK;
    }

    public void setEntityEntityPK(EntityEntityPK entityEntityPK) {
        this.entityEntityPK = entityEntityPK;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public List<NostroEntity> getNostros() {
        return nostros;
    }

    public void setNostros(List<NostroEntity> nostros) {
        this.nostros = nostros;
    }
}

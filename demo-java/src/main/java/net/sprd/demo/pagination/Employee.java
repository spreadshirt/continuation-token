package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Pageable;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public class Employee implements Pageable {
    private int id;
    private String name;
    private Timestamp dateCreated;

    public Employee(int id, String name, Timestamp dateCreated) {
        this.id = id;
        this.name = name;
        this.dateCreated = dateCreated;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    @NotNull
    @Override
    public String getID() {
        return Integer.toString(this.id);
    }

    @Override
    public long getTimestamp() {
        return dateCreated.toInstant().getEpochSecond();
    }
}

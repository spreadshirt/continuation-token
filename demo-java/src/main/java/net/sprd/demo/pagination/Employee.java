package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Pageable;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public class Employee implements Pageable {
    private int id;
    private String name;
    private Timestamp timestamp;

    public Employee(int id, String name, Timestamp timestamp) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Timestamp getTimestamp2() {
        return timestamp;
    }

    @NotNull
    @Override
    public String getID() {
        return Integer.toString(this.id);
    }

    @Override
    public long getTimestamp() {
        return timestamp.toInstant().getEpochSecond();
    }
}

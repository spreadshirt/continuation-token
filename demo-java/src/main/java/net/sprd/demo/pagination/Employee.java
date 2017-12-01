package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Pageable;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;

import static java.lang.String.format;
import static java.sql.Types.INTEGER;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.VARCHAR;

public class Employee implements SqlParameterSource, Pageable {
    private int id;
    private String name;
    private Timestamp timestamp;

    public Employee(int id, String name, Timestamp timestamp) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
    }

    private Object get(String fieldName) {
        switch (fieldName) {
            case "id":
                return id;
            case "name":
                return name;
            case "timestamp":
                return timestamp;
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getEmployeeId() {
        return name;
    }

    @Override
    public boolean hasValue(String paramName) {
        return get(paramName) != null;
    }

    @Nullable
    @Override
    public Object getValue(String paramName) throws IllegalArgumentException {
        if (!hasValue(paramName)) {
            throw new IllegalArgumentException(format("Field '%s' is unknown", paramName));
        }
        return get(paramName);
    }

    @Override
    public int getSqlType(String paramName) {
        switch (paramName.toLowerCase()) {
            case "id":
                return INTEGER;
            case "name":
                return VARCHAR;
            case "timestamp":
                return TIMESTAMP;
        }
        return TYPE_UNKNOWN;
    }

    @Nullable
    @Override
    public String getTypeName(String paramName) {
        try {
            return this.getClass().getField(paramName).getType().getName();
        } catch (NoSuchFieldException e) {
            return "";
        }
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

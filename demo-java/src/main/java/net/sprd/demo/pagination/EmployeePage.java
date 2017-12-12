package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Page;

import java.util.List;

public class EmployeePage {
    private final List<Employee> entities;
    private final String token;
    private final boolean hasNext;

    public EmployeePage(Page<Employee> page) {
        this.entities = page.getEntities();
        this.hasNext = page.getHasNext();
        this.token = (page.getToken() == null) ? null : page.getToken().toString();
    }

    public List<Employee> getEntities() {
        return entities;
    }

    public String getToken() {
        return token;
    }

    public boolean hasNext() {
        return hasNext;
    }
}

package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Page;

import java.util.List;

public class EmployeePage {
    private final List<Employee> entities;
    private final String token;

    public EmployeePage(Page<Employee> page) {
        this.entities = page.getEntities();
        this.token = (page.getToken() == null) ? null : page.getToken().toString();
    }

    public List<Employee> getEntities() {
        return entities;
    }

    public String getToken() {
        return token;
    }
}

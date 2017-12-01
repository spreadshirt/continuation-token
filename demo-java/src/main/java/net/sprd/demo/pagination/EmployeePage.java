package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Page;

import java.util.List;

import static java.lang.String.format;

public class EmployeePage {
    private final List<Employee> entities;
    private final String token;

    public EmployeePage(Page<Employee> page, String hostAddress) {
        this.entities = page.getEntities();
        if (page.getToken() == null) {
            this.token = null;
            return;
        }
        this.token = format("http://%s/?pageSize=%d&continue=%s", hostAddress, page.getEntities().size(), page.getToken().toString());
    }

    public List<Employee> getEntities() {
        return entities;
    }

    public String getToken() {
        return token;
    }
}

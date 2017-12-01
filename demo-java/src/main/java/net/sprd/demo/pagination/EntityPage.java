package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Page;

import java.util.List;

public class EntityPage {
    private final List<Entity> entities;
    private final String token;

    public EntityPage(Page<Entity> page) {
        this.entities = page.getEntities();
        this.token = (page.getToken() == null) ? null : page.getToken().toString();
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public String getToken() {
        return token;
    }
}

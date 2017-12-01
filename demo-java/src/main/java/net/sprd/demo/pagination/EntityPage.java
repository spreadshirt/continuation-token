package net.sprd.demo.pagination;

import net.sprd.common.continuationtoken.Page;

import java.util.List;

import static java.lang.String.format;

public class EntityPage {
    private final List<Entity> entities;
    private final String token;

    public EntityPage(Page<Entity> page, String hostAddress) {
        this.entities = page.getEntities();
        if (page.getToken() == null) {
            this.token = null;
            return;
        }
        this.token = format("http://%s/?pageSize=%d&continue=%s", hostAddress, page.getEntities().size(), page.getToken().toString());
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public String getToken() {
        return token;
    }
}

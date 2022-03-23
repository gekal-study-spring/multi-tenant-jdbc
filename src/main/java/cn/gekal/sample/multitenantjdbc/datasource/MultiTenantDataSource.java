package cn.gekal.sample.multitenantjdbc.datasource;

import cn.gekal.sample.multitenantjdbc.security.MultiTenantUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiTenantDataSource extends AbstractRoutingDataSource {

    /** LOGGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiTenantDataSource.class);

    private final AtomicBoolean initialized = new AtomicBoolean();

    @Override
    public DataSource determineTargetDataSource() {

        if (this.initialized.compareAndSet(false, true)) {
            this.afterPropertiesSet();
        }

        return super.determineTargetDataSource();
    }

    @Override
    protected Object determineCurrentLookupKey() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MultiTenantUser user) {
            var tenantId = user.getTenantId();
            LOGGER.info("the tenant is is {}", tenantId);
            return tenantId;
        }

        return null;
    }
}
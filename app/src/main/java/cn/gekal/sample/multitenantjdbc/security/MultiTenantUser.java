package cn.gekal.sample.multitenantjdbc.security;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class MultiTenantUser extends User {

  private final Integer tenantId;

  /**
   * @param password 事前にエンコード済みのパスワード（{id}プレフィックス付き）を渡すこと
   */
  public MultiTenantUser(
      String username,
      String password,
      boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired,
      boolean accountNonLocked,
      Integer tenantId) {
    super(
        username,
        password,
        enabled,
        accountNonExpired,
        credentialsNonExpired,
        accountNonLocked,
        List.of(new SimpleGrantedAuthority("USER")));
    this.tenantId = tenantId;
  }

  public Integer getTenantId() {
    return tenantId;
  }
}

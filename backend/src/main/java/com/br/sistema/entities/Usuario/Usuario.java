package com.br.sistema.entities.Usuario;

import com.br.sistema.entities.Role.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "tb_usuarios")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario implements UserDetails {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private boolean senhaProvisoria = false;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_usuarios_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // @OneToMany(mappedBy = "usuario") REMOVIDO
    // Usuario não referencia mais Solicitacao diretamente.
    // O vínculo agora é Usuario → FichaSolicitacao → Solicitacao.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo;
    }

    public boolean isAdmin() {
        return this.roles.stream()
                .anyMatch(role -> "ADMIN".equals(role.getNome()));
    }
}
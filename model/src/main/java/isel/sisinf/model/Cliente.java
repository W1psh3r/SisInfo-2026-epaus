package isel.sisinf.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Version;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "cartao_cidadao", unique = true, nullable = false, length = 20)
    private String cartaoCidadao;

    @Column(name = "nome", nullable = false, length = 256)
    private String nome;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    public Cliente() {}

    public Cliente(String nif, String cartaoCidadao, String nome) {
        this.nif = nif;
        this.cartaoCidadao = cartaoCidadao;
        this.nome = nome;
    }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getCartaoCidadao() { return cartaoCidadao; }
    public void setCartaoCidadao(String cartaoCidadao) { this.cartaoCidadao = cartaoCidadao; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}

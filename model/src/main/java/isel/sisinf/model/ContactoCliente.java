package isel.sisinf.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contacto_cliente")
public class ContactoCliente {

    @Id
    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "cartao_cidadao", length = 20)
    private String cartaoCidadao;

    @Column(name = "nome", length = 256)
    private String nome;

    @Column(name = "tipo_contacto", length = 20)
    private String tipoContacto;

    @Column(name = "contacto", length = 254)
    private String contacto;

    @Column(name = "descricao", length = 50)
    private String descricao;

    public ContactoCliente() {}

    public ContactoCliente(String nif, String cartaoCidadao, String nome, String tipoContacto, String contacto, String descricao) {
        this.nif = nif;
        this.cartaoCidadao = cartaoCidadao;
        this.nome = nome;
        this.tipoContacto = tipoContacto;
        this.contacto = contacto;
        this.descricao = descricao;
    }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getCartaoCidadao() { return cartaoCidadao; }
    public void setCartaoCidadao(String cartaoCidadao) { this.cartaoCidadao = cartaoCidadao; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipoContacto() { return tipoContacto; }
    public void setTipoContacto(String tipoContacto) { this.tipoContacto = tipoContacto; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
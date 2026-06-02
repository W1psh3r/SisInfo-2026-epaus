package isel.sisinf.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "portefolio",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cliente_nif", "nome"})
        }
)
public class Portefolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portefolio_id")
    private Long portefolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_nif", nullable = false)
    private Cliente cliente;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    public Portefolio() {
    }

    public Long getPortefolioId() {
        return portefolioId;
    }

    public void setPortefolioId(Long portefolioId) {
        this.portefolioId = portefolioId;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
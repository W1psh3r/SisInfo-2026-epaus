/*
MIT License

Copyright (c) 2025-2026, Nuno Datia, Matilde Pato, ISEL

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package isel.sisinf.ui;

import java.util.Scanner;
import java.util.HashMap;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import isel.sisinf.jpa.Dal;
import isel.sisinf.model.Cliente;
import isel.sisinf.model.Portefolio;
import isel.sisinf.model.ContactoCliente;

/**
 *
 * Didactic material to support
 * to the curricular unit of
 * Introduction to Information Systems
 *
 * The examples may not be complete and/or totally correct.
 * They are made available for teaching and learning purposes and
 * any inaccuracies are the subject of debate.
 */

interface DbWorker
{
    void doWork();
}
class UI implements AutoCloseable
{
    private enum Option
    {
        // DO NOT CHANGE ANYTHING!
        Unknown,
        Exit,
        createClient,
        createPortfolio,
        listPositions,
        updateInvestments,
        updateClient,
        about
    }
    private static UI __instance = null;
    private static Scanner __s = null;

    private HashMap<Option,DbWorker> __dbMethods;

    private UI()
    {
        // DO NOT CHANGE ANYTHING!
        __dbMethods = new HashMap<Option,DbWorker>();
        __dbMethods.put(Option.createClient, () -> UI.this.createClient());
        __dbMethods.put(Option.createPortfolio, () -> UI.this.createPortfolio());
        __dbMethods.put(Option.listPositions, () -> UI.this.listPositions());
        __dbMethods.put(Option.updateInvestments, () -> UI.this.updateInvestments());
        __dbMethods.put(Option.updateClient, () ->  UI.this.updateClient());
        __dbMethods.put(Option.about, new DbWorker() {public void doWork() {UI.this.about();}});
    }

    public static UI getInstance()
    {
        // DO NOT CHANGE ANYTHING!
        if(__instance == null)
        {
            __instance = new UI();
        }
        return __instance;
    }

    public static Scanner getScanner()
    {
        if(__s == null)
        {
            __s = new Scanner(System.in);
        }
        return __s;
    }

    private Option DisplayMenu()
    {
        Option option = Option.Unknown;
        Scanner s = getScanner();
        try
        {
            // DO NOT CHANGE ANYTHING!
            System.out.println("  ___ ___                 ");
            System.out.println(" | __| _ \\__ _ _  _ ___  ");
            System.out.println(" | _||  _/ _` | || (_-<  ");
            System.out.println(" |___|_| \\__,_|\\_,_/__/  ");
            System.out.println("        Management DEMO   ");
            System.out.println();
            System.out.println("1. Exit");
            System.out.println("2. Create Client");
            System.out.println("3. Create Portfolio");
            System.out.println("4. List Positions");
            System.out.println("5. Update Investments");
            System.out.println("6. Update Client");
            System.out.println("7. About");
            System.out.print(">");
            int result = s.nextInt();
            option = Option.values()[result];
        }
        catch(RuntimeException ex)
        {
            //nothing to do.
        }

        return option;

    }
    private static void clearConsole() throws Exception
    {
        // DO NOT CHANGE ANYTHING!
        for (int y = 0; y < 25; y++) //console is 80 columns and 25 lines
            System.out.println("\n");
    }

    public void Run() throws Exception
    {
        // DO NOT CHANGE ANYTHING!
        Option userInput;
        do
        {
            clearConsole();
            userInput = DisplayMenu();
            clearConsole();
            try
            {
                __dbMethods.get(userInput).doWork();
                System.in.read();
            }
            catch(NullPointerException ex)
            {
                //Nothing to do. The option was not a valid one. Read another.
            }

        }while(userInput!=Option.Exit);
    }

    /**
     To implement from this point forward.
     -------------------------------------------------------------------------------------
     IMPORTANT:
     --- DO NOT MESS WITH THE CODE ABOVE. YOU JUST HAVE TO IMPLEMENT THE METHODS BELOW ---
     --- Other Methods and properties can be added to support implementation.
     ---- Do that also below                                                         -----
     -------------------------------------------------------------------------------------

     */


    //Implement an AutoClosable object.
    // If needed you can add more stuff to clean at the end
    @Override
    public void close()
    {
        if(__s != null)
        {
            __s.close();
            __s = null;
        }
        Dal.close();
    }

    private String getFriendlyErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("ERROR: ")) {
            msg = msg.substring(msg.indexOf("ERROR: ") + 7);
            if (msg.contains("\n")) {
                msg = msg.substring(0, msg.indexOf('\n'));
            }
        }
        return msg;
    }

    private void createClient() {
        Scanner s = getScanner();
        System.out.print("NIF: ");
        String nif = s.nextLine();
        if (nif.isEmpty()) {
            nif = s.nextLine();
        }
        System.out.print("Cartao de Cidadao: ");
        String cc = s.nextLine();
        System.out.print("Nome: ");
        String name = s.nextLine();
        System.out.print("Tipo de Contacto (email / telefone): ");
        String contactType = s.nextLine();
        System.out.print("Contacto (ex: teste@gmail.com ou 912345678): ");
        String contact = s.nextLine();
        System.out.print("Descrição do Contacto (ex: Pessoal): ");
        String contactDesc = s.nextLine();

        EntityManager em = Dal.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ContactoCliente ccObj = new ContactoCliente(nif, cc, name, contactType, contact, contactDesc);
            em.persist(ccObj);
            tx.commit();
            System.out.println("Cliente e contacto criados com sucesso!");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("Erro ao criar cliente/contacto: " + getFriendlyErrorMessage(e));
        } finally {
            em.close();
        }
    }


    private void createPortfolio()
    {
        Scanner s = getScanner();
        System.out.print("NIF do Cliente: ");
        String nif = s.nextLine();
        if (nif.isEmpty()) {
            nif = s.nextLine();
        }
        System.out.print("Nome do Portefólio: ");
        String portfolioName = s.nextLine();

        EntityManager em = Dal.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cliente cliente = em.find(Cliente.class, nif);
            if (cliente == null) {
                System.out.println("Erro: Cliente não encontrado.");
                tx.rollback();
                return;
            }
            Portefolio p = new Portefolio(cliente, portfolioName);
            em.persist(p);
            tx.commit();
            System.out.println("Portefólio criado com sucesso!");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("Erro ao criar portefolio: " + getFriendlyErrorMessage(e));
        } finally {
            em.close();
        }
    }

    private void listPositions()
    {
        Scanner s = getScanner();
        System.out.print("NIF do Cliente: ");
        String nif = s.nextLine();
        if (nif.isEmpty()) {
            nif = s.nextLine();
        }

        EntityManager em = Dal.getEntityManager();
        try {
            List<Portefolio> portfolios = em.createQuery("SELECT p FROM Portefolio p WHERE p.cliente.nif = :nif", Portefolio.class)
                    .setParameter("nif", nif)
                    .getResultList();

            if (portfolios.isEmpty()) {
                System.out.println("Nenhum portefólio encontrado para este cliente.");
                return;
            }

            double grandTotal = 0;
            for (Portefolio p : portfolios) {
                Long portfolioId = p.getPortefolioId();

                @SuppressWarnings("unchecked")
                List<Object[]> positions = em.createNativeQuery("SELECT * FROM fx_portefolio_info(?)")
                        .setParameter(1, portfolioId)
                        .getResultList();

                System.out.println("\nPortefólio ID: " + portfolioId + " - Nome: " + p.getNome());

                if (positions.isEmpty()) {
                    System.out.println("Este portefólio nao tem posições.");
                    continue;
                }

                System.out.printf("%-15s %-15s %-15s %-15s %-15s\n", "ISIN", "Quantidade", "Valor Atual", "% Var Diária", "Valor Total");
                double portfolioTotal = 0;
                for (Object[] pos : positions) {
                    String isin = (String) pos[0];
                    java.math.BigDecimal quantity = (java.math.BigDecimal) pos[1];
                    java.math.BigDecimal currentValue = (java.math.BigDecimal) pos[2];
                    java.math.BigDecimal dailyVar = (java.math.BigDecimal) pos[3];
                    double totalValue = quantity.doubleValue() * currentValue.doubleValue();
                    portfolioTotal += totalValue;
                    System.out.printf("%-15s %-15.4f %-15.2f %-15.2f %-15.2f\n", isin, quantity, currentValue, dailyVar, totalValue);
                }
                System.out.printf("Total deste portefolio: %.2f Eur\n", portfolioTotal);
                grandTotal += portfolioTotal;
            }
            System.out.printf("\nTOTAL GERAL DE TODOS OS PORTEFOLIOS: %.2f Eur\n", grandTotal);

        } catch (Exception e) {
            System.out.println("Erro ao listar posicoes: " + getFriendlyErrorMessage(e));
        } finally {
            em.close();
        }
    }

    private void updateInvestments() {
        EntityManager em = Dal.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("CALL p_actualizaValorDiario()").executeUpdate();
            tx.commit();
            System.out.println("Valores de investimento atualizados com sucesso!");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("Erro ao atualizar investimentos: " + getFriendlyErrorMessage(e));
        } finally {
            em.close();
        }
    }

    private void updateClient()
    {
        Scanner s = getScanner();
        System.out.print("NIF do Cliente a atualizar: ");
        String nif = s.nextLine();
        if (nif.isEmpty()) {
            nif = s.nextLine();
        }

        EntityManager em = Dal.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cliente cliente = em.find(Cliente.class, nif);
            if (cliente == null) {
                System.out.println("Cliente não encontrado!");
                tx.rollback();
                return;
            }

            System.out.println("Nome atual: " + cliente.getNome());
            System.out.print("Novo Nome (deixe em branco para manter o atual): ");
            String newName = s.nextLine();
            if (!newName.isEmpty()) {
                cliente.setNome(newName);
            }

            System.out.println("Cartão de Cidadão atual: " + cliente.getCartaoCidadao());
            System.out.print("Novo Cartão de Cidadão (deixe em branco para manter o atual): ");
            String newCC = s.nextLine();
            if (!newCC.isEmpty()) {
                cliente.setCartaoCidadao(newCC);
            }

            em.merge(cliente);
            tx.commit();
            System.out.println("Cliente atualizado com sucesso!");

        } catch (OptimisticLockException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("ERRO: Os dados deste cliente foram modificados por outro utilizador enquanto tentava guardar. A transação foi cancelada (Optimistic Locking). Tente novamente.");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("Erro ao atualizar cliente: " + getFriendlyErrorMessage(e));
        } finally {
            em.close();
        }
    }

    private void about()
    {
        // TODO: Change the code and your Group ID & member names
        System.out.println("Brought to you by Group 49!");
        System.out.println("Fernando Duarte e Gabriel Ferreira!");
        System.out.println("DAL version:"+ isel.sisinf.jpa.Dal.version());
        System.out.println("Core version:"+ isel.sisinf.model.Core.version());

    }
}

public class App{
    public static void main(String[] args) throws Exception{
        try(UI ui = UI.getInstance())
        {
            ui.Run();
        }
    }
}
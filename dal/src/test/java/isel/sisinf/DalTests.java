package isel.sisinf;

import isel.sisinf.jpa.Dal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Unit test for simple App.
 */
public class DalTests
{

    @Test
    public void checkTriggerNIFIsNull(){

        EntityManager em = Dal.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.createNativeQuery("""
                INSERT INTO cliente(nif)
                VALUES (NULL)
            """).executeUpdate();

            fail("Expected trigger NIF Null Exception");
        }
        catch (Exception e){
            System.out.println("Expected exception:");
            System.out.println(e.getMessage());
        }
        finally {

            if (tx.isActive()) {
                tx.rollback();
            }

            em.close();
        }
    }

    @Test
    public void checkInvalidNIF() {

        EntityManager em = Dal.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            em.createNativeQuery("""
                INSERT INTO cliente(nif)
                VALUES ('12345')
            """).executeUpdate();

            throw new AssertionError(
                    "Expected trigger exception"
            );

        }
        catch (Exception e) {

            System.out.println("Expected exception:");
            System.out.println(e.getMessage());

        }
        finally {

            if (tx.isActive()) {
                tx.rollback();
            }

            em.close();
        }
    }

    @Test
    public void checkValidNIF() {

        EntityManager em = Dal.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            em.createNativeQuery("""
                INSERT INTO cliente(nif, cartao_cidadao, nome)
                VALUES ('123456789', '12345678', 'Cliente Teste')
            """).executeUpdate();

            // success expected

        }
        catch (Exception e) {
            fail("Insert should have succeeded: " + e.getMessage());
        }
        finally {

            if (tx.isActive()) {
                tx.rollback();
            }

            em.close();
        }
    }

}

package hellojpa;

import org.hibernate.Hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setUsername("member");
            em.persist(member);

            em.flush();
            em.clear();

            Member reference = em.getReference(Member.class, member.getId());
            System.out.println("reference = " + reference.getClass()); //Proxy
            Hibernate.initialize(reference); //강제초기화

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println("e = "+ e);
        } finally {
            em.close();
        }
        emf.close();
    }

}

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
            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member");
            member.setTeam(team);
            em.persist(member);

            em.flush();
            em.clear();

            Member m = em.find(Member.class, member.getId());
            System.out.println("m = " + m.getTeam().getClass());

            System.out.println("======================");
            System.out.println("teamName = " + m.getTeam().getName());
            System.out.println("======================");

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

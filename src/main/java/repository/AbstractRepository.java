package repository;

import exception.EmptyRepositoryResultException;
import exception.RepositoryAccessException;
import model.Entitiy;

import java.sql.SQLException;
import java.util.List;

/**
 * Apstraktna generička klasa koja definira osnovni ugovor za sve repozitorije u aplikaciji.
 * Svaki repozitorij koji radi s entitetima koji nasljeđuju {@link Entitiy} treba naslijediti ovu klasu.
 *
 * @param <T> Tip entiteta s kojim repozitorij radi (npr. Client, Proposal).
 */
public abstract class AbstractRepository<T extends Entitiy> {
        /**
         * Pronalazi entitet prema njegovom jedinstvenom ID-ju.
         *
         * @param id ID entiteta koji se traži.
         * @return Pronađeni entitet.
         * @throws EmptyRepositoryResultException ako entitet s danim ID-jem nije pronađen.
         * @throws SQLException ako dođe do greške prilikom pristupa bazi podataka.
         * @throws RepositoryAccessException ako dođe do općenite greške pri pristupu repozitoriju.
         */
        public abstract T findById(Long id) throws EmptyRepositoryResultException, SQLException, RepositoryAccessException;

        /**
         * Dohvaća sve entitete iz repozitorija.
         *
         * @return Lista svih entiteta.
         * @throws RepositoryAccessException ako dođe do greške prilikom dohvaćanja podataka.
         */
        public abstract List<T> findAll() throws RepositoryAccessException;

        /**
         * Sprema listu entiteta u repozitorij.
         * Obično se koristi za batch operacije.
         *
         * @param entities Lista entiteta za spremanje.
         * @throws RepositoryAccessException ako spremanje ne uspije.
         * @throws SQLException ako dođe do greške prilikom pristupa bazi podataka.
         */
        public abstract void save(List<T> entities) throws RepositoryAccessException, SQLException;

        /**
         * Sprema jedan entitet u repozitorij.
         *
         * @param entity Entitet koji se sprema.
         * @throws RepositoryAccessException ako spremanje ne uspije.
         */
        public abstract void save(T entity) throws RepositoryAccessException;
}
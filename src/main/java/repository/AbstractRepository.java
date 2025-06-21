package repository;

import exception.EmptyRepositoryResultException;
import exception.RepositoryAccessException;
import model.Entitiy;

import java.sql.SQLException;
import java.util.List;

public abstract class AbstractRepository<T extends Entitiy> {
        /**
         * Finds an entity by its unique ID.
         *
         * @param id The ID of the entity.
         * @return The entity if found.
         * @throws EmptyRepositoryResultException if no entity is found.
         * @throws SQLException if a database error occurs.
         */
        public abstract T findById(Long id) throws EmptyRepositoryResultException, SQLException;
        /**
         * Retrieves all entities from the repository.
         *
         * @return A list of all entities.
         * @throws RepositoryAccessException if an error occurs during retrieval.
         */
        public abstract List<T> findAll() throws RepositoryAccessException;
        /**
         * Saves multiple entities to the repository.
         *
         * @param entities A list of entities to save.
         * @throws RepositoryAccessException if saving fails.
         * @throws SQLException if a database error occurs.
         */
        public abstract void save(List<T> entities) throws RepositoryAccessException, SQLException;
        /**
         * Saves a single entity to the repository.
         *
         * @param entity The entity to save.
         * @throws RepositoryAccessException if saving fails.
         */
        public abstract void save(T entity) throws RepositoryAccessException;
}

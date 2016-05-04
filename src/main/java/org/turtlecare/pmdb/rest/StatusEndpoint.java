package org.turtlecare.pmdb.rest;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

/**
 * 
 */
@Stateless
@Path("/statuses")
public class StatusEndpoint {
	@PersistenceContext(unitName = "PMDB")
	private EntityManager em;

	@POST
	@Consumes("application/json")
	public Response create(org.turtlecare.pmdb.model.Status entity) {
		em.persist(entity);
		return Response.created(
				UriBuilder.fromResource(StatusEndpoint.class)
						.path(String.valueOf(entity.getId())).build()).build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") int id) {
		org.turtlecare.pmdb.model.Status entity = em.find(org.turtlecare.pmdb.model.Status.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("id") int id) {
		TypedQuery<org.turtlecare.pmdb.model.Status> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT s FROM Status s WHERE s.id = :entityId ORDER BY s.id",
						org.turtlecare.pmdb.model.Status.class);
		findByIdQuery.setParameter("entityId", id);
		org.turtlecare.pmdb.model.Status entity;
		try {
			entity = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			entity = null;
		}
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(entity).build();
	}

	@GET
	@Produces("application/json")
	public List<org.turtlecare.pmdb.model.Status> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		TypedQuery<org.turtlecare.pmdb.model.Status> findAllQuery = em.createQuery(
				"SELECT DISTINCT s FROM Status s ORDER BY s.id", org.turtlecare.pmdb.model.Status.class);
		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}
		if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}
		final List<org.turtlecare.pmdb.model.Status> results = findAllQuery.getResultList();
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(@PathParam("id") int id, org.turtlecare.pmdb.model.Status entity) {
		if (entity == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (id != entity.getId()) {
			return Response.status(Status.CONFLICT).entity(entity).build();
		}
		if (em.find(org.turtlecare.pmdb.model.Status.class, id) == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
			entity = em.merge(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		}

		return Response.noContent().build();
	}
}

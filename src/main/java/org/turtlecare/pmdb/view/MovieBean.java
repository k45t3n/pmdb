package org.turtlecare.pmdb.view;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.PhaseId;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.turtlecare.pmdb.model.Movie;
import java.util.Iterator;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.turtlecare.pmdb.model.Actor;
import org.turtlecare.pmdb.model.Genre;
import org.turtlecare.pmdb.model.Status;

/**
 * Backing bean for Movie entities.
 * <p/>
 * This class provides CRUD functionality for all Movie entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class MovieBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Movie entities
	 */

	private Integer id;
	
	public StreamedContent getImage() {
		DefaultStreamedContent streamedContent = new DefaultStreamedContent();

		FacesContext context = FacesContext.getCurrentInstance();		
		if (context.getCurrentPhaseId() != PhaseId.RENDER_RESPONSE) {
			String passedId = context.getExternalContext().getRequestParameterMap().get("movieId");
			Movie movie = findById(Integer.parseInt(passedId));
			if (movie != null) { 
				byte[] byteArr = movie.getPoster();
				if (byteArr != null) {
					streamedContent = new DefaultStreamedContent(new ByteArrayInputStream(byteArr));
				}
			}
		}

		return streamedContent;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Movie movie;

	public Movie getMovie() {
		return this.movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}

	@Inject
	private Conversation conversation;

	@PersistenceContext(unitName = "PMDB", type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	public String create() {

		this.conversation.begin();
		this.conversation.setTimeout(1800000L);
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.conversation.isTransient()) {
			this.conversation.begin();
			this.conversation.setTimeout(1800000L);
		}

		if (this.id == null) {
			this.movie = this.example;
		} else {
			this.movie = findById(getId());
		}
	}

	public Movie findById(Integer id) {

		return this.entityManager.find(Movie.class, id);
	}

	/*
	 * Support updating and deleting Movie entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				this.entityManager.persist(this.movie);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.movie);
				return "view?faces-redirect=true&id=" + this.movie.getId();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		try {
			Movie deletableEntity = findById(getId());
			Iterator<Actor> iterActors = deletableEntity.getActors().iterator();
			for (; iterActors.hasNext();) {
				Actor nextInActors = iterActors.next();
				nextInActors.getMovies().remove(deletableEntity);
				iterActors.remove();
				this.entityManager.merge(nextInActors);
			}
			Iterator<Genre> iterGenres = deletableEntity.getGenres().iterator();
			for (; iterGenres.hasNext();) {
				Genre nextInGenres = iterGenres.next();
				nextInGenres.getMovies().remove(deletableEntity);
				iterGenres.remove();
				this.entityManager.merge(nextInGenres);
			}
			Status status = deletableEntity.getStatus();
			status.getMovies().remove(deletableEntity);
			deletableEntity.setStatus(null);
			this.entityManager.merge(status);
			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Movie entities with pagination
	 */

	private int page;
	private long count;
	private List<Movie> pageItems;

	private Movie example = new Movie();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Movie getExample() {
		return this.example;
	}

	public void setExample(Movie example) {
		this.example = example;
	}

	public String search() {
		this.page = 0;
		return null;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Movie> root = countCriteria.from(Movie.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Movie> criteria = builder.createQuery(Movie.class);
		root = criteria.from(Movie.class);
		TypedQuery<Movie> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Movie> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String title = this.example.getTitle();
		if (title != null && !"".equals(title)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("title")),
					'%' + title.toLowerCase() + '%'));
		}
		Status status = this.example.getStatus();
		if (status != null) {
			predicatesList.add(builder.equal(root.get("status"), status));
		}
		String review = this.example.getReview();
		if (review != null && !"".equals(review)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("review")),
					'%' + review.toLowerCase() + '%'));
		}
		byte[] poster = this.example.getPoster();
		if (poster != null) {
			predicatesList.add(builder.equal(root.get("poster"), poster));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Movie> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Movie entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Movie> getAll() {

		CriteriaQuery<Movie> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Movie.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Movie.class))).getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final MovieBean ejbProxy = this.sessionContext
				.getBusinessObject(MovieBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context,
					UIComponent component, String value) {

				return ejbProxy.findById(Integer.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context,
					UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((Movie) value).getId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Movie add = new Movie();

	public Movie getAdd() {
		return this.add;
	}

	public Movie getAdded() {
		Movie added = this.add;
		this.add = new Movie();
		return added;
	}
}

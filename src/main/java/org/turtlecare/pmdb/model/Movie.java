package org.turtlecare.pmdb.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;

/**
 * The persistent class for the movie database table.
 * 
 */
@Entity
@Table(name = "movie")
@NamedQuery(name = "Movie.findAll", query = "SELECT m FROM Movie m")
@XmlRootElement
public class Movie implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;

	@Temporal(TemporalType.DATE)
	@Column(name = "date_of_release", nullable = false)
	private Date dateOfRelease;

	@Column(nullable = false, length = 128)
	private String title;

	@Lob
	private byte[] poster;

	//bi-directional many-to-many association to Actor
	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "movie_actor_map", joinColumns = {@JoinColumn(name = "movie_id", nullable = false)}, inverseJoinColumns = {@JoinColumn(name = "actor_id", nullable = false)})
	private Set<Actor> actors;

	//bi-directional many-to-many association to Genre
	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "movie_genre_map", joinColumns = {@JoinColumn(name = "movie_id", nullable = false)}, inverseJoinColumns = {@JoinColumn(name = "genre_id", nullable = false)})
	private Set<Genre> genres;

	//uni-directional many-to-one association to Status
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "status_id")
	private Status status;

	@Column(length = 2048)
	private String review;

	public Movie() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDateOfRelease() {
		return this.dateOfRelease;
	}

	public void setDateOfRelease(Date dateOfRelease) {
		this.dateOfRelease = dateOfRelease;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public byte[] getPoster() {
		return poster;
	}

	public void setPoster(byte[] poster) {
		this.poster = poster;
	}

	public Set<Actor> getActors() {
		return this.actors;
	}

	public void setActors(Set<Actor> actors) {
		this.actors = actors;
	}

	public Set<Genre> getGenres() {
		return this.genres;
	}

	public void setGenres(Set<Genre> genres) {
		this.genres = genres;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (title != null && !title.trim().isEmpty())
			result += "title: " + title;
		return result;
	}

}
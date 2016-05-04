package org.turtlecare.pmdb.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the status database table.
 * 
 */
@Entity
@Table(name = "status")
@NamedQuery(name = "Status.findAll", query = "SELECT s FROM Status s")
@XmlRootElement
public class Status implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;

	@Column(nullable = false, length = 32)
	private String name;

	//bi-directional many-to-one association to Movie
	@JsonIgnore
	@OneToMany(mappedBy = "status")
	private Set<Movie> movies;

	public Status() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Movie> getMovies() {
		return this.movies;
	}

	public void setMovies(Set<Movie> movies) {
		this.movies = movies;
	}

	public Movie addMovie(Movie movie) {
		getMovies().add(movie);
		movie.setStatus(this);

		return movie;
	}

	public Movie removeMovie(Movie movie) {
		getMovies().remove(movie);
		movie.setStatus(null);

		return movie;
	}

	public boolean equals(Object obj) {
		final Status other = (Status) obj;
		if (name == null) {
			if (other.getName() != null) {
				return false;
			}			
		} else if (name.equals(other.getName()) == false) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (name != null && !name.trim().isEmpty())
			result += "name: " + name + " ";
		return result;
	}
	
}
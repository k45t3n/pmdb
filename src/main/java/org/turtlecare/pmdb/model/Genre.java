package org.turtlecare.pmdb.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the genre database table.
 * 
 */
@Entity
@Table(name = "genre")
@NamedQuery(name = "Genre.findAll", query = "SELECT g FROM Genre g")
@XmlRootElement
public class Genre implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;

	@Column(nullable = false, length = 32)
	private String name;

	//bi-directional many-to-many association to Movie
	@JsonIgnore
	@ManyToMany(mappedBy = "genres")
	private Set<Movie> movies;

	public Genre() {
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

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (name != null && !name.trim().isEmpty())
			result += "name: " + name + " ";
		return result;
	}

}
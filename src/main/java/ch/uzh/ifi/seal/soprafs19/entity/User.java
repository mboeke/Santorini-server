package ch.uzh.ifi.seal.soprafs19.entity;

import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@DynamicUpdate
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(nullable = false, updatable = false)
	@GeneratedValue
	private Long id;
	
	@Column(nullable = false)
	@NotEmpty
	private String name;
	
	@Column(nullable = false, unique = true)
	@NotEmpty
	private String username;

	@Column(nullable = false)
	@NotEmpty
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@Column
	private Date birthday;

	@Column(nullable = true, unique = true)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String token;

	@Column(nullable = false)
	private UserStatus status;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdOn;

	@OneToOne
	private Game game;

	public Long getId() {
		return id;
	}

	public void setId(Long id) { this.id = id;	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public LocalDateTime getCreatedOn() {return createdOn;}

	@JsonIgnore
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	@JsonIgnore
	public Game getGame() { return game;}

	public void setGame(Game game) { this.game = game;	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof User)) {
			return false;
		}
		User user = (User) o;
		return this.getId().equals(user.getId());
	}
}

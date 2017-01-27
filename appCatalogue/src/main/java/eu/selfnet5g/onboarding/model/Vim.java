package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Vim {
	@Id
	@JsonIgnore
	protected String id;
		
	private String  name;
	private VimType type;
	private String  url;
	private String  username;
	private String  password;
	private String  tenant;
		
	public Vim() {
		//JPA only
	}
	
	public Vim(String  name,
			   VimType type,
	           String  url,
	           String  user,
	           String  passwd,
	           String  tenant) {
		this.name = name;
		this.type = type;
		this.url = url;
		this.username = user;
		this.password = passwd;
		this.tenant = tenant;
	}

	@JsonIgnore
	@PrePersist
	public void ensureId() {
		id = UUID.randomUUID().toString();
	}
	
	@JsonIgnore
	public String getId() {
		return id;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("type")
	public VimType getType() {
		return type;
	}

	@JsonProperty("url")
	public String getUrl() {
		return url;
	}

	@JsonProperty("username")
	public String getUsername() {
		return username;
	}

	@JsonProperty("password")
	public String getPassword() {
		return password;
	}

	@JsonProperty("tenant")
	public String getTenant() {
		return tenant;
	}
	
}

package infomediaservice.authenticationserver.models;


public class Configuracion {

	private Long id;

	private String user;
	private String password;
	private String server;
	private Long port;
	private String database;
	private String sslMode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Long getPort() {
		return port;
	}

	public void setPort(Long port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getSslMode() {
		return sslMode;
	}

	public void setSslMode(String sslMode) {
		this.sslMode = sslMode;
	}

}

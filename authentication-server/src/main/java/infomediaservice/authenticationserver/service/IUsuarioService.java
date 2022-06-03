package infomediaservice.authenticationserver.service;

import infomediaservice.authenticationserver.models.Usuario;

public interface IUsuarioService {
	
	public Usuario findByUsername(String username);
}

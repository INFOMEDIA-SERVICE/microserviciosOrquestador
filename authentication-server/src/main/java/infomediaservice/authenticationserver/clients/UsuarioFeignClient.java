package infomediaservice.authenticationserver.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import infomediaservice.authenticationserver.models.Usuario;

@FeignClient(name="usuario-server")
public interface UsuarioFeignClient {
	
	@GetMapping("/usuarios/search/buscar-username")
	public Usuario findByUsername(@RequestParam String username);
}

package curso.api.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;

@Service
public class ImplementacaoUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		/*Consultar no banco o usuario*/
		Usuario usuario = usuarioRepository.findUserByUserLogin(username);
		
		if (usuario == null) {
			throw new UsernameNotFoundException("usuário não encontrado");
		}
		
		return new User(usuario.getLogin(), usuario.getPassword(), usuario.getAuthorities());
	}

	public void insereAcessoPadrao(Long id) {

		/*primeiro passo descobrir constraint*/
		String constraint = usuarioRepository.consultaConstraintRole();
		
		if(constraint != null) {
		/*segundo passo drop constraint*/
		jdbcTemplate.execute("ALTER table usuarios_role DROP CONSTRAINT "+constraint+"");
		}
		
		/*inserir acessos padrao*/
		usuarioRepository.insereAcessoPadrao(id);
		
	}

}

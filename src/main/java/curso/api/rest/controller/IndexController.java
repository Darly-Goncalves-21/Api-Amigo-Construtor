package curso.api.rest.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import curso.api.rest.model.Usuario;
import curso.api.rest.model.UsuarioDTO;
import curso.api.rest.repository.UsuarioRepository;

@RestController /* Arquitetura REST */
@RequestMapping(value = "/usuario")
public class IndexController {
	
	@Autowired /* de fosse CDI seria @Inject*/
	private UsuarioRepository usuarioRepository;
	
	
	/* Serviço RESTful */
	@GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable (value = "id") Long id
			                                , @PathVariable (value = "venda") Long venda) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		/*o retorno seria um relatorio*/
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
/*
	// Serviço RESTful 
	@GetMapping(value = "/{id}", produces = "application/json")
	@CacheEvict(value ="cacheusuario", allEntries = true)
	@CachePut("cacheusuario")
	public ResponseEntity<UsuarioDTO> initV1(@PathVariable (value = "id") Long id) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		return new ResponseEntity<UsuarioDTO>(new UsuarioDTO(usuario.get()), HttpStatus.OK);
		
	}*/
	
	/* Serviço RESTful */
	@GetMapping(value = "/{id}", produces = "application/json")
	@CacheEvict(value ="cacheusuario", allEntries = true)
	@CachePut("cacheusuario")
	public ResponseEntity<Optional<Usuario>> usuarioId(@PathVariable (value = "id") Long id) {
		
	//	Optional<Usuario> usuario = usuarioRepository.findById(id);
		Optional<Usuario> usuario = usuarioRepository.findById(id);

		
		return new ResponseEntity<Optional<Usuario>>(usuario, HttpStatus.OK);
		
	}
	
	/*
	@GetMapping(value = "/", produces = "application/json")
	@CacheEvict(value ="cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<List<Usuario>> usuario () throws InterruptedException{
		
		List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();
		
		//Thread.sleep(6000);
		
		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
	}
	*/
	
	
	/* Serviço RESTful */
	@GetMapping(value = "v2/{id}", produces = "application/json")
	public ResponseEntity<Usuario> initV2(@PathVariable (value = "id") Long id) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		System.out.println("Versão 2");
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete (@PathVariable("id") Long id){
		
		usuarioRepository.deleteById(id);
		
		return "ok";
	}
	
	
	@DeleteMapping(value = "/{id}/venda", produces = "application/text")
	public String deletevenda(@PathVariable("id") Long id){
		
		usuarioRepository.deleteById(id);
		
		return "ok";
	}
	
	
	@GetMapping(value = "/", produces = "application/json")
	@CacheEvict(value ="cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<List<Usuario>> usuario () throws InterruptedException{
		
		List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();
		
		//Thread.sleep(6000);
		
		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
	}
	
	@GetMapping(value = "/lista-por-nome/{nome}", produces = "application/json")
	@CacheEvict(value ="cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<List<Usuario>> usuarioPorNome (@PathVariable("nome") String nome) throws InterruptedException{
		
			List<Usuario> list = (List<Usuario>) usuarioRepository.findUserByUserNome(nome);
			return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
		
		//Thread.sleep(6000);
		
	}
	
	
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) throws Exception {
		
		for (int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		
		//Consumindo API Externa
		URL url = new URL("https://viacep.com.br/ws/"+usuario.getCep()+"/json/");
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		
		String cep = "";
		StringBuilder jsonCep = new StringBuilder();
		
		while ((cep = br.readLine()) != null) {
			jsonCep.append(cep);
		}
		
		Usuario userAux = new Gson().fromJson(jsonCep.toString(), Usuario.class);
		usuario.setCep(userAux.getCep());
		usuario.setLogradouro(userAux.getLogradouro());
		usuario.setComplmento(userAux.getComplmento());
		usuario.setBairro(userAux.getBairro());
		usuario.setLocalidade(userAux.getLocalidade());
		usuario.setUf(userAux.getUf());
		
		//Consumindo API Externa
		
		String senhaCripto = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhaCripto);
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
		
	}
	
	
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {
		
		/*outras rotinas antes de atualizar*/
		
		for (int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		
		Usuario userTemporario = usuarioRepository.findById(usuario.getId()).get();
		
		if(!userTemporario.getSenha().equals(usuario.getSenha())) {

			String senhaCripto = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senhaCripto);
		}
		
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
		
	}
	
	
	
	@PutMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity updateVenda(@PathVariable Long iduser, 
			                                     @PathVariable Long idvenda) {
		/*outras rotinas antes de atualizar*/
		
		//Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity("Venda atualzada", HttpStatus.OK);
		
	}
	
	
	@PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity cadastrarvenda(@PathVariable Long iduser, 
			                                     @PathVariable Long idvenda) {
		
		/*Aqui seria o processo de venda*/
		//Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity("id user :" + iduser + " idvenda :"+ idvenda, HttpStatus.OK);
		
	}
	
	
	

}
package curso.api.rest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.api.rest.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	
	@Query("select u from Usuario u where u.login = ?1")
	Usuario  findUserByUserLogin(String login);
	
	@Query("select u from Usuario u where u.nome like %?1%")
	List<Usuario>  findUserByUserNome(String nome);
	
	@Query(value= "select constraint_name from information_schema.constraint_column_usage where table_name = 'usuarios_role'\r\n"
			+ "and column_name = 'role_id' and constraint_name <> 'unique_role_user'", nativeQuery = true)
	String consultaConstraintRole();
	
	@Transactional
	@Modifying
	@Query(value="ALTER table usuarios_role DROP CONSTRAINT ?1",nativeQuery = true)
	void removerConstraintRole(String constraint);
	
	@Transactional
	@Modifying
	@Query(value="insert into usuarios_role (usuario_id, role_id)\r\n"
			+ "values (?1,(select id from role where nome_role = 'ROLE_USER')) ",nativeQuery = true)
	void insereAcessoPadrao(Long idUser);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value="update usuario set token = ?1 where login = ?2")
	void atualizarTokenUser(String token, String login );

}

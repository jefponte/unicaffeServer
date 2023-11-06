package br.edu.unilab.unicaffe.dao;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import br.edu.unilab.unicaffe.api.Resource;
import br.edu.unilab.unicaffe.model.Usuario;

/**
 * 
 * Esta classe gerencia persistência no banco de dados de objetos Usuario. 
 * 
 * @author Jefferson Uchôa Ponte
 *
 */
public class UsuarioDAO extends DAO {

	/**
	 * Constroi objeto UsuarioDAO
	 */
	public UsuarioDAO() {
		super();
	}

	/**
	 * Constroi objeto UsuarioDAO
	 */
	public UsuarioDAO(int tipoDeConexao) {
		super(tipoDeConexao);

	}

	/**
	 * Constroi objeto UsuarioDAO
	 */
	public UsuarioDAO(Connection conexao) {
		super(conexao);
	}

	/**
	 * Entre com um objeto Usuario contendo login e senha, retorno true caso a correspondência seja verdadeira. 
	 * @param usuario
	 * @return
	 */
	public boolean autenticaLocal(Usuario usuario) {
		try {
			PreparedStatement ps = this.getConexao().prepareStatement("SELECT * FROM usuario WHERE login = ? AND senha = ?");
			
			ps.setString(1, usuario.getLogin());
			ps.setString(2, usuario.getSenha());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				usuario.setId(rs.getInt("id_usuario"));
				usuario.setNome(rs.getString("nome"));
				usuario.setEmail(rs.getString("email"));
				usuario.setIdBaseExterna(rs.getInt("id_base_externa"));
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return false;

	}

	/**
	 * Retorna true se o nível do discente for graduação. 
	 * @param usuario
	 * @return
	 */
	public boolean seuNivelEhGraduacao(Usuario usuario) {
		try {

			PreparedStatement ps = this.getConexao()
					.prepareStatement("SELECT * FROM vw_usuarios_unicafe WHERE id_usuario = ?");
			ps.setInt(1, usuario.getIdBaseExterna());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getString("nivel_discente") == null) {
					this.getConexao().close();
					return false;
				}
				if (!(rs.getString("nivel_discente").substring(0, 1).equals("G"))) {
					this.getConexao().close();
					return false;
				}
			}
			this.getConexao().close();
		} catch (SQLException e) {

			e.printStackTrace();

			return false;
		}

		return true;

	}
	/**
	 * Entre com um objeto Usuario contendo login e senha, retorno true caso a correspondência seja verdadeira. 
	 * Tenta primeiro na base local do sistema, caso não exista tenta na base de dados da instituição. 
	 * 
	 * @param usuario
	 * @return
	 */

	public boolean autentica(Usuario usuario) {
		if (this.autenticaLocal(usuario)) {
			System.out.println("Usuario Autenticado Localmente: "+usuario.getId());
			return true;
		}
		try {
			this.getConexao().close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		this.setTipoDeConexao(TIPO_USUARIOS);
		this.fazerConexao();
		if (this.autenticaRemoto(usuario)) {
			try {
				this.getConexao().close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
			this.setTipoDeConexao(TIPO_DEFAULT);
			this.fazerConexao();
			
			this.cadastra(usuario);
			try {
				this.getConexao().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return true;
		}
		try {
			this.getConexao().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;

	}

	/**
	 * Entre com um objeto Usuario contendo login e senha, retorno true caso a correspondência seja verdadeira.
	 * Verificação feita na base de dados de autenticação da instituição.  
	 * @param usuario
	 * @return
	 */
	public boolean autenticaRemoto(Usuario usuario) {

		Resource resource = new Resource();
		int id = resource.authenticate(usuario);
		if(id == 0) {
			return false;
		}
		System.out.println("Usuario autenticado pela API: "+id);
		
		this.setTipoDeConexao(TIPO_USUARIOS);

		try {
			PreparedStatement ps = this.getConexao()
					.prepareStatement("SELECT * FROM vw_usuarios_unicafe WHERE id_usuario = ? LIMIT 1");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println("Usuario autenticado pela API localizado na base remota de usuarios");
				usuario.setIdBaseExterna(rs.getInt("id_usuario"));
				usuario.setId(rs.getInt("id_usuario"));
				usuario.setNome(rs.getString("nome"));
				usuario.setEmail(rs.getString("email"));
				return true;
			}
		} catch (SQLException e) {

			e.printStackTrace();
			return false;
		}
		return false;
	}

	

	/**
	 * Tenta cadastrar usuário na base de dados local. 
	 * Retornar verdadeiro caso o cadastro tenha dado certo, falso se o
	 * cadastro tiver dado errado. 
	 * 
	 * @param usuario
	 * @return
	 */
	public boolean cadastra(Usuario usuario) {
		try {
			System.out.println("Localizando usuario para cadastrar ou atualizar: "+usuario.getLogin());
			PreparedStatement ps = this.getConexao().prepareStatement("SELECT * FROM usuario WHERE login = ? ");

			ps.setString(1, usuario.getLogin());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println("Atualizando Senha do Usuario na base Local: "+usuario.getLogin());
				PreparedStatement psUpdate = this.getConexao()
						.prepareStatement("UPDATE usuario set senha = ? WHERE login = ?");
				psUpdate.setString(1, usuario.getSenha());
				psUpdate.setString(2, usuario.getLogin());
				psUpdate.executeUpdate();

				usuario.setId(rs.getInt("id_usuario"));

				return false;
			}
			System.out.println("Inserir novo usuario na base local: "+usuario.getNome());
			// nome email login senha nivel_acesso
			PreparedStatement ps2 = this.getConexao().prepareStatement(
					"INSERT into usuario(nome, email, login, senha, id_base_externa, nivel_acesso) VALUES(?, ?, ?, ?, ?, ?)");

			ps2.setString(1, usuario.getNome());
			ps2.setString(2, usuario.getEmail());
			ps2.setString(3, usuario.getLogin());
			ps2.setString(4, usuario.getSenha());
			ps2.setInt(5, usuario.getId());
			ps2.setInt(6, 1);
			ps2.executeUpdate();
			cadastra(usuario);
			return true;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Retorna uma lista de usuários do banco de dados. 
	 * @return lista de usuários. 
	 * @throws SQLException
	 */
	public Collection<Usuario> retornaLista() {

		Collection<Usuario> lista = new HashSet<Usuario>();
		PreparedStatement ps;
		try {
			ps = this.getConexao().prepareStatement("SELECT * FROM usuario");
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				Usuario usuario = new Usuario();

				usuario.setLogin(resultSet.getString("login"));
				usuario.setNome(resultSet.getString("nome"));
				usuario.setId(resultSet.getInt("id_usuario"));
				usuario.setSenha(resultSet.getString("senha"));

				lista.add(usuario);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lista;
	}


	/**
	 * Gera criptografia MD5 de uma String. 
	 * @param input
	 * @return
	 */
	
	public static String getMD5(String input) {
		byte[] source;
		try {
			source = input.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			source = input.getBytes();
		}
		String result = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			// The result should be one 128 integer
			byte temp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = temp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			result = new String(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}

package br.edu.unilab.unicaffe.model;

/**
 * 
 * Classe que agrega dados do usuário; 
 * @author Jefferson Uchôa Ponte
 * 
 *
 */

public class Usuario {
	private String name;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name; 
	}
	/**
	 * ID do usuário no banco de dados. 
	 */
	private int id;
	/**
	 * Nome do usuário. 
	 */
	private String nome;
	/**
	 * E-mail do Usuário. 
	 */
	private String email;
	/**
	 * Login do usuário. 
	 */
	private String login;
	/**
	 * Senha do Usuário. 
	 */
	private String senha;
	/**
	 * Nível de acesso do usuário.
	 */
	
	private int nivelAcesso;
	/**
	 * ID do usuário na base de origem. 
	 */
	private int idBaseExterna;
	/**
	 * Cadastro de Pessoa Física do Usuário. 
	 */
	private String cpf;


	/**
	 * 
	 * @return ID do usuário. 
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id ID do usuário. 
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * 
	 * @return Nome do Usuário
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * 
	 * @param nome Nome do Usuário. 
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * 
	 * @return E-mail do Usuário. 
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * 
	 * @param email E-mail do Usuário. 
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * 
	 * @return Login do Usuário. 
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * 
	 * @param login Login do Usuário. 
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * 
	 * @return Senha do Usuário. 
	 */
	public String getSenha() {
		return senha;
	}

	/**
	 * 
	 * @param senha senha do usuário. 
	 */
	public void setSenha(String senha) {
		this.senha = senha;
	}

	/**
	 * @return Nivel de Acesso do Usuário. 
	 */

	public int getNivelAcesso() {
		return nivelAcesso;
	}
	/**
	 * 
	 * @param nivelAcesso Nivel de Acesso do Usuário. 
	 */

	public void setNivelAcesso(int nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}

	/**
	 * 
	 * @return Cadastro de PEssoa Física do USuário. 
	 */
	public String getCpf() {
		return cpf;
	}

	/**
	 * 
	 * @param cpf É o Cadastro de Pessoa Física do Usuário. 
	 */
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	/**
	 * Constroi objeto Usuário. 
	 */
	public Usuario() {
		id = 0;
		nome = "livre";
		email = "";
		login = "livre";
		senha = "";
	}

	@Override
	public String toString() {
		// return "Usuario: id=" + id + "\n" + "nome=" + nome + "\n" + "email=" + email + "\n" + "login=" + login + "\n" + "senha=" + senha + "\n" + "nivelAcesso=" + nivelAcesso + "\n" + "cpf=" + cpf;
		return login;
	}
	/**
	 * 
	 * @return Id na base original. 
	 */

	public int getIdBaseExterna() {
		return idBaseExterna;
	}

	/**
	 * @param idBaseExterna id na base original. 
	 * 
	 */
	public void setIdBaseExterna(int idBaseExterna) {
		this.idBaseExterna = idBaseExterna;
	}

}

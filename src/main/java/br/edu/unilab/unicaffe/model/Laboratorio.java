package br.edu.unilab.unicaffe.model;

/**
 * Esta classe representa um local de acesso de computadores, provido de máquinas com UniCaffé. 
 * 
 * @author Jefferson Uchôa Ponte
 *
 */
public class Laboratorio {

	/**
	 * Id do laboratório no banco de dados. 
	 */
	private int id;
	/**
	 * Nome do laboratório. 
	 */
	private String nome;
	/**
	 * Perfil deste laboratório. 
	 */
	private Perfil perfil;
	/**
	 * @return id do laboratório
	 */
	public Laboratorio() {
		this.perfil = new Perfil();
	}
	public Perfil getPerfil() {
		return perfil;
	}
	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}
	public int getId() {
		return id;
	}
	/**
	 * @param id é o ID do laboratório no banco de dados. 
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * 
	 * @return nome do laboratório 
	 */
	public String getNome() {
		return nome;
	}
	/**
	 * @param nome nome do laboratório. 
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}


}

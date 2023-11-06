package br.edu.unilab.unicaffe.model;

public class Perfil {
	
	/**
	 * Id do perfil. 
	 */
	private int id;
	/**
	 * Nome dado ao perfil. 
	 */
	private String nome;
	/**
	 * Tempo em segundos disponibilizado para cada usuário ao fazer autenticação. 
	 */
	private int cota;
	private boolean visitante;
	/**
	 * Indica se o bonus será habilitado. 
	 */
	private boolean bonus;
	/**
	 * Quantidade mínima de máquinas livres para considerar o laboratório lotado. 
	 */
	private int lotacao;
	/**
	 * Tempo de duração de cada turno em horas. 
	 */
	private int tempoTurno;
	
	public Perfil() {
		this.nome = "Padrão";
		this.cota = 3600;
		this.visitante = true;
		this.bonus = true;
		this.lotacao = 4;
		this.tempoTurno = 6;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getCota() {
		return cota;
	}
	public void setCota(int cota) {
		this.cota = cota;
	}
	public boolean isVisitante() {
		return visitante;
	}
	public void setVisitante(boolean visitante) {
		this.visitante = visitante;
	}
	public boolean isBonus() {
		return bonus;
	}
	public void setBonus(boolean bonus) {
		this.bonus = bonus;
	}
	public int getLotacao() {
		return lotacao;
	}
	public void setLotacao(int lotacao) {
		this.lotacao = lotacao;
	}
	public int getTempoTurno() {
		return tempoTurno;
	}
	public void setTempoTurno(int tempoTurno) {
		this.tempoTurno = tempoTurno;
	}
	
	@Override
	public String toString() {
		String perfil = "Perfil = {ID: "+this.getId()+", nome: "+this.getNome()+"}";
		return perfil;
	}

}

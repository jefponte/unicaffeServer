package br.edu.unilab.unicaffe.model;

import java.util.Date;

/**
 * Representa o acesso atual da máquina, com informação de tempo utilizado, estado do acesso e usuário atual. 
 * 
 * @author Jefferson Uchôa Ponte
 *
 */
public class Acesso {
	
	/**
	 * ID do acesso no banco de dados. 
	 */
	private int id;
	/**
	 * Tempo usado no acesso atual. 
	 */
	private int tempoUsado;
	/**
	 * Hora que o acesso atual se iniciou. 
	 */
	private long horaInicial;
	/**
	 * Usuário do acesso atual. 
	 */
	private Usuario usuario;
	/**
	 * Tempo que o usuário recebeu ao iniciar seu acesso. 
	 */
	private int tempoDisponibilizado;
	/**
	 * Estado do acesso. Será preenchido com as constantes desta classe. 
	 * Poderá ser: 
	 * STATUS_EM_UTILIZACAO para informar que o acesso está em utilizaçãoi. 
	 * STATUS_DISPONIVEL para informar que o acesso está disponível para o próximo usuário. 
	 */
	private int status;
	/**
	 * ID da máquina no banco de dados. 
	 * 
	 */
	private int idMaquina;
	/**
	 * IP Do acesso atual. 
	 */
	private String ip;
	/**
	 * data e hora em long que registra
	 * ultima vez que a maquina foi setada como disponivel
	 */
	private long ultimaInteracaoSetStatusDisponivel;
	/**
	 * Esta significa que o acesso está em utilização. 
	 * Constante para ser usada no atributo status. 
	 */
	public static final int STATUS_EM_UTILIZACAO = 0;
	/**
	 * Acesso disponível. 
	 */
	public static final int STATUS_DISPONIVEL = 1;

	/**
	 * Constroi um objeto acesso. 
	 * Além de atribuir valores default. 
	 * status default STATUS_DISPONIVEL
	 * tempo usado 0
	 * 
	 */

	public Acesso() {
		pararDeContar();
		this.usuario = new Usuario();
		this.setTempoUsado(0);
	}
	/**
	 * Começa a contar o tempo de acesso. 
	 * Contagem continuará enquanto status for STATUS_EM_UTILIZACAO. 
	 */
	public void contar(){
		this.status = STATUS_EM_UTILIZACAO;
		Thread contando = new Thread(new Runnable() {
			@Override
			public void run() {
				while(status == STATUS_EM_UTILIZACAO){
					try {
						Thread.sleep(1000);
						tempoUsado++;
					} catch (InterruptedException e) {
						
					}
				}
			}
		});
		contando.start();
	}
	/**
	 * Para a contagem de acesso ao mudar status para STATUS_DISPONIVEL
	 */
	public void pararDeContar(){
		this.status = STATUS_DISPONIVEL;
		this.ultimaInteracaoSetStatusDisponivel = new Date().getTime();		
	}
	/**
	 * 
	 * @return id de Acesso
	 */
	public int getId() {
		return id;
	}
	/**
	 * 
	 * @param id de acesso
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * 
	 * @return usuário
	 */
	public Usuario getUsuario() {
		return usuario;
	}
	/**
	 * 
	 * @param usuario
	 */
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	/**
	 * 
	 * @return tempo usado
	 */
	public int getTempoUsado() {
		return tempoUsado;
	}
	/**
	 * 
	 * @param tempoUsado
	 */
	public void setTempoUsado(int tempoUsado) {
		this.tempoUsado = tempoUsado;
	}
	/**
	 * 
	 * @return hora inicial
	 */
	public long getHoraInicial() {
		return horaInicial;
	}
	/**
	 * 
	 * @param hora inicial
	 */
	public void setHoraInicial(long l) {
		this.horaInicial = l;
	}
	/**
	 * 
	 * @return tempo disponibilizado para o usuário
	 */
	public int getTempoDisponibilizado() {
		return tempoDisponibilizado;
	}
	/**
	 * 
	 * @param tempo disponibilizado
	 */
	public void setTempoDisponibilizado(int tempoDisponibilizado) {
		this.tempoDisponibilizado = tempoDisponibilizado;
	}
	/**
	 * 
	 * @return status do acesso
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * 
	 * @param status da conexão
	 */
	public void setStatus(int statusDaConexao) {
		this.status = statusDaConexao;
	}
	

	@Override
	public String toString() {
		String acesso = "usuario: "+this.usuario.getId()+", hora Acesso Long: "+this.horaInicial+", Tempo Usado: "+this.tempoUsado+", "+this.tempoDisponibilizado+"BLZ\n";
		return acesso;
	}
	/**
	 * 
	 * @return id da máquina
	 */
	public int getIdMaquina() {
		return idMaquina;
	}
	/**
	 * 
	 * @param id da máquina
	 */
	public void setIdMaquina(int idMaquina) {
		this.idMaquina = idMaquina;
	}
	/**
	 * 
	 * @return ip da máquina que está sendo acessada
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * 
	 * @param ip da máquina acessada
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return long
	 * data e hora em long que registra
	 * ultima vez que a maquina foi setada como disponivel
	 */
	public long getUltimaInteracaoSetStatusDisponivel() {
		return ultimaInteracaoSetStatusDisponivel;
	}
}

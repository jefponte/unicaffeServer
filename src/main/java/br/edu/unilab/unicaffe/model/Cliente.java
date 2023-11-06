package br.edu.unilab.unicaffe.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Do lado do servidor tem o socket de conexão com um cliente, do lado do
 * cliente tem um socket de conexão com o servidor. Também serve para organizar
 * informações básicas do acesso, máquina, usuário, etc.
 * 
 * @author Jefferson Uchôa Ponte
 *
 */
public class Cliente {

	/**
	 * Do lado do servidor é uma conexão com um cliente, do lado do cliente é uma
	 * conexão com o servidor.
	 */
	private Socket conexao;
	/**
	 * Máquina com dados de acesso, usuário.
	 */
	private Maquina maquina;
	/**
	 * Fluxo de envio de informações da conexão de socket. No lado do servidor envia
	 * mensagens para um cliente, no lado do cliente envia informações para o
	 * servidor.
	 */
	private OutputStream saida;
	/**
	 * Fluxo recebimento de informações da conexão de socket. Do lado do servidor
	 * recebe informações de um cliente, do lado do cliente recebe informações do
	 * servidor.
	 */

	private InputStream entrada;
	/**
	 * Data e hora da ultima interação de um cliente com o servidor.
	 */
	private long ultimaInteracao;

	/**
	 * Tempo de acesso para cada cliente.
	 * 
	 */
	public int cota;
	
	/**
	 * Constrói um objeto que agrega socket de fluxo entre servidor e cliente, além
	 * de manter informações de máquina e de acesso.
	 * 
	 */
	
	public Cliente() {
		this.cota = 3600;
		this.maquina = new Maquina();
	}

	
	public int getCota() {
		return cota;
	}

	public void setCota(int cota) {
		this.cota = cota;
	}

	/**
	 * @return socket de fluxo entre servidor e cliente.
	 */
	public Socket getConexao() {
		return conexao;
	}

	/**
	 * Atribui uma conexão ao objeto cliente.
	 * 
	 * @param conexao
	 */
	public void setConexao(Socket conexao) {

		this.conexao = conexao;
	}

	/**
	 * @return Objeto máquina com informações de acesso.
	 */
	public Maquina getMaquina() {
		return maquina;
	}

	/**
	 * Atribui uma máquina a um objeto cliente.
	 * 
	 * @param maquina
	 */
	public void setMaquina(Maquina maquina) {
		this.maquina = maquina;
	}

	/**
	 * 
	 * @return fluxo de saída.
	 */
	public OutputStream getSaida() {
		return saida;
	}

	/**
	 * Atribui fluxo de dados de saída.
	 * 
	 * @param saida
	 */
	public void setSaida(OutputStream saida) {
		this.saida = saida;
	}

	/**
	 * s
	 * 
	 * @return fluxo de dados de entrada.
	 */
	public InputStream getEntrada() {
		return entrada;
	}

	/**
	 * 
	 * @param entrada
	 */
	public void setEntrada(InputStream entrada) {
		this.entrada = entrada;
	}

	/**
	 * 
	 * @return data e hora da ultima interação do cliente com o servidor.
	 */
	public long getUltimaInteracao() {
		return ultimaInteracao;
	}

	/**
	 * Atribui data e hora da ultima interação do cliente com o servidor.
	 * 
	 * @param ultimaInteracao
	 */
	public void setUltimaInteracao(long ultimaInteracao) {
		this.ultimaInteracao = ultimaInteracao;
	}

}

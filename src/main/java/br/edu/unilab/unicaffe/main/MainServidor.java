package br.edu.unilab.unicaffe.main;

import br.edu.unilab.unicaffe.controller.ServidorController;

/**
 * 
 * @author Jefferson Uchôa Ponte
 * Classe responsável pelo inicialização do servidor
 */
public class MainServidor {
	/**
	 * Método que inicia o processo. 
	 * @param args
	 */
	public static void main(String[] args) {
		ServidorController servidor = new ServidorController();
		servidor.iniciaServico();		
	}
}

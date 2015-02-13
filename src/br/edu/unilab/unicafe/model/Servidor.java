package br.edu.unilab.unicafe.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.omg.CORBA.Environment;

import br.edu.unilab.unicafe.dao.AcessoDAO;
import br.edu.unilab.unicafe.dao.DAO;
import br.edu.unilab.unicafe.dao.MaquinaDAO;
import br.edu.unilab.unicafe.dao.UsuarioDAO;
import br.edu.unilab.unicafe.view.FrameApresentacao;
import br.edu.unilab.unicafe.view.FrameServidor;

/**
 * 
 * @author Jefferson
 *
 */

public class Servidor {

	private Maquina maquina;
	private String ip;
	private ServerSocket serverSocket;
	private ArrayList<Cliente> listaDeClientes;
	public FrameApresentacao frameApresentacao;
	private FrameServidor frameServidor;

	public Servidor() {
		this.maquina = new Maquina();
		this.ip = "";
		this.listaDeClientes = new ArrayList<Cliente>();

	}

	public void printd(final String mensagem) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				frameServidor.getDisplay().append("\n" + mensagem);

			}
		});

	}

	public void iniciaSplash() {
		Thread iniciando = new Thread(new Runnable() {

			@Override
			public void run() {
				frameApresentacao = new FrameApresentacao();
				frameApresentacao.setLocationRelativeTo(null);
				frameApresentacao.setVisible(true);
				try {
					Thread.sleep(3000);
					frameApresentacao.setVisible(false);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				iniciaServico();
			}
		});
		iniciando.start();
	}

	public void iniciaServico() {

		
		frameServidor = new FrameServidor();
		
		frameServidor.getItemRenovaTempo().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				processaMensagem(null, "renovaGalera()");
			}
		});
		
		this.maquina.preencheComMaquinaLocal();
		this.ip = this.maquina.getIp();
		frameServidor.setVisible(true);
		printd("Iniciando servidor...");
		try {
			this.serverSocket = new ServerSocket(12345, 100);
			printd("Servidor iniciado. ");
			printd("Dados do Servidor: Ip-> " + this.ip + " - MAC-> "
					+ this.maquina.getEnderecoMac());
			esperaConexoes();

		} catch (IOException e) {
			printd("Erro de IO.");
			// e.printStackTrace();
		}

	}

	public void esperaConexoes() {
		Thread recebendoConexao = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						printd("Aguardando conexoes...");
						Socket conexao = serverSocket.accept();
						printd("Nova conex�o! "
								+ conexao.getInetAddress().toString());
						processaConexao(conexao);

					}
				} catch (IOException e) {
					System.out.println("???");
					e.printStackTrace();
				}
			}
		});
		recebendoConexao.start();

	}

	public synchronized void processaMensagem(Cliente cliente, String mensagem){
		String comando = mensagem.substring(0,
				mensagem.indexOf('('));
		String parametros = mensagem.substring(
				mensagem.indexOf('(') + 1,
				mensagem.indexOf(')'));

		/*
		printd(cliente.getMaquina().getNome() + ">> "
				+ mensagem);
		
		*/

		if (comando.equals("autentica")) {
			String login = parametros.substring(0,
					parametros.indexOf(','));
			String senha = parametros.substring(parametros
					.indexOf(',') + 1);
			printd(cliente.getMaquina().getNome()
					+ ">> Tentativa de Autentica��o.");
			printd(cliente.getMaquina().getNome()
					+ ">> Login : " + login);
			printd(cliente.getMaquina().getNome()
					+ ">> Senha : " + senha);
			UsuarioDAO dao = new UsuarioDAO();
			Usuario usuario = new Usuario();
			usuario.setLogin(login);
			usuario.setSenha(senha);

			if (dao.autentica(usuario)) {
				printd(cliente.getMaquina().getNome()
						+ ">> Autentica��o bem sucedida.");
				try {
					cliente.getSaida().flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				printd("Verificar tempo acessado. ");
				AcessoDAO acessoDao = new AcessoDAO(dao.getConexao());
				int tempo = acessoDao.retornaTempoUsado(usuario);
				
				printd("Usou: "+tempo);
				if(tempo <= AcessoDAO.COTA){
					printd("Pode acessar durante "+((AcessoDAO.COTA)-(tempo))+" segundos");
					try {
						cliente.getSaida().writeObject("desbloqueia(" + login + ", "+((AcessoDAO.COTA)-(tempo))+")");
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					cliente.setAcesso(new Acesso());
					Acesso acesso = cliente.getAcesso();
					acesso.setUsuario(usuario);
					acesso.setTempoDisponibilizado(((AcessoDAO.COTA)-(tempo)));
					acesso.setTempoUsado(0);
					acesso.setMaquina(cliente.getMaquina());
					acesso.contar();
					
					acesso.setHoraInicial(System.currentTimeMillis());
					
					
				}else{
					try {
						cliente.getSaida()
						.writeObject(
								"printc(Que peninha, seu tempo j� acabou)");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			} else {
				printd(cliente.getMaquina().getNome()
						+ ">> Errou login ou senha.");
				try {
					cliente.getSaida().flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					cliente.getSaida()
							.writeObject(
									"printc(Beleza, Fera! Mas e a senha correta, vc sabe?)");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				dao.getConexao().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(comando.equals("renovaGalera")){
			AcessoDAO dao = new AcessoDAO();
			dao.renovaGalera();
		
		}
		else if (comando.equals("setNome")) {

			String nome = parametros;
			printd(cliente.getMaquina().getNome()
					+ ">> Tentou mudar o nome para : " + nome);
			cliente.getMaquina().setNome(nome);
			//Precisamos verificar se j� existe uma m�quina no banco com esse nome. 
			//Caso n�o exista iremos adicionar. 
			MaquinaDAO maquinaDao = new MaquinaDAO();
			//Existe diferente de verdadeiro== existe igual a false. 
			if(!maquinaDao.existe(cliente.getMaquina())){
				if(maquinaDao.cadastra(cliente.getMaquina())){
					printd("Maquina nova cadastrada: "+cliente.getMaquina().getNome());
					printd("Listarei as m�quinas: ");
					for(Maquina maquina : maquinaDao.retornaLista()){
						printd("ID: "+maquina.getId()+" Nome: "+maquina.getNome());
					}
				}
				
			}
			try {
				maquinaDao.getConexao().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (comando.equals("setMac")) {

			cliente.getMaquina().setEnderecoMac(parametros);
			printd(cliente.getMaquina().getNome()
					+ ">> Mudou endere�o MAC para: "
					+ parametros);

		} else if (comando.equals("setStatus")) {

			int status = Integer.parseInt(parametros);
			cliente.getMaquina().setStatus(status);
			System.out.println("Mudou Status. ");
			if((status == Maquina.STATUS_DISPONIVEL) && (cliente.getAcesso() != null)){
				cliente.getAcesso().pararDeContar();
				AcessoDAO acessodao = new AcessoDAO();
				acessodao.cadastra(cliente.getAcesso());
				cliente.setAcesso(null);
				System.out.println("� pra ter cadastrado um novo acesso. ");
				try {
					acessodao.getConexao().close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			printd(cliente.getMaquina().getNome()
					+ ">> Mudou o Status para "
					+ Maquina.statusString(status));
		

		} else {

			printd(cliente.getMaquina().getNome() + ">>"
					+ " Comando n�o encontrado.");
		}

	}
	/**
	 * 
	 * @param conexao
	 */
	public void processaConexao(final Socket conexao) {
		Thread processando = new Thread(new Runnable() {

			@Override
			public void run() {
				Cliente cliente = new Cliente();
				listaDeClientes.add(cliente);
				try {
					cliente.setEntrada(new ObjectInputStream(conexao
							.getInputStream()));
					final OutputStream outputStream = conexao.getOutputStream();
					cliente.setSaida(new ObjectOutputStream(outputStream));
					
					
				} catch (IOException e) {
					printd("Errinho de IO.");
					// e.printStackTrace();
				}

				cliente.getMaquina().setNome("NAO LISTADO");

				while (!conexao.isClosed()) {


					try {
						String mensagem = (String) cliente.getEntrada()
								.readObject();
						
						
						processaMensagem(cliente, mensagem);
						
					} catch (ClassNotFoundException e) {

						// e.printStackTrace();
						break;
					} catch (IOException e) {

						// e.printStackTrace();
						break;
					}
				}
				if(cliente.getAcesso() != null){
					cliente.getAcesso().pararDeContar();
					AcessoDAO acessodao = new AcessoDAO();
					acessodao.cadastra(cliente.getAcesso());
					try {
						acessodao.getConexao().close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				printd(cliente.getMaquina().getNome() + ">> Conex�o fechada. ");

			}
		});

		processando.start();

		// Iremos ouvir String. Processar essa string.
		// Ouvir direto at� a conex�o acabar.
		// A fala vai ter que ser impulsionada por algum evento.
		// Logo n�o precismos nos procupar com ela agora.

	}

	/**
	 * M�todo criado durante o desenvolvimento para facilitar atualiza��o do
	 * software. Com este m�todo podemos atualizar o sistema em todos os
	 * clientes. Uma nova op��o foi adicionada na tela do servidor, um menu para
	 * mandar a atualiza��o. Esse menu vai chamar este m�todo.
	 */
	public void atualizaGalera(OutputStream socketOut) {
		for (Cliente cliente : listaDeClientes) {
			printd("Atualizar Cliente " + cliente.getMaquina().getNome());
			try {
				cliente.getSaida()
						.writeObject("printc(Sistema em atualizacao)");
				cliente.getSaida().writeObject("atualizaAgora()");

				// Criando tamanho de leitura
				byte[] cbuffer = new byte[1024];
				int bytesRead;

				// Criando arquivo que sera transferido pelo servidor
				File file = new File("update_pasta_server\\cliente.jar");
				 FileInputStream fileIn = new FileInputStream(file);
				System.out.println("Lendo arquivo...");

				// Criando canal de transferencia
				//OutputStream socketOut = cliente.getConexao().getOutputStream();

				System.out.println("Enviando Arquivo...");
				while ((bytesRead = fileIn.read(cbuffer)) != -1) {
					socketOut.write(cbuffer, 0, bytesRead);
					socketOut.flush();
				}

				System.out.println("Arquivo Enviado!");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public Maquina getMaquina() {
		return maquina;
	}

	public void setMaquina(Maquina maquina) {
		this.maquina = maquina;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public ArrayList<Cliente> getListaDeClientes() {
		return listaDeClientes;
	}

	public void setListaDeClientes(ArrayList<Cliente> listaDeClientes) {
		this.listaDeClientes = listaDeClientes;
	}

	public FrameServidor getFrameServidor() {
		return frameServidor;
	}

	public void setFrameServidor(FrameServidor frameServidor) {
		this.frameServidor = frameServidor;
	}

}

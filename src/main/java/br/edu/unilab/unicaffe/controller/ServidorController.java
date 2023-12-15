package br.edu.unilab.unicaffe.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import br.edu.unilab.unicaffe.dao.AcessoDAO;
import br.edu.unilab.unicaffe.dao.MaquinaDAO;
import br.edu.unilab.unicaffe.dao.PerfilDAO;
import br.edu.unilab.unicaffe.dao.UsuarioDAO;
import br.edu.unilab.unicaffe.model.Acesso;
import br.edu.unilab.unicaffe.model.Cliente;
import br.edu.unilab.unicaffe.model.Laboratorio;
import br.edu.unilab.unicaffe.model.Maquina;
import br.edu.unilab.unicaffe.model.Usuario;

/**
 *
 * @author Jefferson
 *
 *         Nesta classe funciona o núcleo do Servidor. Processa as conexões com
 *         os clientes. Permite conexões cod o UniCaffé Web.
 *
 */

public class ServidorController {

	/**
	 * Provê conexões com os clientes do UniCaffé.
	 */
	private ServerSocket serverSocket;
	/**
	 * Lista com os clientes conectados.
	 */
	private Collection<Cliente> listaDeClientes;
	/**
	 * Lista de Bloqueados
	 */
	private String bloqueados;

	/**
	 *
	 * @return lista de programas bloqueados
	 */
	public String getBloqueados() {
		return bloqueados;
	}

	/**
	 *
	 * @param bloqueados
	 */
	public void setBloqueados(String bloqueados) {
		this.bloqueados = bloqueados;
	}

	/**
	 * Construtor
	 */
	public ServidorController() {
		this.listaDeClientes = new Vector<Cliente>(200);
	}

	/**
	 * Retorna uma lista com todos os clientes conectados.
	 *
	 * @return
	 */
	public Collection<Cliente> getListaDeClientes() {
		return listaDeClientes;
	}

	/**
	 * Inicializa o servidor do UniCaffé.
	 */
	public void iniciaServico() {
		limparInativos(15);
		int porta = 27289;
		try {
			Properties config = new Properties();
			FileInputStream file = new FileInputStream(ARQUIVO_CONFIGURACAO);
			config.load(file);
			porta = Integer.parseInt(config.getProperty("unicaffe_porta"));
			file.close();
			System.out.println("Servico iniciado na porta " + porta);
			this.serverSocket = new ServerSocket(porta, 500);
			esperaConexoes();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * verificar os clientes que interagiram a menos de 30 minutos enviar toVivo
	 * enviar caça fantasma
	 *
	 * @param cliente
	 * @param tempoEsperaMinutos
	 * @param tempoDesligaPorInatividadeMinutos
	 */
	public void limparInativos(final long tempoEsperaMinutos) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					long umMinuto = 60000;
					long tempoEspera = umMinuto * tempoEsperaMinutos;
					Iterator<Cliente> it = listaDeClientes.iterator();
					while (it.hasNext()) {
						Cliente daVez = it.next();
						long tempoOcio =  new Date().getTime() - daVez.getUltimaInteracao();
						if (tempoOcio > tempoEspera) cacaFantasmasSemThread(daVez);
					}
					try {
						Thread.sleep(tempoEspera / 2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}


	/**
	 * Inicia uma Thread que aguarda as conexões com os clientes, chamando outro
	 * método que processará conexão desses clientes.
	 */
	public void esperaConexoes() {
		Thread recebendoConexao = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Servidor iniciado!");
				System.out.println("Aguardando conexões...");
				while (true) {
					Socket conexao;
					try {
						conexao = serverSocket.accept();
						processaConexao(conexao);
					} catch (IOException e) {
						System.out.println("Tentativa de conexão frustrada! ");
					} catch (Exception e) {
						System.out.println("Outra Excessão! ");
					}
				}
			}
		});
		recebendoConexao.start();

	}

	private void corrigirClientesDuplicados(Cliente cliente) {
		System.out.println("entrando na tread do limpar Inativos");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Iterator<Cliente> it = listaDeClientes.iterator();
		int qtd = 0;
		Cliente primeiro = null;

		while (it.hasNext()) {
			Cliente daVez = it.next();
			boolean nomeIgual = daVez.getMaquina().getNome().equals(cliente.getMaquina().getNome());

			if (nomeIgual) {
				++qtd;
				if (qtd == 1) {
					primeiro = daVez;
					continue;
				}

				if (qtd > 1) {
					if (qtd == 2) {
						cacaFantasmas(primeiro);
					}
					cacaFantasmas(daVez);
				}
			}
		}
		if (qtd > 1) {
			System.out.println("encontrou " + qtd + " " + cliente.getMaquina().getNome() + " "
					+ cliente.getMaquina().getLaboratorio().getNome());
		}
	}


	/**
	 * Inicia uma Thread, que vai aguardar uma mensagem do cliente e responde. A
	 * mensagem poderá conter parâmetros e para isso deverá conter parênteses.
	 *
	 * @param conexao
	 */
	public void processaConexao(final Socket conexao) {

		Thread processando = new Thread(new Runnable() {

			@Override
			public void run() {
				Cliente cliente = new Cliente();
				cliente.setConexao(conexao);

				PrintStream ps = null;
				BufferedReader in = null;
				String mensagem = null;

				try {
					cliente.setSaida(conexao.getOutputStream());
					cliente.getSaida().flush();
					cliente.setEntrada(conexao.getInputStream());
					cliente.getMaquina().setIp(cliente.getConexao().getInetAddress().toString().substring(1));
					ps = new PrintStream(cliente.getSaida());
					in = new BufferedReader(new InputStreamReader(cliente.getEntrada(), "UTF-8"));
					mensagem = in.readLine();

					if (mensagem == null) {
						ps.println("Estamos conectados!");
						conexao.close();
						return;
					}
					if (mensagem.indexOf("GET / HTTP/1.1") != -1) {
						ps.println("HTTP/1.0 200 OK\r\n" + "Server: UniCaffeServer/2.0\r\n"
								+ "Content-Type: text/html\r\n" + "\r\n" + "<!DOCTYPE html><html>" + "<header><style>"
								+ "body{background-color:#FF9999;}" + "" + "" + "</style></header>" + ""
								+ "<h1>Servidor Funcionando!</h1>" + "</html>");
						cliente.getConexao().close();

					} else if (mensagem.indexOf('(') != -1 && mensagem.indexOf(')') != -1) {
						String comando = mensagem.substring(0, mensagem.indexOf('('));
						String parametros = mensagem.substring(mensagem.indexOf('(') + 1, mensagem.lastIndexOf(')'));
						if (comando.equals("setStatus") && Integer.parseInt(parametros) == Maquina.STATUS_DISPONIVEL) {
							cliente.getMaquina().setStatus(Integer.parseInt(parametros));

							listaDeClientes.add(cliente);
							corrigirClientesDuplicados(cliente);

							while (!conexao.isClosed()) {
								mensagem = in.readLine();
								if (mensagem == null) {
									System.out.println("mensagem nula fechando conexão" );
									conexao.close();
									break;
								}


								processaMensagem(cliente, mensagem);
							}
							System.out.println("Cliente removido " + mensagem);
							listaDeClientes.remove(cliente);
							return;

						} else if (comando.equals("setStatus")
								&& Integer.parseInt(parametros) == Maquina.STATUS_ADMIN) {
							mensagem = in.readLine();
							processaMensagemAdmin(cliente, mensagem);
							cliente.getConexao().close();
							return;
						} else if (comando.equals("setStatus")
								&& Integer.parseInt(parametros) == Maquina.STATUS_UPDATE) {

							System.out.println("Vamos Atualizar esse cara.");
							cliente.getMaquina().setStatus(Integer.parseInt(parametros));
							try {
								File f = new File("/dados/unicaffe/UniCafeClient.exe");
								@SuppressWarnings("resource")
								FileInputStream in1 = new FileInputStream(f);
								OutputStream out = cliente.getConexao().getOutputStream();
								OutputStreamWriter osw = new OutputStreamWriter(out);
								BufferedWriter writer = new BufferedWriter(osw);
								writer.flush();

								System.out.println("o tamanho em bytes da atualização é " + f.length());

								int tamanho = 4096;
								byte[] buffer = new byte[tamanho];
								int lidos = -1;
								while ((lidos = in1.read(buffer, 0, tamanho)) != -1) {
									out.write(buffer, 0, lidos);
									System.out.println("buffo: " + buffer + " tamanho" + tamanho + " lidos:" + lidos);
								}
								System.out.println("Aweeee");
								cliente.getConexao().close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						} else if (comando.equals("setStatus")
								&& Integer.parseInt(parametros) == Maquina.STATUS_UPDATE_ATUALIZADOR) {

							System.out.println("Vamos Atualizar esse cara.");
							cliente.getMaquina().setStatus(Integer.parseInt(parametros));
							try {
								File f = new File("/dados/unicaffe/unicafe-update.jar");
								@SuppressWarnings("resource")
								FileInputStream in1 = new FileInputStream(f);
								OutputStream out = cliente.getConexao().getOutputStream();
								OutputStreamWriter osw = new OutputStreamWriter(out);
								BufferedWriter writer = new BufferedWriter(osw);
								writer.flush();
								int tamanho = 4096;
								byte[] buffer = new byte[tamanho];
								int lidos = -1;
								while ((lidos = in1.read(buffer, 0, tamanho)) != -1) {
									out.write(buffer, 0, lidos);
									System.out.println("buffo: " + buffer + " tamanho" + tamanho + " lidos:" + lidos);
								}
								System.out.println("Aweeee");
								cliente.getConexao().close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						} else {
							System.out.println("Comando recusado.");
							System.out.println("Uma conexao recusada. ");
							ps.println("Eu sou o Servidor, meu chapa!");
							conexao.close();
							return;
						}

					}
				} catch (IOException e) {
					if (cliente.getMaquina().getAcesso().getStatus() == Acesso.STATUS_EM_UTILIZACAO) {
						cliente.getMaquina().getAcesso().pararDeContar();
						AcessoDAO acessodao = new AcessoDAO();
						if (cliente.getMaquina().getLaboratorio().getId() != 0)
							acessodao.cadastra(cliente.getMaquina());

						try {
							acessodao.getConexao().close();
						} catch (SQLException e1) {
							System.out.println("Erro de SQLEXception.");
						}
					}
					listaDeClientes.remove(cliente);
				}

				if (cliente.getMaquina().getAcesso().getStatus() == Acesso.STATUS_EM_UTILIZACAO) {
					cliente.getMaquina().getAcesso().pararDeContar();
					AcessoDAO acessodao = new AcessoDAO();
					if (cliente.getMaquina().getLaboratorio().getId() != 0)
						acessodao.cadastra(cliente.getMaquina());

					try {

						acessodao.getConexao().close();

					} catch (SQLException e) {
						System.out.println("Erro de SQLEXception.");
					}
				}

				long tempoConexao = new Date().getTime() - cliente.getUltimaInteracao();

				System.out.println(
						"processamento do " + cliente.getMaquina().getNome() + "  durou " + tempoConexao / 60000
								+ " minutos " + " msg " + mensagem + "conectado?" + cliente.getConexao().isConnected());
			}
		});

		processando.start();
	}

	/**
	 * Retorna uma mensagem de ajuda.
	 *
	 * @return mensagem de ajudar.
	 */
	public String getAjuda() {
		String ajuda = "getMaquinas() - Mostra lista de máquinas em JSON\n";
		ajuda += "desligaTudo() - Desliga todas as máquinas conectadas ao servidor\n";
		ajuda += "exec(strNomeMaquina, strComando) - Executa um comando no CMD\n";
		return ajuda;
	}

	/**
	 * Verifica se o usuário está ou não logado. Caso esteja logado em alguma
	 * máquina vai bloquear o acesso dele na mesma.
	 *
	 * @param usuario
	 * @return se está logado ou não.
	 */
	public boolean jaEstaLogado(Usuario usuario) {

		Iterator<Cliente> it = listaDeClientes.iterator();
		while (it.hasNext()) {
			Cliente daVez = it.next();
			if (daVez.getMaquina().getAcesso().getStatus() == Acesso.STATUS_EM_UTILIZACAO
					&& daVez.getMaquina().getAcesso().getUsuario().getId() == usuario.getId()) {

				daVez.getMaquina().getAcesso().pararDeContar();
				AcessoDAO dao = new AcessoDAO();
				dao.cadastra(daVez.getMaquina());
				new PrintStream(daVez.getSaida()).println("bloqueia()");
				return false;
			}
		}
		return false;
	}

	/**
	 * Processa uma mensagem vinda do UniCaffé Web. Esta está preparada para
	 * responder uma mensagem de administrador e enviar uma resposta.
	 *
	 * @param cliente
	 * @param mensagem
	 */
	public synchronized void processaMensagemAdmin(final Cliente cliente, String mensagem) {
		PrintStream psAdm = new PrintStream(cliente.getSaida());
		if (mensagem == null) {
			psAdm.println("Errou");
			fecharConexao(cliente);
			return;
		}
		if (!cliente.getConexao().getInetAddress().toString().equals("/127.0.0.1")
				&& !cliente.getConexao().getInetAddress().toString().equals("/10.5.6.79")) {

			psAdm.println(cliente.getConexao().getInetAddress().toString() + ">> " + "Conexao Recusada\n");
			fecharConexao(cliente);
			return;
		}

		if (mensagem.equals("ajuda")) {
			psAdm.println(getAjuda());
			fecharConexao(cliente);
			return;
		} else if (mensagem.equals("restart")) {
			psAdm.println("Vamos restartar");
			fecharConexao(cliente);
			processaProcesso("/etc/init.d/unicafe.sh restart");
			return;
		} else if (mensagem.equals("reboot")) {
			psAdm.println("Vamos rebotar");
			fecharConexao(cliente);
			processaProcesso("reboot");

			return;
		} else if (mensagem.equals("stop")) {
			psAdm.println("Parando aplicação");
			fecharConexao(cliente);
			System.exit(0);
			return;
		}
		if (mensagem.toLowerCase().substring(0, 6).equals("select")) {
			psAdm.println(getJson());
			fecharConexao(cliente);
			return;
		}

		if (mensagem.indexOf('(') == -1 || mensagem.indexOf(')') == -1) {
			psAdm.println("Mensagem não encontrada. Digite ajuda para obter ajuda. ");
			fecharConexao(cliente);
			return;
		}

		String parametros = "";
		String comando = mensagem.substring(0, mensagem.indexOf('('));
		parametros = mensagem.substring(mensagem.indexOf('(') + 1, mensagem.lastIndexOf(')'));

		if (comando.equals("cadastraMaquina")) {
			cadastraMaquina(psAdm, parametros);
			fecharConexao(cliente);
			return;
		} else if (comando.equals("setPerfil")) {

			String strIdPerfil = parametros.substring(0, parametros.indexOf(','));
			String strNomeLaboratorio = parametros.substring(parametros.indexOf(',') + 1);
			Laboratorio laboratorio = new Laboratorio();
			laboratorio.setNome(strNomeLaboratorio);
			laboratorio.getPerfil().setId(Integer.parseInt(strIdPerfil));
			PerfilDAO perfilDao = new PerfilDAO();
			perfilDao.definirPerfil(laboratorio);

			psAdm.println("Perfil definido com sucesso!");
			fecharConexao(cliente);

			Iterator<Cliente> it = listaDeClientes.iterator();
			while (it.hasNext()) {
				Cliente daVez = it.next();
				if (daVez.getMaquina().getLaboratorio().getId() == laboratorio.getId()) {
					daVez.getMaquina().getLaboratorio().setPerfil(laboratorio.getPerfil());
				}
			}

			try {
				perfilDao.getConexao().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return;
		} else if (comando.equals("mudarServidor")) {
			String nomeDaMaquina = parametros.substring(0, parametros.indexOf(','));
			String servidores = parametros.substring(parametros.indexOf(',') + 1);

			psAdm.println(" Mudando a máquina " + nomeDaMaquina + " para os servidores " + servidores);
			fecharConexao(cliente);
			enviadorComando(nomeDaMaquina, comando, servidores);
			return;
		}

		else if (comando.equals("ligador")) {

			MaquinaDAO maquinaDAO = new MaquinaDAO();
			Maquina maquinaALigar = new Maquina();
			maquinaALigar.setNome(parametros.trim());
			psAdm.println("Tentando ligar: " + maquinaALigar.getNome());
			fecharConexao(cliente);

			if (!maquinaDAO.existe(maquinaALigar, true)) {
				return;
			}

			for (Cliente sirene : listaDeClientes) {
				if (sirene.getMaquina().getLaboratorio().getNome().equals(maquinaALigar.getLaboratorio().getNome())) {
					new PrintStream(sirene.getSaida()).println("sirene(" + maquinaALigar.getEnderecoMac() + ")");
					new PrintStream(sirene.getSaida()).println("ligador(" + maquinaALigar.getEnderecoMac() + ")");
				}
			}
			return;

		} else if (comando.equals("atualizaMac")) {
			Iterator<Cliente> it = listaDeClientes.iterator();
			while (it.hasNext()) {
				Cliente daVez = it.next();
				if (parametros.trim().toLowerCase().equals(daVez.getMaquina().getNome().trim().toLowerCase())) {
					MaquinaDAO maquinaDAO = new MaquinaDAO();
					Maquina maquina = new Maquina();
					maquina.setNome(daVez.getMaquina().getNome());
					if (!maquinaDAO.existe(maquina, false)) {
						psAdm.println("Deve cadastrar antes a: " + maquina.getNome());
						psAdm.close();
						fecharConexao(cliente);
						return;
					}

					maquinaDAO.atualizaMac(daVez.getMaquina());
					psAdm.println("Atualizei o mac de: " + maquina.getNome());
					psAdm.close();
					fecharConexao(cliente);
					return;
				}
			}
		}

		else if (comando.equals("alocarMaquina")) {
			String nomeMaquina = parametros.substring(0, parametros.indexOf(','));
			String nomeLaboratorio = parametros.substring(parametros.indexOf(',') + 1);
			Laboratorio laboratorio = new Laboratorio();
			laboratorio.setNome(nomeLaboratorio.trim());

			boolean achei = false;
			Cliente caraQueSeraMexido = null;
			for (Cliente daVez : listaDeClientes) {
				if (nomeMaquina.toLowerCase().equals(daVez.getMaquina().getNome().toLowerCase())) {
					achei = true;
					caraQueSeraMexido = daVez;
					break;
				}
			}

			MaquinaDAO dao = new MaquinaDAO();
			Maquina maquina = new Maquina();
			maquina.setNome(nomeMaquina);
			maquina.setLaboratorio(laboratorio);

			if (!dao.existeEsseLaboratorio(laboratorio)) {
				new PrintStream(cliente.getSaida()).println("Não existe esse laboratorio " + laboratorio.getNome());
				fecharConexao(cliente);
				return;
			}
			if (dao.existeSemBulir(maquina)) {
				new PrintStream(cliente.getSaida()).println("Existe a maquina " + nomeMaquina);
				if (dao.atualizaOuInsereLaboratorio(maquina)) {
					psAdm.println(nomeMaquina + " adicionada a novo laboratorio");
				} else {
					psAdm.println(" Um erro na hora de atualizar ou inserir");
				}
				if (achei) {
					caraQueSeraMexido.getMaquina().setLaboratorio(laboratorio);
				}
				fecharConexao(cliente);
				return;

			} else {
				if (!achei) {
					psAdm.println("Não encontrei " + nomeMaquina);
					fecharConexao(cliente);
					return;
				}

				if (dao.cadastra(caraQueSeraMexido.getMaquina())) {
					maquina.setId(caraQueSeraMexido.getMaquina().getId());
					caraQueSeraMexido.getMaquina().setCadastrada(true);
					new PrintStream(cliente.getSaida()).println("Cadastrei " + nomeMaquina);
					if (dao.atualizaOuInsereLaboratorio(maquina)) {
						caraQueSeraMexido.getMaquina().setLaboratorio(laboratorio);
						psAdm.println(nomeMaquina + " adicionada a novo laboratorio");
					} else {
						psAdm.println("E nao fiz mais nada");
					}
				}
				fecharConexao(cliente);
				return;
			}

		} else if (comando.equals("desligaTudo")) {
			psAdm.println("Desligando todos");
			fecharConexao(cliente);
			enviadorComandoParaTodos(comando);
			return;
		} else if (comando.equals("eliminarInativos")) {
			psAdm.println("Eliminando inativos");
			fecharConexao(cliente);
			eliminarInativos(14400);
			return;

		} else if (comando.equals("atualizaTudo")) {
			psAdm.println("Atualizando Tudo....");
			fecharConexao(cliente);
			enviadorComandoParaTodos(comando);
			return;
		} else if (comando.equals("atualizaAtualizadorEmTudo")) {
			psAdm.println("Atualizando atualizador em Tudo....");
			fecharConexao(cliente);
			enviadorComandoParaTodos(comando);
			return;
		} else if (comando.equals("desliga")) {
			psAdm.println("Tentando desligar:  " + parametros);
			fecharConexao(cliente);

			enviadorComando(parametros.toLowerCase(), comando + "r", "");
			return;
		} else if (comando.equals("logoff")) {
			psAdm.println("fazendo logoff :" + parametros);
			fecharConexao(cliente);

			enviadorComando(parametros.toLowerCase(), comando, "");

			return;
		}

		else if (comando.equals("limparDados")) {
			psAdm.println(
					"Limpando o " + parametros + " o tempo de limpeza" + " depende do volume de dados na máquina");
			fecharConexao(cliente);
			enviadorComando(parametros.toLowerCase(), comando, "");
			return;
		} else if (comando.equals("aula")) {
			psAdm.println("Liberado pra aula ");
			fecharConexao(cliente);
			Iterator<Cliente> it = listaDeClientes.iterator();
			while (it.hasNext()) {
				Cliente daVez = it.next();
				if (daVez.getMaquina().getNome().toLowerCase().equals(parametros.toLowerCase())) {
					daVez.getMaquina().getAcesso().getUsuario().setNome("Aula");

					PrintStream psC = new PrintStream(daVez.getSaida());
					psC.println("bloqueia()");
					psC.println("desbloqueia(aula, " + 28800 + ")");
					System.out.println("liberando pra aula");
					cacaFantasmas(daVez);
				}
			}

			return;
		} else if (comando.equals("bloqueia")) {
			psAdm.println("Bloqueando o " + parametros);
			fecharConexao(cliente);
			enviadorComando(parametros.toLowerCase(), comando, "");
			return;
		} else if (comando.equals("desativar")) {
			psAdm.println("Desativando o " + parametros);
			fecharConexao(cliente);

			Iterator<Cliente> it = listaDeClientes.iterator();
			while (it.hasNext()) {
				Cliente daVez = it.next();
				if (daVez.getMaquina().getNome().toLowerCase().equals(parametros.toLowerCase())) {
					daVez.getMaquina().getAcesso().getUsuario().setNome("Manutenção");
					daVez.getMaquina().getAcesso().getUsuario().setLogin("Manutenção");
					new PrintStream(daVez.getSaida()).println("desativar()");
				}
			}
			return;
		} else if (comando.equals("exec")) {
			String nomeDaMaquina = parametros.substring(0, parametros.indexOf(','));
			String strCmd = parametros.substring(parametros.indexOf(',') + 1);
			psAdm.println("Enviando Exec");
			fecharConexao(cliente);

			enviadorComando(nomeDaMaquina, comando, strCmd);

			return;
		} else if (comando.equals("atualiza")) {
			psAdm.println("Aguarde....");
			fecharConexao(cliente);

			enviadorComando(parametros.toLowerCase(), comando + "r", "");

			return;
		} else if (comando.equals("atualizaatualizador")) {

			psAdm.println("Atualiza Atualizador");
			fecharConexao(cliente);
			enviadorComando(parametros.toLowerCase(), comando, "");

			return;
		} else if (comando.equals("mensagemLaboatorio")) {
			String nomeLaboratorio = parametros.substring(0, parametros.indexOf(','));
			String texto = parametros.substring(parametros.indexOf(',') + 1);


			for (Cliente daVez : listaDeClientes) {
				if (daVez.getMaquina().getLaboratorio().getNome().toLowerCase().trim()
						.equals(nomeLaboratorio.toLowerCase().trim())) {
					PrintStream ps;
					try {
						ps = new PrintStream(daVez.getSaida(), true, "UTF-8");
						ps.println("printa(" + texto + ")");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

				}
			}

			new PrintStream(cliente.getSaida())
					.println("Mensagem: " + texto + " <br>Enviada para Laboratorio: " + nomeLaboratorio);
			fecharConexao(cliente);
			return;
		} else if (comando.equals("semInternet")) {
			psAdm.println(parametros + " Sem Internet");
			fecharConexao(cliente);
			enviadorComando(parametros.toLowerCase(), comando, "");

			return;
		} else if (comando.equals("comInternet")) {
			psAdm.println(parametros + " Com Internet");
			fecharConexao(cliente);
			enviadorComando(parametros.toLowerCase(), comando, "");
			return;
		} else if (comando.equals("aviso")) {
			String nomeDaMaquina = parametros.substring(0, parametros.indexOf(','));
			String texto = parametros.substring(parametros.indexOf(',') + 1);

			psAdm.println("Mensagem: " + texto + " <br>Enviada a maquina: " + nomeDaMaquina);
			fecharConexao(cliente);
			enviadorComando(nomeDaMaquina, "printa", texto);
			return;
		}

		else if (comando.equals("verProcessosBloqueados")) {
			psAdm.println(
					processosBloqueadosJaDisponiveis() ? getBloqueados() : "Não está respondendo, tente mais tarde...");
			fecharConexao(cliente);
			return;
		}

		else if (comando.equals("meDeProcessosBloqueados")) {
			fecharConexao(cliente);
			setBloqueados("teste");
			boolean maquinaConectada = enviadorComando(parametros.toLowerCase(), comando, "");
			if (!maquinaConectada)
				setBloqueados("Máquina Desconectada");
			return;

		} else if (comando.equals("liberaProcessosBloqueados")) {
			String nomeDaMaquina = parametros.substring(0, parametros.indexOf(','));
			String texto = parametros.substring(parametros.indexOf(',') + 1);
			psAdm.println("Processos Liberados " + texto + " <br>da maquina: " + nomeDaMaquina);
			fecharConexao(cliente);
			enviadorComando(nomeDaMaquina, comando, texto);
			return;

		}

		else {
			psAdm.println("Mensagem não encontrada. Digite ajuda para obter ajuda. ");
			fecharConexao(cliente);
			return;
		}

	}

	/**
	 * @param psAdm
	 * @param parametros
	 */
	public void cadastraMaquina(PrintStream psAdm, String parametros) {
		Iterator<Cliente> it = listaDeClientes.iterator();
		while (it.hasNext()) {
			Cliente daVez = it.next();
			if (parametros.toLowerCase().equals(daVez.getMaquina().getNome().toLowerCase())) {
				MaquinaDAO dao = new MaquinaDAO();
				if (dao.cadastra(daVez.getMaquina())) {
					daVez.getMaquina().setCadastrada(true);
					psAdm.println("Cadastrei " + parametros);
				} else {
					psAdm.println("Erro na tentativa de cadastro de " + parametros);
				}
				return;
			}
		}
		psAdm.println("Não encontrei " + parametros);
	}

	/**
	 *
	 * @return String com os dados no formato Json
	 */
	public String getJson() {
		String json = "";
		int h = 0;
		Iterator<Cliente> it = listaDeClientes.iterator();
		while (it.hasNext()) {
			Cliente daVez = it.next();
			String dados = "|{\"id_maquina\":" + daVez.getMaquina().getId() + ",\"nome_pc\":\""
					+ daVez.getMaquina().getNome() + "\",\"mac\":\"" + daVez.getMaquina().getEnderecoMac()
					+ "\",\"id_acesso\":" + daVez.getMaquina().getAcesso().getId() + ",\"hora_inicial\":\""
					+ daVez.getMaquina().getAcesso().getHoraInicial() + "\",\"tempo_oferecido\":"
					+ daVez.getMaquina().getAcesso().getTempoDisponibilizado() + ",\"tempo_usado\":"
					+ daVez.getMaquina().getAcesso().getTempoUsado() + ",\"ip\":\""
					+ daVez.getConexao().getInetAddress().toString() + "\",\"id_usuario\":"
					+ daVez.getMaquina().getAcesso().getUsuario().getId() + "," + "\"id_laboratorio\":"
					+ daVez.getMaquina().getLaboratorio().getId() + "," + "\"nome_laboratorio\":\""
					+ daVez.getMaquina().getLaboratorio().getNome() + "\",\"nome\":\""
					+ daVez.getMaquina().getAcesso().getUsuario().getNome() + "\"," + "\"email\":\""
					+ daVez.getMaquina().getAcesso().getUsuario().getEmail() + "\"," + "\"login\":\""
					+ daVez.getMaquina().getAcesso().getUsuario().getLogin() + "\"," + "\"senha\":\""
					+ daVez.getMaquina().getAcesso().getUsuario().getSenha() + "\"," + "\"status_maquina\":"
					+ daVez.getMaquina().getStatus() + "," + "\"status_acesso\":"
					+ daVez.getMaquina().getAcesso().getStatus() + "," + "\"nivel_acesso\":\""
					+ daVez.getMaquina().getAcesso().getUsuario().getNivelAcesso() + "\"," + "\"cota\":\""
					+ daVez.getCota() + "\"," + "\"versao\":\"" + daVez.getMaquina().getVersao() + "\","
					+ "\"ultima_interacao\":" + daVez.getUltimaInteracao() + "}";
			if (h == 0) {
				json = dados;
				h++;
			} else {
				json = json + dados;
			}
		}
		return json;
	}

	/**
	 * Processa processos
	 */
	public void processaProcesso(String processo) {
		try {
			Runtime.getRuntime().exec(processo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * envia comando para todos os clientes conectados no momento
	 * @param comando
	 */
	private void enviadorComandoParaTodos(String comando) {
		Iterator<Cliente> it = listaDeClientes.iterator();
		while (it.hasNext()) {
			Cliente daVez = it.next();
			cacaFantasmas(daVez);
			new PrintStream(daVez.getSaida()).println(comando+"()");
		}
	}

	/**
	 * verifica se a String bloqueados já recebeu ou não a lista de aplicativos
	 * bloqueados
	 *
	 * @return boolean
	 *
	 */
	public boolean processosBloqueadosJaDisponiveis() {
		for (int i = 0; i < 50; i++) {
			if (!getBloqueados().equals("teste")) {
				return true;
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}


	/**
	 * Envia um comando para um cliente/máquina
	 *
	 * @param nomeDaMaquina
	 * @param parametro
	 * @param comando
	 */
	public boolean enviadorComando(String nomeDaMaquina, String comando, String parametro) {
		boolean encontrei = false;
		Iterator<Cliente> it = listaDeClientes.iterator();
		while (it.hasNext()) {
			Cliente daVez = it.next();

			if (daVez.getMaquina().getNome().toLowerCase().equals(nomeDaMaquina.toLowerCase())) {
				cacaFantasmas(daVez);
				encontrei = true;
				PrintStream ps;
				try {
					ps = new PrintStream(daVez.getSaida(), true, "UTF-8");
					ps.println(comando + "(" + parametro + ")");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return encontrei;
	}

	/**
	 * fecha conexão
	 *
	 * @param cliente
	 * @return se fechou ou não
	 */
	public boolean fecharConexao(Cliente cliente) {

		try {
			cliente.getEntrada().close();
			cliente.getSaida().close();
			cliente.getConexao().close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// public void printTeste() {
	// for (Cliente daVez : this.getListaDeClientes()) {
	// System.out.println("______Cliente " + daVez.getMaquina().getNome() +
	// "___________");
	// System.out.println("LAB: " + daVez.getMaquina().getLaboratorio().getNome());
	// System.out.println("PERFIL: " +
	// daVez.getMaquina().getLaboratorio().getPerfil().getNome());
	// System.out.println("Cota: " +
	// daVez.getMaquina().getLaboratorio().getPerfil().getCota());
	// System.out.println("Visitante: " +
	// daVez.getMaquina().getLaboratorio().getPerfil().isVisitante());
	// System.out.println("Bonus: " +
	// daVez.getMaquina().getLaboratorio().getPerfil().isBonus());
	// System.out.println("Lotacao: " +
	// daVez.getMaquina().getLaboratorio().getPerfil().getLotacao());
	// System.out.println("Tempo Turno: " +
	// daVez.getMaquina().getLaboratorio().getPerfil().getTempoTurno());
	//
	// }
	// }

	/**
	 * Gostbusters. Elimina um cliente da lista de clientes, caso ela não esteja
	 * conectada ao servidor.
	 *
	 * @param c
	 * @return
	 */
	public void cacaFantasmas(final Cliente cliente) {
		System.out.println("CHamou caca fantasmas");
		Thread cacando = new Thread(new Runnable() {
			@Override
			public void run() {
				new PrintStream(cliente.getSaida()).println("toVivoSim()");

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long teste = new Date().getTime() - 20000;
				if (cliente.getUltimaInteracao() < teste) {
					listaDeClientes.remove(cliente);
					System.out.println("removendo fantasma " + cliente.getMaquina().getNome());
					try {
						cliente.getConexao().close();
					} catch (IOException e) {
						System.out.println("Ja estava desconectado");
						e.printStackTrace();
					}

				}
			}
		});
		cacando.start();
	}

	/**
	 * Gostbusters. Elimina um cliente da lista de clientes, caso ela não esteja
	 * conectada ao servidor.
	 * @param cliente
	 */
	public void cacaFantasmasSemThread(Cliente cliente) {
				new PrintStream(cliente.getSaida()).println("toVivoSim()");

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long teste = new Date().getTime() - 20000;
				if (cliente.getUltimaInteracao() < teste) {
					listaDeClientes.remove(cliente);
					System.out.println("removendo fantasma " + cliente.getMaquina().getNome());
					try {
						cliente.getConexao().close();
					} catch (IOException e) {
						System.out.println("Ja estava desconectado");
						e.printStackTrace();
					}

				}

	}

	/**
	 * Elimina todos os clientes que não tiveram interação com o servidor nos
	 * ultimos segundos.
	 *
	 * @param tempo
	 */

	public void eliminarInativos(int tempo) {
		long teste = new Date().getTime() - (tempo) * 1000;
		Iterator<Cliente> it = listaDeClientes.iterator();
		while (it.hasNext()) {
			Cliente daVez = it.next();
			if (daVez.getUltimaInteracao() < teste) {
				listaDeClientes.remove(daVez);
				try {
					daVez.getConexao().close();
				} catch (IOException e) {
					System.out.println("Ja estava desconectado");
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * Faz o tratamento da mensagem de um cliente e envia uma resposta.
	 *
	 * @param cliente
	 * @param mensagem
	 */
	public synchronized void processaMensagem(final Cliente cliente, String mensagem) {
		System.out.println(cliente.getMaquina().getNome() + " >> " + mensagem);

		Date data = new Date();
		cliente.setUltimaInteracao(data.getTime());

		if (mensagem.indexOf('(') == -1 || mensagem.indexOf(')') == -1) {
			return;
		}
		String comando = mensagem.substring(0, mensagem.indexOf('('));
		String parametros = mensagem.substring(mensagem.indexOf('(') + 1, mensagem.lastIndexOf(')'));
		if (comando.equals("autentica")) {
			String login = parametros.substring(0, parametros.indexOf(','));
			String senha = parametros.substring(parametros.indexOf(',') + 1);
			Usuario usuario = new Usuario();
			usuario.setLogin(login.toLowerCase());
			usuario.setSenha(senha);
//			if (login.trim().equals("usuario")) {
//				try {
//					PrintStream ps;
//					ps = new PrintStream(cliente.getSaida(), true, "UTF-8");
//					ps.println("printc(Usuario Inválido!)");
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//				return;
//			}

			int idLaboratorio = cliente.getMaquina().getLaboratorio().getId();
			int numeroDeVisitantes = 0;
			int numeroDeMaquinasLivres = 0;
			for (Cliente oDaVez : listaDeClientes) {
				if (oDaVez.getMaquina().getAcesso().getUsuario().getLogin().equals("visitante")
						&& oDaVez.getMaquina().getLaboratorio().getId() == idLaboratorio) {
					numeroDeVisitantes++;
				}
				if (oDaVez.getMaquina().getAcesso().getStatus() == Acesso.STATUS_DISPONIVEL
						&& oDaVez.getMaquina().getLaboratorio().getId() == idLaboratorio) {
					numeroDeMaquinasLivres++;
				}
			}

			if (login.equals("visitante") && senha.equals(UsuarioDAO.getMD5("M@les2019"))) {

				if (numeroDeMaquinasLivres <= 2) {
					PrintStream ps;
					try {
						ps = new PrintStream(cliente.getSaida(), true, "UTF-8");
						ps.println("printc(Laboratório Lotado!)");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					return;
				}
				if (numeroDeVisitantes >= 5) {
					new PrintStream(cliente.getSaida()).println("printc(Muitos visitantes conectados!)");
					return;
				}
				if (!cliente.getMaquina().getLaboratorio().getPerfil().isVisitante()) {
					new PrintStream(cliente.getSaida()).println("printc(Senha de visitante desabilitada.)");
					return;
				}
				usuario.setNome("Visitante");
				cliente.getMaquina().getAcesso().setUsuario(usuario);
				cliente.getMaquina().getAcesso().setStatus(Acesso.STATUS_EM_UTILIZACAO);
				cliente.getMaquina().setStatus(Maquina.STATUS_OCUPADA);
				new PrintStream(cliente.getSaida()).println("desbloqueia(" + login + ", " + 120 + ")");
				return;
			}

			UsuarioDAO dao = new UsuarioDAO();

			if (dao.autentica(usuario)) {
				if (this.jaEstaLogado(usuario)) {
					new PrintStream(cliente.getSaida()).println("printc(já está logado!)");
					return;
				}
				AcessoDAO acessoDao = new AcessoDAO();
				int tempo = acessoDao.retornaTempoUsadoHoje(usuario,
						cliente.getMaquina().getLaboratorio().getPerfil().getTempoTurno());

				if (tempo <= cliente.getMaquina().getLaboratorio().getPerfil().getCota()) {
					new PrintStream(cliente.getSaida()).println("desbloqueia(" + login + ", "
							+ (cliente.getMaquina().getLaboratorio().getPerfil().getCota() - tempo) + ")");
					cliente.getMaquina().getAcesso().setUsuario(usuario);
					cliente.getMaquina().getAcesso().setTempoDisponibilizado(
							cliente.getMaquina().getLaboratorio().getPerfil().getCota() - tempo);
					cliente.getMaquina().getAcesso().setTempoUsado(0);
					cliente.getMaquina().setIp(cliente.getConexao().getInetAddress().toString().substring(1));
					cliente.getMaquina().getAcesso().contar();
					cliente.getMaquina().getAcesso().setHoraInicial(System.currentTimeMillis());
				} else if (numeroDeMaquinasLivres > cliente.getMaquina().getLaboratorio().getPerfil().getLotacao()
						&& !cliente.getMaquina().getLaboratorio().getPerfil().isBonus()) {
					new PrintStream(cliente.getSaida()).println("desbloqueia(" + login + ", 120)");
					cliente.getMaquina().getAcesso().setUsuario(usuario);
					cliente.getMaquina().getAcesso().setTempoDisponibilizado(120);
					cliente.getMaquina().getAcesso().setTempoUsado(0);
					cliente.getMaquina().setIp(cliente.getConexao().getInetAddress().toString().substring(1));
					cliente.getMaquina().getAcesso().contar();
					cliente.getMaquina().getAcesso().setHoraInicial(System.currentTimeMillis());
				}

				else {
					try {
						cliente.getSaida().flush();
						new PrintStream(cliente.getSaida()).println("printc(Seu tempo acabou)");
						return;
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
				try {
					dao.getConexao().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;

			} else {
				try {
					cliente.getSaida().flush();
					new PrintStream(cliente.getSaida()).println("printc(Errou o login ou a senha)");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				dao.getConexao().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		} else if (comando.equals("meDaBonus")) {
			if (!cliente.getMaquina().getLaboratorio().getPerfil().isBonus()) {
				new PrintStream(cliente.getSaida()).println("naoTemDireitoAbonus()");
				return;
			}
			int disponiveis = 0;
			int idLaboratorio = cliente.getMaquina().getLaboratorio().getId();

			for (Cliente umCliente : listaDeClientes) {

				if (umCliente.getMaquina().getStatus() == Maquina.STATUS_DISPONIVEL
						&& umCliente.getMaquina().getLaboratorio().getId() == idLaboratorio) {
					disponiveis++;
				}
			}
			if (disponiveis >= cliente.getMaquina().getLaboratorio().getPerfil().getLotacao()) {
				new PrintStream(cliente.getSaida()).println("bonus()");
				return;
			}
		} else if (comando.equals("setNome")) {
			String nome = parametros;

			cliente.getMaquina().setNome(nome);

			MaquinaDAO maquinaDao = new MaquinaDAO();
			if (maquinaDao.existe(cliente.getMaquina(), false)) {
				cliente.getMaquina().setCadastrada(true);
				PerfilDAO perfilDao = new PerfilDAO(maquinaDao.getConexao());
				perfilDao.carregaPerfil(cliente.getMaquina().getLaboratorio());
			}

			try {
				maquinaDao.getConexao().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else if (comando.equals("setMac")) {
			cliente.getMaquina().setEnderecoMac(parametros);
			return;
		} else if (comando.equals("taVivo")) {
			new PrintStream(cliente.getSaida()).println("toVivoSim()");
			return;
		}

		else if (comando.equals("setVersao")) {
			cliente.getMaquina().setVersao(parametros);
		}

		else if (comando.equals("tomaBloqueados")) {
			setBloqueados(parametros);
		}

		else if (comando.equals("setStatus")) {
			int status = Integer.parseInt(parametros);
			cliente.getMaquina().setStatus(status);
			switch (status) {
			case Maquina.STATUS_DISPONIVEL:
				if (cliente.getMaquina().getAcesso().getStatus() == Acesso.STATUS_EM_UTILIZACAO) {
					cliente.getMaquina().getAcesso().pararDeContar();
					cliente.getMaquina().setStatus(status);
					cliente.getMaquina().getAcesso().setStatus(Acesso.STATUS_DISPONIVEL);
					AcessoDAO acessodao = new AcessoDAO();
					if (cliente.getMaquina().getAcesso().getUsuario().getId() != 0)
						acessodao.cadastra(cliente.getMaquina());

					try {
						acessodao.getConexao().close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				break;
			case Maquina.STATUS_OCUPADA:
				cliente.getMaquina().setStatus(status);
				cliente.getMaquina().getAcesso().setStatus(Acesso.STATUS_EM_UTILIZACAO);
				System.out.println("Maquina enviou status ocupada.");
				break;
			case Maquina.STATUS_DESCONECTADA:
				listaDeClientes.remove(cliente);
				break;
			case Maquina.STATUS_UPDATE:
				cliente.getMaquina().setStatus(status);

				try {

					File f = new File("UniCafeClient.exe");
					@SuppressWarnings("resource")
					FileInputStream in = new FileInputStream(f);
					OutputStream out = cliente.getConexao().getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(out);
					BufferedWriter writer = new BufferedWriter(osw);
					writer.flush();
					int tamanho = 4096;
					byte[] buffer = new byte[tamanho];
					int lidos = -1;
					while ((lidos = in.read(buffer, 0, tamanho)) != -1) {
						out.write(buffer, 0, lidos);
					}
					cliente.getConexao().close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				break;
			case Maquina.STATUS_UPDATE_ATUALIZADOR:

				cliente.getMaquina().setStatus(status);

				try {

					File f = new File("unicafe-update.jar");
					@SuppressWarnings("resource")
					FileInputStream in = new FileInputStream(f);
					OutputStream out = cliente.getConexao().getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(out);
					BufferedWriter writer = new BufferedWriter(osw);
					writer.flush();
					int tamanho = 4096;
					byte[] buffer = new byte[tamanho];
					int lidos = -1;
					while ((lidos = in.read(buffer, 0, tamanho)) != -1) {
						out.write(buffer, 0, lidos);
					}
					cliente.getConexao().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			default:
				cliente.getMaquina().setStatus(status);
				break;
			}
		}
	}

	/**
	 * Local do arquivo de configuração de banco de dados.
	 */

	public static final String ARQUIVO_CONFIGURACAO = "/dados/unicaffe/unicaffe_bd.ini";

}

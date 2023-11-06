package br.edu.unilab.unicaffe.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Faz conexão com banco de dados e gerencia persistências. 
 * @author Jefferson Uchôa Ponte
 *
 */
public class DAO {

	/**
	 * Tipo de conexão. 
	 */
	private int tipoDeConexao;
	/**
	 * Sistema gerenciador de banco de dados. 
	 */
	private String sgdb;
	/**
	 * Tabela ou View de usuarios da base remota. 
	 */
	private String entidade;
	/**
	 * Conexão com banco. 
	 */
	private Connection conexao;

	/**
	 * Constroi objeto DAO com conexão com banco de dados. 
	 * @param tipoDeConexao
	 */
	public DAO(int tipoDeConexao) {
		this.tipoDeConexao = tipoDeConexao;
		fazerConexao();
	}

	/**
	 * Constroi objeto DAO com conexão com banco de dados.
	 */
	public DAO() {
		this.tipoDeConexao = TIPO_DEFAULT;
		fazerConexao();
	}

	/**
	 * Constroi objeto DAO com conexão com banco de dados.
	 */
	public DAO(Connection conexao) {
		this.conexao = conexao;
	}


	/**
	 * Faz uma conexão com banco de dados de acordo com as informações do arquivo de configuração. 
	 */
	public void fazerConexao() {
		this.conexao = null;
		try {
			Properties config = new Properties();
			FileInputStream file;
			file = new FileInputStream(ARQUIVO_CONFIGURACAO);
			config.load(file);
			String sgdb, host, porta, bdNome, usuario, senha;
			switch (tipoDeConexao) {
			case TIPO_USUARIOS:
				sgdb = config.getProperty("usuarios_sgdb");
				host = config.getProperty("usuarios_host");
				porta = config.getProperty("usuarios_porta");
				bdNome = config.getProperty("usuarios_bd_nome");
				usuario = config.getProperty("usuarios_usuario");
				senha = config.getProperty("usuarios_senha");
				this.entidade = config.getProperty("usuarios_entidade_nome");
				break;
			case TIPO_AUTENTICACAO:
				sgdb = config.getProperty("autenticacao_sgdb");
				host = config.getProperty("autenticacao_host");
				porta = config.getProperty("autenticacao_porta");
				bdNome = config.getProperty("autenticacao_bd_nome");
				usuario = config.getProperty("autenticacao_usuario");
				senha = config.getProperty("autenticacao_senha");
				senha = senha.replace("\"", "");
				this.entidade = config.getProperty("autenticacao_entidade_nome");
				break;
			default:
				sgdb = config.getProperty("default_sgdb");
				host = config.getProperty("default_host");
				porta = config.getProperty("default_porta");
				bdNome = config.getProperty("default_bd_nome");
				usuario = config.getProperty("default_usuario");
				senha = config.getProperty("default_senha");
				break;
			}
			file.close();
			if (sgdb.equals("postgres")) {
				Class.forName(DRIVER_POSTGRES);
				this.conexao = DriverManager.getConnection(JDBC_BANCO_POSTGRES+ "//" + host + "/" + bdNome, usuario, senha);
				
			} else if (sgdb.equals("sqlite")) {
				Class.forName(DRIVER_SQLITE);
				this.conexao = DriverManager.getConnection(JDBC_BANCO_SQLITE+bdNome);
			} else if (sgdb.equals("mysql")) {
				Class.forName(DRIVER_MYSQL);
				this.conexao = DriverManager.getConnection(JDBC_BANCO_MYSQL + "//" + host +":"+ porta + "/" + bdNome, usuario, senha);
			}

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retorna a conexão com banco de dados. 
	 * @return
	 */
	public Connection getConexao() {
		return conexao;
	}
	/**
	 * Atribui a conexão com banco de dados. 
	 * @param conexao
	 */
	public void setConexao(Connection conexao) {
		this.conexao = conexao;
	}

	/**
	 * Define tipod e conexão para um DAO. 
	 * @param tipoDeConexao
	 */
	public void setTipoDeConexao(int tipoDeConexao) {
		this.tipoDeConexao = tipoDeConexao;
	}

	/**
	 * @return entidade
	 */
	public String getEntidade() {
		return entidade;
	}

	/**
	 * @param entidade 
	 */
	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}

	/**
	 * @return the sgdb
	 */
	public String getSgdb() {
		return sgdb;
	}

	/**
	 * @param sgdb
	 */
	public void setSgdb(String sgdb) {
		this.sgdb = sgdb;
	}

	/**
	 * Arquivo de configuração do banco de dados. 
	 */
	public static final String ARQUIVO_CONFIGURACAO = "/dados/unicaffe/unicaffe_bd.ini";

	/**
	 * Tipo padrãod e banco de dados. 
	 */
	public static final int TIPO_DEFAULT = 0;
	/**
	 * Tipo de banco de dados de informações de usuários. 
	 */
	public static final int TIPO_USUARIOS = 1;
	/**
	 * Tipo de banco de dados para autenticação. 
	 */
	public static final int TIPO_AUTENTICACAO = 2;

	/**
	 * Drive jdbc para Sqlite. 
	 */
	public static final String DRIVER_SQLITE = "org.sqlite.JDBC";
	/**
	 * Banco de dados squlite
	 */
	
	public static final String JDBC_BANCO_SQLITE = "jdbc:sqlite:";

	/**
	 * JDBC para postgres. 
	 */
	public static final String JDBC_BANCO_POSTGRES = "jdbc:postgresql:";
	/**
	 * Driver JDBC postgres
	 */
	public static final String DRIVER_POSTGRES = "org.postgresql.Driver";
	/**
	 * JDBC Mysql
	 */
	public static final String JDBC_BANCO_MYSQL = "jdbc:mysql:";
	/**
	 * Driver JDBC Mysql
	 */
	public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";

}

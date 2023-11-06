package br.edu.unilab.unicaffe.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.edu.unilab.unicaffe.model.Laboratorio;
import br.edu.unilab.unicaffe.model.Perfil;

public class PerfilDAO extends DAO{
	/**
	 * Constroi objeto PerfilDAO
	 */
	public PerfilDAO(){
		super();
	}

	/**
	 * Constroi objeto PerfilDAO com tipo de conexão.
	 */
	public PerfilDAO(int tipoDeConexao){
		super(tipoDeConexao);
		
	}

	/**
	 * Constroi objeto PerfilDAO
	 */
	public PerfilDAO(Connection conexao){
		super(conexao);
	}

	public boolean definirPerfil(Laboratorio laboratorio) {
		this.preencheLaboratorioPorNome(laboratorio);
		this.carregaPerfil(laboratorio.getPerfil());
		String sql = "";
		if(this.possuiPerfil(laboratorio)) {
			sql = "UPDATE perfil_laboratorio SET id_perfil = ? WHERE id_laboratorio = ?";
		}else {
			
			sql = "INSERT INTO perfil_laboratorio(id_perfil, id_laboratorio) VALUES(? , ?) ";
		}
	
		PreparedStatement ps;
		try {
			ps = this.getConexao().prepareStatement(sql);
			ps.setInt(1, laboratorio.getPerfil().getId());
			ps.setInt(2, laboratorio.getId());
			
			ps.executeUpdate();
			return true;	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		
	}
	public void carregaPerfil(Perfil perfil) {
		PreparedStatement ps;
		
		try {
			String sql = "SELECT * FROM perfil WHERE id_perfil = ? LIMIT 1";
			ps = this.getConexao().prepareStatement(sql);
			ps.setInt(1, perfil.getId());
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next()){
				perfil.setNome(resultSet.getString("nome_perfil"));
				perfil.setCota(resultSet.getInt("cota"));
				perfil.setVisitante(resultSet.getBoolean("visitante"));
				perfil.setBonus(resultSet.getBoolean("bonus"));
				perfil.setLotacao(resultSet.getInt("lotacao"));
				perfil.setTempoTurno(resultSet.getInt("tempo_turno"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void carregaPerfil(Laboratorio laboratorio) {
		PreparedStatement ps;
		
		try {
			String sql = "SELECT * FROM perfil INNER JOIN perfil_laboratorio ON perfil_laboratorio.id_perfil = perfil.id_perfil WHERE id_laboratorio = ? LIMIT 1";
			
			ps = this.getConexao().prepareStatement(sql);
			ps.setInt(1, laboratorio.getId());
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next()){
				laboratorio.getPerfil().setId(resultSet.getInt("id_perfil"));
				laboratorio.getPerfil().setNome(resultSet.getString("nome_perfil"));
				laboratorio.getPerfil().setCota(resultSet.getInt("cota"));
				laboratorio.getPerfil().setVisitante(resultSet.getBoolean("visitante"));
				laboratorio.getPerfil().setBonus(resultSet.getBoolean("bonus"));
				laboratorio.getPerfil().setLotacao(resultSet.getInt("lotacao"));
				laboratorio.getPerfil().setTempoTurno(resultSet.getInt("tempo_turno"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public boolean possuiPerfil(Laboratorio laboratorio) {
		PreparedStatement ps;
		
		try {
			String sql = "SELECT * FROM perfil_laboratorio WHERE id_laboratorio = ? LIMIT 1";
			ps = this.getConexao().prepareStatement(sql);
			ps.setInt(1, laboratorio.getId());
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next()){
				
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
	public void preencheLaboratorioPorNome(Laboratorio laboratorio) {
		PreparedStatement ps;
		
		try {
			String sql = "SELECT * FROM laboratorio WHERE nome_laboratorio = ? LIMIT 1";
			ps = this.getConexao().prepareStatement(sql);
			String nome = laboratorio.getNome().trim();
			ps.setString(1, nome);
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next()){
				laboratorio.setId(resultSet.getInt("id_laboratorio"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

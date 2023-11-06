package br.edu.unilab.unicaffe.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import br.edu.unilab.unicaffe.model.Laboratorio;
import br.edu.unilab.unicaffe.model.Maquina;
/**
 * 
 * Gerencia persistência de objetos Maquina no banco de dados. 
 * @author Jefferson Uchôa Ponte
 *
 */
public class MaquinaDAO extends DAO{

	/**
	 * Constroi objeto MaquinaDAO
	 */
	public MaquinaDAO(){
		super();
	}

	/**
	 * Constroi objeto MaquinaDAO com tipo de conexão.
	 */
	public MaquinaDAO(int tipoDeConexao){
		super(tipoDeConexao);
		
	}

	/**
	 * Constroi objeto MaquinaDAO
	 */
	public MaquinaDAO(Connection conexao){
		super(conexao);
	}
	/**
	 * Retorna a lista de máquinas do banco de dados. 
	 * @return
	 */
	public Collection<Maquina>retornaLista(){
		Collection<Maquina>lista = new HashSet<Maquina>();
		PreparedStatement ps;
		try {
			ps = this.getConexao().prepareStatement("SELECT maquina.id_maquina, maquina.nome_pc, laboratorio_maquina.id_maquina, laboratorio_maquina.id_laboratorio, laboratorio.id_laboratorio, laboratorio.nome_laboratorio FROM maquina LEFT join laboratorio_maquina ON maquina.id_maquina = laboratorio_maquina.id_maquina LEFT JOIN laboratorio ON laboratorio.id_laboratorio = laboratorio_maquina.id_laboratorio;");
			ResultSet resultSet = ps.executeQuery();
			while(resultSet.next()){
				Maquina maquina = new Maquina();
				maquina.getLaboratorio().setId(resultSet.getInt("id_laboratorio"));
				maquina.getLaboratorio().setNome(resultSet.getString("nome_laboratorio"));
				maquina.setNome(resultSet.getString("nome_pc"));
				maquina.setEnderecoMac(resultSet.getString("mac"));
				maquina.setId(resultSet.getInt("id_maquina"));
				lista.add(maquina);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lista;
	}
	/**
	 * Verifica se existe um laboratório com este nome. 
	 * @param laboratorio
	 * @return
	 */
	public boolean existeEsseLaboratorio(Laboratorio laboratorio){
		try {
			PreparedStatement ps = this.getConexao().prepareStatement("SELECT * FROM laboratorio WHERE nome_laboratorio = ? ");
			ps.setString(1, laboratorio.getNome());
			
			System.out.println("Entrou aqui, seu..."+laboratorio.getNome());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				laboratorio.setId(rs.getInt("id_laboratorio"));
				return true;
			}
			return false;					
			
		} catch (SQLException e) {
			return false;
		}		
	}
	
	/**
	 * Altera laboratório da máquina, caso ela não esteja em nenhum laboratório insere a máquina no laboratório. 
	 * @param maquina
	 * @return 
	 */
	public boolean atualizaOuInsereLaboratorio(Maquina maquina){
		try {
			PreparedStatement ps = this.getConexao().prepareStatement("SELECT * FROM laboratorio_maquina WHERE id_maquina = ? ");
			ps.setInt(1, maquina.getId());
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				
				PreparedStatement ps2 = this.getConexao().prepareStatement("UPDATE laboratorio_maquina set id_laboratorio = ? WHERE id_maquina = ?");			
				ps2.setInt(1, maquina.getLaboratorio().getId());
				ps2.setInt(2, maquina.getId());
				ps2.executeUpdate();
				return true;
			}
			System.out.println("Nao encontrei o relacionamento, logo vou tentar inserir. ID MAQUINA: "+maquina.getId()+"ID LAB: "+maquina.getLaboratorio().getId());
			PreparedStatement ps3 = this.getConexao().prepareStatement("INSERT into laboratorio_maquina (id_laboratorio, id_maquina) VALUES(?, ?)");			
			ps3.setInt(1, maquina.getLaboratorio().getId());
			ps3.setInt(2, maquina.getId());
			ps3.executeUpdate();
			
			return true;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}	
	}
	
	/**
	 * Retorna True se a máquina existir. 
	 * Verifica a partir do atributo nome da méquina. 
	 * Além de alterar propriedade da máquina endereço MAC para o valor 
	 * existente na base de dados. 
	 * 
	 * @param maquina
	 * @return
	 */
	public boolean existe(Maquina maquina, boolean alterarMac){
		try {
			PreparedStatement ps = this.getConexao().prepareStatement("SELECT * FROM maquina LEFT JOIN laboratorio_maquina ON maquina.id_maquina = laboratorio_maquina.id_maquina LEFT JOIN laboratorio ON laboratorio.id_laboratorio = laboratorio_maquina.id_laboratorio WHERE nome_pc = ? ");
			ps.setString(1, maquina.getNome());
			
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				maquina.setId(rs.getInt("id_maquina"));
				if(alterarMac) {
					maquina.setEnderecoMac(rs.getString("mac"));
				}
				maquina.getLaboratorio().setId(rs.getInt("id_laboratorio"));
				maquina.getLaboratorio().setNome(rs.getString("nome_laboratorio"));
				return true;
			}
			return false;					
			
		} catch (SQLException e) {
			return false;
		}		
		
	}
	/**
	 * Verifica se a máquina existe no banco de dados. 
	 * 
	 */
	public boolean existeSemBulir(Maquina maquina){
		try {
			PreparedStatement ps = this.getConexao().prepareStatement("SELECT * FROM maquina LEFT JOIN laboratorio_maquina ON maquina.id_maquina = laboratorio_maquina.id_maquina LEFT JOIN laboratorio ON laboratorio.id_laboratorio = laboratorio_maquina.id_laboratorio WHERE nome_pc = ? ");
			ps.setString(1, maquina.getNome());
			
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				maquina.setId(rs.getInt("id_maquina"));
				
				return true;
			}
			return false;					
			
		} catch (SQLException e) {
			return false;
		}		
		
	}
	/**
	 * Cadastra uma máquina no banco de dados. Verificando antes se ela já existe para que não seja cadastrada 
	 * mais de uma máquina com o mesmo nome. 
	 * 
	 * Retorna true se a máquina for cadastrada com sucesso. 
	 * @param maquina
	 * @return
	 */

	public boolean cadastra(Maquina maquina){
		try {
			PreparedStatement ps = this.getConexao().prepareStatement("SELECT * FROM maquina WHERE nome_pc = ?");
			ps.setString(1, maquina.getNome());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				return false;
			PreparedStatement ps2 = this.getConexao().prepareStatement("INSERT into maquina(nome_pc, mac) VALUES(?, ?)");			
			ps2.setString(1, maquina.getNome());
			ps2.setString(2, maquina.getEnderecoMac());
			ps2.executeUpdate();
			
			PreparedStatement ps3 = this.getConexao().prepareStatement("SELECT * FROM maquina WHERE nome_pc = ?");
			ps3.setString(1, ""+maquina.getNome());
			ResultSet rs2 = ps3.executeQuery();
			while(rs2.next()){
				maquina.setId(rs2.getInt("id_maquina"));
				
				
			}
			return true;					
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
	}
	/**
	 * Vai atualizar no banco o seu MAC. Filtro pelo ID. 
	 * @param maquina
	 * @return
	 */
	public boolean atualizaMac(Maquina maquina){
		PreparedStatement ps3;
		try {
			ps3 = this.getConexao().prepareStatement("UPDATE maquina set mac = ? WHERE id_maquina = ?");
			ps3.setString(1, maquina.getEnderecoMac());
			ps3.setInt(2, maquina.getId());
			ps3.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
		return true;
	}

}

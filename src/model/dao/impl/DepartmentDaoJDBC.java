package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentDaoJDBC implements DepartmentDao {

	private Connection conn;

	public DepartmentDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Department obj) {
		PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement("INSERT INTO department (Name) "
					+ "VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			
			st.setString(1, obj.getName());
			
			int AffectedRows = st.executeUpdate();
			
			if (AffectedRows > 0) {
				ResultSet rs = st.getGeneratedKeys();
				
				if (rs.next()) {
					obj.setId(rs.getInt(1));
				}
				
				DB.closeResultSet(rs);
				
			} else {
				throw new DbException("Unxpected error! No affected rows.");
			}
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
			
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Department obj) {
		PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement("UPDATE department SET Name = ? WHERE Id = ?");
			
			st.setString(1, obj.getName());
			st.setInt(2, obj.getId());
			
			int affectedRows = st.executeUpdate();
			
			if (affectedRows == 0) {
				throw new DbException("Unpexpected error! No rows affected.");
			}
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
			
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement("DELETE FROM department WHERE Id = ?");
			st.setInt(1, id);
			
			int AffectedRows = st.executeUpdate();
			
			if (AffectedRows == 0) {
				throw new DbException("None row deleted.");
			}
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
			
		} finally {
			DB.closeStatement(st);
			
		}
	}

	@Override
	public Department findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement("SELECT a.id, a.Name FROM department as a WHERE a.Id = ? ");

			st.setInt(1, id);
			rs = st.executeQuery();

			if (rs.next()) {
				return instantiateDepartment(rs);
			}
			
			return null;

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
			
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
			
		}
	}

	private Department instantiateDepartment(ResultSet rs) {
		Department dep = new Department();
			
		try {
			dep.setId(rs.getInt("id"));
			dep.setName(rs.getString("name"));
			
			return dep;
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

	@Override
	public List<Department> findAll() {
		Statement st = null;
		ResultSet rs = null;
		
		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT a.id, a.Name FROM department as a");
			
			List<Department> deps = new ArrayList<>();
			
			while(rs.next()) {
				Department dep = instantiateDepartment(rs);
				deps.add(dep);
			}
			
			return deps;
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
			
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}
}

package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller obj) {
		PreparedStatement st = null;

		try {
			
			st = conn.prepareStatement("INSERT INTO seller "
					+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+ "VALUES "
					+ "(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
	
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, Date.valueOf(obj.getBirthDate()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());

			int rowsAffected = st.executeUpdate();
			
			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				
				if (rs.next()) {
					obj.setId(rs.getInt(1));
				}
				
				DB.closeResultSet(rs);
				
			} else {
				throw new DbException("Unxpected error! No rows affected.");
			
			}
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
			
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Seller obj) {
		PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement("UPDATE seller "
					+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
					+ "WHERE Id = ?");
			
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, Date.valueOf(obj.getBirthDate()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			st.setInt(6, obj.getId());
			
			int rowsAffected = st.executeUpdate();
			
			if (rowsAffected == 0) {
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
			st = conn.prepareStatement("DELETE FROM seller WHERE Id = ?");
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
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {

			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "WHERE seller.Id = ? ");

			st.setInt(1, id);

			rs = st.executeQuery();

			if (rs.next()) {
				Department dep = instantiateDepartment(rs);
				Seller sl = instantiateSeller(rs, dep);

				return sl;
			}

			return null;

		} catch (SQLException e) {
			throw new DbException(e.getMessage());

		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}

	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
		Seller sl = new Seller();

		sl.setId(rs.getInt("Id"));
		sl.setName(rs.getString("Name"));
		sl.setEmail(rs.getString("Email"));
		sl.setBirthDate(rs.getDate("BirthDate").toLocalDate());
		sl.setBaseSalary(rs.getDouble("BaseSalary"));
		sl.setDepartment(dep);

		return sl;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		return new Department(rs.getInt("departmentId"), rs.getString("DepName"));
	}

	@Override
	public List<Seller> findAll() {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id " + "ORDER BY Name");

			List<Seller> sellers = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {
				Department dep = map.get(rs.getInt("departmentId"));

				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(dep.getId(), dep);
				}

				Seller sl = instantiateSeller(rs, dep);
				sellers.add(sl);
			}

			return sellers;

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "WHERE DepartmentId = ? " + "ORDER BY Name");

			st.setInt(1, department.getId());
			rs = st.executeQuery();

			List<Seller> sellers = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {
				Department dep = map.get(rs.getInt("departmentId"));

				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("departmentId"), dep);
				}

				sellers.add(instantiateSeller(rs, dep));
			}

			return sellers;

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}
}

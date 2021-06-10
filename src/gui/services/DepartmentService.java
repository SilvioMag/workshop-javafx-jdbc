package gui.services;

import java.util.ArrayList;
import java.util.List;

import model.entities.Department;

public class DepartmentService {

	public List<Department> findAll() {
		List<Department> lista = new ArrayList<>();
		
		lista.add(new Department(1, "Books"));
		lista.add(new Department(2, "Electronics"));
		lista.add(new Department(2, "Computers"));
		
		return lista;
		
	}
}

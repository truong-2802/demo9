package com.example.demo9.Service;

import com.example.demo9.Entity.Department;
import com.example.demo9.Entity.Employee;
import com.example.demo9.Repository.DepartmentRepository;
import com.example.demo9.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Employee> getAllEmployees(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(pageable).getContent();
    }

    public Employee addEmployee(Employee employee) {
        Department department = employee.getDepartment();
        if (department == null) {
            throw new RuntimeException("Department cannot be null");
        }
        department = departmentRepository.findById(department.getId())
                .orElseThrow(() -> new RuntimeException("Department does not exist"));

        validateEmployee(employee, department);
        employeeRepository.save(employee);
        department.setNumberOfEmployees(department.getNumberOfEmployees() + 1);
        departmentRepository.save(department);
        return employee;
    }

    public Employee updateEmployee(Long id, Employee i) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Department department = employee.getDepartment();
        if (department == null) {
            throw new RuntimeException("Employee's department cannot be null");
        }
        validateEmployee(i, department);


        employee.setName(i.getName());
        employee.setDob(i.getDob());
        employee.setAddress(i.getAddress());
        employee.setBasicSalary(i.getBasicSalary());
        employee.setNetSalary(i.getNetSalary());
        employee.setInsurance(i.getInsurance());
        employee.setPosition(i.getPosition());

        employeeRepository.save(employee);
        return employee;
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employeeRepository.delete(employee);

        Department department = employee.getDepartment();
        if (department != null) {
            department.setNumberOfEmployees(department.getNumberOfEmployees() - 1);
            departmentRepository.save(department);
        }
    }

    private void validateEmployee(Employee employee, Department department) {
        if ("manager".equals(employee.getPosition()) && department.getManager() != null) {
            throw new RuntimeException("The department already has a department head");
        }
        long deputyManagersCount = department.getEmployees().stream()
                .filter(emp -> "deputy".equals(emp.getPosition()))
                .count();
        if ("deputy".equals(employee.getPosition()) && deputyManagersCount >= 2) {
            throw new RuntimeException("The department already has 2 deputy managers");
        }
    }
}

package com.example.employeeexample.data


import android.app.Application
import androidx.lifecycle.LiveData

class EmployeeShowRepository(context: Application) {
    private val employeeShowDao: EmployeeShowDao = EmployeeDatabase.getDatabase(
        context
    ).employeeShowDao()

    fun getEmployee(id: Long): LiveData<Employee> {
        return employeeShowDao.getEmployee(id)
    }
}
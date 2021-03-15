package com.example.employeeexample.ui

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.employeeexample.data.Employee
import com.example.employeeexample.data.EmployeeShowRepository

class EmployeeShowViewModel(application: Application):AndroidViewModel(application) {

    private val repo: EmployeeShowRepository = EmployeeShowRepository(application)

    private val _employeeId=MutableLiveData<Long>(0)

    val employee:LiveData<Employee> = Transformations.switchMap(_employeeId){id->
        repo.getEmployee(id)
    }

    fun setEmployeeId(id:Long){
        if(_employeeId.value!=id){
            _employeeId.value=id
        }
    }

}
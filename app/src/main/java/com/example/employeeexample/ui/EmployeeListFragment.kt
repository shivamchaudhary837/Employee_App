package com.example.employeeexample.ui

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.employeeexample.BuildConfig
import com.example.employeeexample.R
import com.example.employeeexample.data.Employee
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_employee_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

const val READ_FILE_REQUEST = 1
//const val CREATE_FILE_REQUEST = 2
const val LATEST_EMPLOYEE_NAME_KEY = "LATEST_EMPLOYEE_NAME_KEY"
class EmployeeListFragment : Fragment() {

    private lateinit var viewModel: EmployeeListViewModel

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)
            .get(EmployeeListViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupNavigationDrawer()

        with(employee_list){
            layoutManager = LinearLayoutManager(activity)
            adapter = EmployeeAdapter {show,id->
                if(show){
                    findNavController().navigate(EmployeeListFragmentDirections.actionEmployeeListFragmentToEmployeeShowFragment(id))
                }else{
                    findNavController().navigate(EmployeeListFragmentDirections.actionEmployeeListFragmentToEmployeeDetailFragment(id))
                }
            }
        }

        add_employee.setOnClickListener{
            findNavController().navigate(
                EmployeeListFragmentDirections.actionEmployeeListFragmentToEmployeeDetailFragment(
                    0
                )
            )
        }

        viewModel.employees.observe(viewLifecycleOwner, Observer {
            (employee_list.adapter as EmployeeAdapter).submitList(it)
            if(it.isNotEmpty()){
                no_employee_record.visibility=View.INVISIBLE
            }else{
                no_employee_record.visibility=View.VISIBLE
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                READ_FILE_REQUEST -> {
                    GlobalScope.launch{
                        val resolver = requireActivity().contentResolver
                        resolver.openInputStream(data!!.data!!).use { stream ->
                            stream?.let{
                                withContext(Dispatchers.IO) {
                                    parseCSVFile(stream)
                                }
                            }
                        }
                    }
                }

            }
        }
    }
    private fun setupNavigationDrawer(){
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.navigation_view)

      //  NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)
        NavigationUI.setupWithNavController(toolbar,navController,
        AppBarConfiguration.Builder(R.id.navigation,R.id.employeeListFragment).setDrawerLayout(drawerLayout).build()
        )
        navigationView.setupWithNavController(navController)

        navigationView.setNavigationItemSelectedListener {
            drawerLayout.closeDrawers()

            when (it.itemId) {
                R.id.add_new ->  findNavController().navigate(
                    EmployeeListFragmentDirections.actionEmployeeListFragmentToEmployeeDetailFragment(
                        0
                    )
                )
                R.id.contact ->  findNavController().navigate(
                    EmployeeListFragmentDirections.actionEmployeeListFragmentToContactFragment()
                )
                R.id.about ->  findNavController().navigate(
                    EmployeeListFragmentDirections.actionEmployeeListFragmentToAboutFragment()
                )
                R.id.sign_out->{
                    val auth=FirebaseAuth.getInstance()
                    auth.signOut()
                    auth.addAuthStateListener{
                        if(auth.currentUser==null){
                            //listener is called multiple times so check if we are in correct fragment
                            val currId=findNavController().currentDestination!!.id
                            if(currId==R.id.employeeListFragment){
                                findNavController().navigate(
                                        EmployeeListFragmentDirections.actionEmployeeListFragmentToLoginFragment()
                                )
                            }
                        }
                        true
                    }
                }
                R.id.menu_export_data -> {
                    GlobalScope.launch {
                        exportEmployees()
                    }
                }
                R.id.menu_import_data -> {
                    Intent(Intent.ACTION_GET_CONTENT).also { readFileIntent ->
                        readFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        readFileIntent.type = "text/*"
                        readFileIntent.resolveActivity(requireActivity().packageManager)?.also {
                            startActivityForResult(readFileIntent,
                                READ_FILE_REQUEST
                            )
                        }
                    }
                }
                R.id.menu_alarm->{
                    val alarmMgr = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                        PendingIntent.getBroadcast(context, 0, intent, 0)
                    }

                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                            //API >= 23
                            alarmMgr.setExactAndAllowWhileIdle(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() + 60 * 1000,
                                alarmIntent)
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                            //API >= 19
                            alarmMgr.setExact(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() + 60 * 1000,
                                alarmIntent)
                        }
                        else -> {
                            alarmMgr.set(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() + 60 * 1000,
                                alarmIntent)
                        }
                    }
                    requireActivity().showToast(getString(R.string.alarm_set_message), Toast.LENGTH_SHORT)
                }
                R.id.menu_latest_employee_name -> {
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                    val name = sharedPref?.getString(LATEST_EMPLOYEE_NAME_KEY, "")
                    if(!name.isNullOrEmpty()){
                        Snackbar.make(main_container,
                            getString(R.string.latest_employee, name), Snackbar.LENGTH_LONG)
                            .setAction("CLOSE"){

                            }.show()

                    } else if(name != null){
                        requireActivity().showToast(getString(R.string.no_employee_added))
                    }
                }
                R.id.menu_chat->{
                    findNavController().navigate(
                        EmployeeListFragmentDirections.actionEmployeeListFragmentToChatFragment()
                    )
                }
            }

            true
        }
    }


    private suspend fun parseCSVFile(stream: InputStream){
        val employees = mutableListOf<Employee>()

        BufferedReader(InputStreamReader(stream)).forEachLine {
            val tokens = it.split(",")
            employees.add(Employee(id = 0, name = tokens[0], role = tokens[1].toInt(),
                    age = tokens[2].toInt(), gender = tokens[3].toInt(), photo = "", responsibility = "",
                    experience = "", education = "", phone = 0, email = "", address = ""))
        }

        if(employees.isNotEmpty()){
            viewModel.insertEmployees(employees)
        }
    }


    private suspend fun exportEmployees(){
        var csvFile: File? = null
        withContext(Dispatchers.IO) {
            csvFile = createFile(requireActivity(), "Documents", "csv")
            csvFile?.printWriter()?.use { out ->
                val employees = viewModel.getEmployeeList()
                if(employees.isNotEmpty()){
                    employees.forEach{
                        out.println(it.name + "," + it.role + "," + it.age + "," + it.gender)
                    }
                }
            }
        }
        withContext(Dispatchers.Main){
            csvFile?.let{
                val uri = FileProvider.getUriForFile(
                        requireActivity(), BuildConfig.APPLICATION_ID + ".fileprovider",
                        it)
                launchFile(uri, "csv")
            }
        }
    }

    private fun launchFile(uri: Uri, ext: String){
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, mimeType)
        if(intent.resolveActivity(requireActivity().packageManager) != null){
            startActivity(intent)
        } else{
            Toast.makeText(requireActivity(), getString(R.string.no_app_csv), Toast.LENGTH_SHORT).show()
        }
    }
}

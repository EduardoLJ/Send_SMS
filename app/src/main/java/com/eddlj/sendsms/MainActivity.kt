package com.eddlj.sendsms

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eddlj.sendsms.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactAdapter: ArrayAdapter<String>

    // Lista de permisos requeridos
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE
    )

    private val PERMISSION_REQUEST_CODE = 200

    // Variable para almacenar el número detectado
    var phoneNum: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Detectar si hay un número de llamada entrante
        phoneNum = intent.getStringExtra("number")

        // Solicitar todos los permisos al iniciar la aplicación
        requestAllPermissions()

        // Configurar el adaptador de contactos
        contactAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        binding.contactsListView.adapter = contactAdapter

        // Manejo de selección de contacto
        binding.contactsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedContactNumber = getContactNumber(contactAdapter.getItem(position).toString())
            showContactDetails(contactAdapter.getItem(position).toString())
            binding.editTextPhone.setText(selectedContactNumber)
        }

        // Botón de enviar SMS
        binding.btnSent.setOnClickListener {
            if (hasPermission(Manifest.permission.SEND_SMS)) {
                sendSMS()
            } else {
                requestAllPermissions()
            }
        }

        // Botón para mostrar contactos
        binding.showContactsButton.setOnClickListener {
            if (hasPermission(Manifest.permission.READ_CONTACTS)) {
                displayContacts()
            } else {
                requestAllPermissions()
            }
        }
    }

    // Verificar si un permiso está concedido
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Solicitar todos los permisos si no han sido concedidos
    private fun requestAllPermissions() {
        val permissionsToRequest = REQUIRED_PERMISSIONS.filter { !hasPermission(it) }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE)
        }
    }

    // Manejar la respuesta de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }

            if (deniedPermissions.isEmpty()) {
                Log.d("Permissions", "Todos los permisos concedidos")
            } else {
                Log.e("Permissions", "Permisos denegados: $deniedPermissions")
                Toast.makeText(this, "Debe conceder todos los permisos para un funcionamiento correcto", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Mostrar contactos en el ListView
    private fun displayContacts() {
        val contacts = getContacts()
        contactAdapter.clear()
        contactAdapter.addAll(contacts)
    }

    // Obtener contactos del dispositivo
    private fun getContacts(): List<String> {
        val contactsList = ArrayList<String>()
        val contentResolver: ContentResolver = contentResolver
        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name: String? = it.getString(nameIndex)
                val number: String? = it.getString(numberIndex)

                if (!name.isNullOrBlank() && !number.isNullOrBlank()) {
                    val contactInfo = "$name : $number"
                    contactsList.add(contactInfo)
                }
            }
        }
        return contactsList
    }

    // Extraer número de un contacto
    private fun getContactNumber(contact: String): String {
        val parts = contact.split(" : ").toTypedArray()
        return parts[1]
    }

    // Mostrar detalles del contacto en la pantalla
    private fun showContactDetails(contact: String) {
        binding.contactDetailsTextView.text = contact
        binding.contactDetailsTextView.visibility = View.VISIBLE
    }

    // Enviar SMS con validaciones
    private fun sendSMS() {
        val phone: String = binding.editTextPhone.text.toString()
        val message: String = binding.editTextSMS.text.toString()

        Log.d("SendSMS", "Número: $phone, Mensaje: $message") // Registro en LogCat

        if (phone.isNotEmpty() && message.isNotEmpty()) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phone, null, message, null, null)
                Toast.makeText(this, "SMS enviado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SendSMS", "Error al enviar SMS", e)
                Toast.makeText(this, "Error al enviar SMS", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ingrese número y mensaje", Toast.LENGTH_SHORT).show()
        }
    }
}

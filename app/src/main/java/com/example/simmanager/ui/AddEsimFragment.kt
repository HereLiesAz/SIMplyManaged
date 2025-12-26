package com.example.simmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.simmanager.R
import com.example.simmanager.data.EsimProfile
import com.example.simmanager.data.EsimRepository

class AddEsimFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_esim, container, false)

        val nameInput = view.findViewById<EditText>(R.id.input_esim_name)
        val carrierInput = view.findViewById<EditText>(R.id.input_esim_carrier)
        val codeInput = view.findViewById<EditText>(R.id.input_esim_code)
        val saveButton = view.findViewById<Button>(R.id.btn_save_esim)

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val carrier = carrierInput.text.toString()
            val code = codeInput.text.toString()

            if (name.isEmpty() || code.isEmpty()) {
                 Toast.makeText(context, "Name and Activation Code are required", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }

            val newProfile = EsimProfile(name, code, carrier, "")
            EsimRepository.addProfile(requireContext(), newProfile)
            parentFragmentManager.popBackStack()
        }

        return view
    }
}

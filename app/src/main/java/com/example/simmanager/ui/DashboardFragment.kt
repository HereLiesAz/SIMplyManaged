package com.example.simmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.simmanager.R
import com.example.simmanager.logic.SimController

class DashboardFragment : Fragment() {

    private lateinit var simController: SimController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        simController = SimController(requireContext())

        val statusText = view.findViewById<TextView>(R.id.text_status)
        val sims = simController.getAvailableSims()

        if (sims.isEmpty()) {
            statusText.text = "No SIMs detected or permission missing."
        } else {
            statusText.text = "Detected ${sims.size} SIM(s).\n" + sims.joinToString("\n") {
                "${it.displayName} (${it.carrierName})"
            }
        }

        return view
    }
}

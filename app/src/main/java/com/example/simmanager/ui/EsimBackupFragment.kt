package com.example.simmanager.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.simmanager.R
import com.example.simmanager.data.EsimRepository

class EsimBackupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_esim_backup, container, false)
        val listContainer = view.findViewById<LinearLayout>(R.id.esim_list_container)
        val btnAdd = view.findViewById<Button>(R.id.btn_add_esim)

        btnAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddEsimFragment())
                .addToBackStack(null)
                .commit()
        }

        EsimRepository.loadProfiles(requireContext())
        val profiles = EsimRepository.getAllProfiles()

        if (profiles.isEmpty()) {
            val emptyView = TextView(context)
            emptyView.text = "No saved eSIM profiles."
            listContainer.addView(emptyView)
        } else {
            for (profile in profiles) {
                val itemView = inflater.inflate(R.layout.item_esim_profile, listContainer, false)
                itemView.findViewById<TextView>(R.id.text_name).text = profile.name
                itemView.findViewById<TextView>(R.id.text_carrier).text = profile.carrier

                itemView.findViewById<Button>(R.id.btn_copy_code).setOnClickListener {
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Activation Code", profile.activationCode)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                }

                // Placeholder for Install Action (will be refined in next step)
                itemView.findViewById<Button>(R.id.btn_install).setOnClickListener {
                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                         // Try to install programmatically if we have permission/system privs
                         val mgr = requireContext().getSystemService(android.telephony.euicc.EuiccManager::class.java)
                         if (mgr != null && mgr.isEnabled) {
                             val sub = android.telephony.euicc.DownloadableSubscription.forActivationCode(profile.activationCode)
                             // This requires PendingIntent for callback
                             val intent = android.content.Intent(context, MainActivity::class.java)
                             val callbackIntent = android.app.PendingIntent.getActivity(
                                 context, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                             )
                             try {
                                 mgr.downloadSubscription(sub, true, callbackIntent)
                                 Toast.makeText(context, "Attempting install...", Toast.LENGTH_SHORT).show()
                             } catch (e: Exception) {
                                 // Likely SecurityException
                                 Toast.makeText(context, "Cannot install directly: ${e.message}. Copy code instead.", Toast.LENGTH_LONG).show()
                             }
                         } else {
                             Toast.makeText(context, "eSIM Manager not available.", Toast.LENGTH_SHORT).show()
                         }
                     } else {
                         Toast.makeText(context, "Install not supported on this Android version.", Toast.LENGTH_SHORT).show()
                     }
                }

                listContainer.addView(itemView)
            }
        }

        return view
    }
}

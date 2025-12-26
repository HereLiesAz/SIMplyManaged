package com.example.simmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.simmanager.R
import com.example.simmanager.data.Rule
import com.example.simmanager.data.RuleCondition
import com.example.simmanager.data.RuleType
import com.example.simmanager.data.UsageMetric
import com.example.simmanager.data.Action
import com.example.simmanager.data.RuleRepository
import java.time.LocalTime

class AddRuleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_rule, container, false)

        val nameInput = view.findViewById<EditText>(R.id.input_rule_name)
        val typeSpinner = view.findViewById<Spinner>(R.id.spinner_rule_type)
        val paramInput = view.findViewById<EditText>(R.id.input_extra_param)
        val saveButton = view.findViewById<Button>(R.id.btn_save_rule)

        val simSpinner = view.findViewById<Spinner>(R.id.spinner_sim_select)
        val metricSpinner = view.findViewById<Spinner>(R.id.spinner_usage_metric)
        val actionSimSpinner = view.findViewById<Spinner>(R.id.spinner_action_sim)
        val hotspotCheckbox = view.findViewById<android.widget.CheckBox>(R.id.checkbox_hotspot)
        val cbData = view.findViewById<android.widget.CheckBox>(R.id.cb_action_data)
        val cbVoice = view.findViewById<android.widget.CheckBox>(R.id.cb_action_voice)
        val cbSms = view.findViewById<android.widget.CheckBox>(R.id.cb_action_sms)

        // Setup Types Spinner
        val types = RuleType.values()
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        // Setup Metric Spinner
        val metrics = UsageMetric.values()
        val metricAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, metrics)
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        metricSpinner.adapter = metricAdapter

        // Setup SIM Spinner
        val simController = com.example.simmanager.logic.SimController(requireContext())
        val sims = simController.getAvailableSims()
        val simNames = sims.map { "${it.displayName} (ID: ${it.subscriptionId})" }
        val simAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, simNames)
        simAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        simSpinner.adapter = simAdapter
        actionSimSpinner.adapter = simAdapter // Reuse adapter for Action SIM

        // Toggle visibility based on type
        typeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val type = types[position]
                if (type == RuleType.USAGE_BASED) {
                    metricSpinner.visibility = View.VISIBLE
                    simSpinner.visibility = View.VISIBLE
                } else {
                    metricSpinner.visibility = View.GONE
                    simSpinner.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        // Toggle Action Spinner vs Hotspot
        hotspotCheckbox.setOnCheckedChangeListener { _, isChecked ->
             if (isChecked) {
                 actionSimSpinner.visibility = View.GONE
                 cbData.visibility = View.GONE
                 cbVoice.visibility = View.GONE
                 cbSms.visibility = View.GONE
             } else {
                 actionSimSpinner.visibility = View.VISIBLE
                 cbData.visibility = View.VISIBLE
                 cbVoice.visibility = View.VISIBLE
                 cbSms.visibility = View.VISIBLE
             }
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val selectedType = typeSpinner.selectedItem as RuleType
            val param = paramInput.text.toString()

            if (name.isEmpty() || param.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // Determine monitored SIM ID
                val selectedSimIndex = simSpinner.selectedItemPosition
                val monitoredSubId = if (sims.isNotEmpty() && selectedSimIndex >= 0) {
                    sims[selectedSimIndex].subscriptionId
                } else {
                    -1
                }

                // Determine Action SIM ID
                val selectedActionSimIndex = actionSimSpinner.selectedItemPosition
                val actionSubId = if (sims.isNotEmpty() && selectedActionSimIndex >= 0) {
                    sims[selectedActionSimIndex].subscriptionId
                } else {
                    1 // Default fallback
                }

                val condition = when (selectedType) {
                    RuleType.TIME_BASED -> {
                        val parts = param.split(":")
                        val time = LocalTime.of(parts[0].toInt(), parts[1].toInt())
                        RuleCondition.TimeCondition(
                            startTime = time,
                            endTime = time.plusHours(8),
                            days = java.time.DayOfWeek.values().toSet()
                        )
                    }
                    RuleType.USAGE_BASED -> {
                        val metric = metricSpinner.selectedItem as UsageMetric
                        RuleCondition.UsageCondition(
                            metric = metric,
                            threshold = param.toLong(),
                            subscriptionId = monitoredSubId
                        )
                    }
                    RuleType.WIFI_BASED -> {
                        RuleCondition.WifiCondition(
                            ssid = param,
                            isConnected = true
                        )
                    }
                }

                val action = if (hotspotCheckbox.isChecked) {
                    Action.EnableHotspot(true)
                } else {
                    // Check which checkboxes are selected
                    var voiceSub: Int? = null
                    var smsSub: Int? = null
                    var dataSub: Int? = null

                    if (cbData.isChecked) dataSub = actionSubId
                    if (cbVoice.isChecked) voiceSub = actionSubId
                    if (cbSms.isChecked) smsSub = actionSubId

                    // Construct composite action
                    Action.SwitchProfile(voiceSub, smsSub, dataSub)
                }

                val newRule = Rule(
                    name = name,
                    type = selectedType,
                    condition = condition,
                    action = action
                )
                RuleRepository.addRule(requireContext(), newRule)
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(context, "Invalid Input: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}

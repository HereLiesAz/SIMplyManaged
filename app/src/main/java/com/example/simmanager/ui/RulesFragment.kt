package com.example.simmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.simmanager.R

class RulesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rules, container, false)
        val rulesContainer = view.findViewById<android.widget.LinearLayout>(R.id.rules_container)

        // Ensure rules are loaded
        com.example.simmanager.data.RuleRepository.loadRules(requireContext())
        val rules = com.example.simmanager.data.RuleRepository.getAllRules()

        if (rules.isEmpty()) {
             val emptyView = android.widget.TextView(context)
             emptyView.text = "No rules configured."
             rulesContainer.addView(emptyView)
        } else {
            for (rule in rules) {
                val ruleView = android.widget.TextView(context)
                ruleView.text = "Rule: ${rule.name} (${rule.type})"
                ruleView.textSize = 16f
                ruleView.setPadding(0, 8, 0, 8)
                rulesContainer.addView(ruleView)
            }
        }

        view.findViewById<android.widget.Button>(R.id.btn_add_rule).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddRuleFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}

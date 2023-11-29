package com.golfzon.login.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.login.R
import com.golfzon.login.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppbarVisibility()
        setAppbarAction()
    }

    private fun setAppbarAction() {
        binding.btnLoginAppbarBack.setOnDebounceClickListener {
            findNavController().navigateUp()
        }
    }
    private fun setAppbarVisibility() {
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            binding.layoutLoginAppbar.visibility = when (destination.id) {
                R.id.LoginFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

}
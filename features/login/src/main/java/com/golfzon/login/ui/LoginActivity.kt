package com.golfzon.login.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.navigation.DeeplinkHandler
import com.golfzon.login.R
import com.golfzon.login.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel by viewModels<LoginViewModel>()

    @Inject
    lateinit var deeplinkHandler: DeeplinkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setRegisterMenuVisibility()
        setAppbarAction()
        setNavigation()
    }

    private fun setAppbarAction() {
        binding.btnLoginAppbarBack.setOnDebounceClickListener {
            findNavController().navigateUp()
        }
    }

    fun setNextClickListener(directions: NavDirections? = null) {
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.ImageCropFragment -> {
                    binding.btnNext.setOnDebounceClickListener {
                        findNavController().popBackStack()
                    }
                }

                R.id.UserImageSetFragment -> {
                    binding.btnNext.setOnDebounceClickListener {
                        loginViewModel.requestSetUserInfo()
                    }
                }

                else -> {
                    binding.btnNext.setOnDebounceClickListener {
                        directions?.let {
                            findNavController().navigate(directions)
                        }
                    }
                }
            }
        }
    }

    private fun setRegisterMenuVisibility() {
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            setLoginLayout(destination.id)
            setImageCropLayout(destination.id)
        }
    }

    private fun setLoginLayout(destinationId: Int) {
        with(binding) {
            layoutLoginAppbar.visibility = when (destinationId) {
                R.id.LoginFragment -> View.GONE
                else -> View.VISIBLE
            }
            btnNext.visibility = when (destinationId) {
                R.id.LoginFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun setImageCropLayout(destinationId: Int) {
        with(binding) {
            btnLoginAppbarBack.visibility = when (destinationId) {
                R.id.ImageCropFragment -> View.INVISIBLE
                else -> View.VISIBLE
            }
            tvLoginAppbarTitle.text = when (destinationId) {
                R.id.ImageCropFragment -> "Crop"
                else -> "Join"
            }
            btnNext.text = when (destinationId) {
                R.id.ImageCropFragment -> getString(com.golfzon.core_ui.R.string.complete)
                R.id.UserImageSetFragment -> getString(com.golfzon.core_ui.R.string.complete)
                else -> getString(com.golfzon.core_ui.R.string.next)
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    private fun setNavigation() {
        intent?.let {
            handleIntent(it)
        }
    }

    private fun handleIntent(intent: Intent) {
        intent.data?.toString()?.let {
            deeplinkHandler.process(it)
        }
    }

    fun navigateToTeam() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/team"))
        startActivity(intent)
        this@LoginActivity.finish()
    }
}
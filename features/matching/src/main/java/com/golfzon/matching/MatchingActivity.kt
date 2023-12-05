package com.golfzon.matching

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.core_ui.navigation.DeeplinkHandler
import com.golfzon.matching.databinding.ActivityMatchingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MatchingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatchingBinding
    @Inject
    lateinit var deeplinkHandler: DeeplinkHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setNavigation()
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
    }
}
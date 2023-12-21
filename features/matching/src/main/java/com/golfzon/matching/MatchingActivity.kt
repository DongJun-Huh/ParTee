package com.golfzon.matching

import android.content.Intent
import android.net.Uri
import android.os.Build
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

    override fun onPause() {
        super.onPause()
        removeActivityChangeAnimation()
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    private fun removeActivityChangeAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
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
        finishAffinity()
    }

    fun navigateToGroup(destination: String = "", groupUId: String = "") {
        val intent = if (groupUId.isNotEmpty())
            Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/group${destination}/${groupUId}"))
        else
            Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/group${destination}"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finishAffinity()
    }

    fun navigateToRecruit() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/recruit"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finishAffinity()
    }
}
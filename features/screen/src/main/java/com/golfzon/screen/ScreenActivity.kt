package com.golfzon.screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.golfzon.screen.databinding.ActivityScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
    fun navigateToGroup() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("partee://multi.module.app/group"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }
}
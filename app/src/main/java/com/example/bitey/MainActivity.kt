package com.example.bitey

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bitey.databinding.ActivityMainBinding
import com.example.bitey.ui.fragment.MealListFragment
import androidx.fragment.app.Fragment
import com.example.bitey.ui.fragment.SavedMealsFragment
import androidx.core.view.isVisible
import android.content.Intent
import com.example.bitey.ui.HomeActivity

/*hosts XML Fragment, ViewModel owns the screen state*/
class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding

    /*fid fragment and reutilize it instead of creating a new one every time*/
    companion object {
        private const val TAG_RECIPES = "tag_recipes"
        private const val TAG_SAVED = "tag_saved"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupBottomNavigation()
        setupHomeButton()

        /*hides bottom nav while a detail Fragment is on the back stack*/
        supportFragmentManager.addOnBackStackChangedListener {
            updateBottomNavigationVisibility()
        }

        updateBottomNavigationVisibility()


        // FragmentManager restores existing Fragments after rotation.
        if (savedInstanceState == null) {
            showFragment(TAG_RECIPES)
        }
    }

    /*configures two main navigation tabs*/
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recipes -> showFragment(TAG_RECIPES)
                R.id.nav_saved -> showFragment(TAG_SAVED)
            }
            true
        }

        binding.bottomNavigation.setOnItemReselectedListener { }
    }

    /*keeps bottom tabs visible only on main screens*/
    private fun updateBottomNavigationVisibility() {
        binding.bottomNavigation.isVisible =
            supportFragmentManager.backStackEntryCount == 0
    }


    /*shows one fragment and hides the other*/
    private fun showFragment(tag: String) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()

        manager.fragments.forEach { fragment ->
            transaction.hide(fragment)
        }

        val existing = manager.findFragmentByTag(tag)

        if (existing != null) {
            transaction.show(existing)
        } else {
            transaction.add(
                R.id.fragmentContainer,
                createFragment(tag),
                tag
            )
        }

        transaction.commit()
    }

    /*creates a fragment only if it doesnt exist yet*/
    private fun createFragment(tag: String): Fragment =
        when (tag) {
            TAG_SAVED -> SavedMealsFragment()
            else -> MealListFragment()
        }


    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }
    }

    /*header shortcut: goes back to the Home screen*/
    private fun setupHomeButton() {
        binding.biteyHeader.btnHome.setOnClickListener {
            // CLEAR_TOP reuses the Home that is already below in the stack (closing this screen)
            // instead of stacking a second copy of it.
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }
}

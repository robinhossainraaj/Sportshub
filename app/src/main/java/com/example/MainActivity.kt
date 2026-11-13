package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.SportsRepository
import com.example.ui.Sports24App
import com.example.ui.theme.Sports24Theme
import com.example.ui.viewmodel.EventsViewModel
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    
    // Core application configurations and local references
    private lateinit var database: AppDatabase
    private lateinit var repository: SportsRepository
    
    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var playerViewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Support full screen bleed configurations for transparent notch bars
        enableEdgeToEdge()
        
        // Single-instance database bootstrapper
        database = AppDatabase.getDatabase(this)
        repository = SportsRepository(database.favouriteDao())
        
        // Construct and cache view models directly to bypass heavy DI frameworks
        eventsViewModel = EventsViewModel(repository)
        categoriesViewModel = CategoriesViewModel(repository)
        playerViewModel = PlayerViewModel(repository)

        setContent {
            Sports24Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.ui.theme.BgMain
                ) {
                    Sports24App(
                        activity = this,
                        eventsViewModel = eventsViewModel,
                        categoriesViewModel = categoriesViewModel,
                        playerViewModel = playerViewModel
                    )
                }
            }
        }
    }
}

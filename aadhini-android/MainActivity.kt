package ai.aadhini.app

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import ai.aadhini.app.ui.navigation.AadhiniUiController

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(AadhiniRootView(this, AadhiniUiController()))
    }
}

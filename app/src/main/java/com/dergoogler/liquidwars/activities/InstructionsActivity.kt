package com.dergoogler.liquidwars.activities

import android.os.Bundle
import com.dergoogler.liquidwars.R

class InstructionsActivity : LiquidCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructions)
        setAdsBanner(R.id.instructions_ads_banner)
    }
}

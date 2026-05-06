package com.jamesfirstok.aegis

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.jamesfirstok.aegis.security.SovereigntyVerifier

class AegisApplication : Application() {
    lateinit var sovereigntyVerifier: SovereigntyVerifier
    lateinit var database: AegisDatabase
    
    override fun onCreate() {
        super.onCreate()
        sovereigntyVerifier = SovereigntyVerifier(this)
        database = Room.databaseBuilder(
            this,
            AegisDatabase::class.java,
            "aegis_tactical_db"
        ).build()
    }
}

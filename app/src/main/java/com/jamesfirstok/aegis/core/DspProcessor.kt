package com.jamesfirstok.aegis.radar

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.log10
import kotlin.math.sqrt

class DspProcessor {
    private val fft = FloatFFT_1D(2048)
    private val hanningWindow = FloatArray(2048)
    
    init

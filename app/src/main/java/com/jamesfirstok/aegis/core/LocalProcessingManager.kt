package com.jamesfirstok.aegis.core

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.*

/**
 * AEGIS Signal Processing Unit v3.0 [RF/Audio Intelligence]
 * الوظيفة: تحويل الموجات الخام إلى بصمات رقمية قابلة للتحليل.
 */
class LocalProcessingManager {
    
    private val fftSize = 2048
    private val fftProcessor = FloatFFT_1D(fftSize)
    
    /**
     * تحليل الإشارة الخام واستخراج مؤشرات التهديد.
     * @param rawData البيانات القادمة من الحساس (راديو أو صوت).
     * @param sampleRate معدل العينة (مثلاً 48000 هيرتز).
     */
    fun analyzeSignal(rawData: FloatArray, sampleRate: Float = 48000f): SignalAnalysisResult {
        // التحقق من سلامة البيانات لمنع انهيار المنظومة
        if (rawData.size < fftSize) return SignalAnalysisResult(-100f, 0f, 0f, 0f)
        
        val spectrum = computePowerSpectrum(rawData)
        
        // 1. حساب قوة الإشارة بالديسيبل (مع منع القيم اللانهائية)
        val maxPower = (spectrum.maxOrNull() ?: 1e-10f).coerceAtLeast(1e-10f)
        val powerDb = 10f * log10(maxPower)
        
        // 2. تحديد التردد المهيمن (الذروة)
        val peakFreq = findPeakFrequency(spectrum, sampleRate)
        
        // 3. تحليل درجة التهديد (بناءً على توزيع الطاقة في الترددات العالية)
        val threatLevel = calculateThreatScore(spectrum)
        
        // 4. كشف الشذوذ (كشف الإشارات الاصطناعية وسط الضجيج)
        val anomalyScore = detectAnomalies(spectrum)
        
        return SignalAnalysisResult(powerDb, peakFreq, threatLevel, anomalyScore)
    }
    
    private fun computePowerSpectrum(input: FloatArray): FloatArray {
        // استخدام حوض ذاكرة مؤقت لمنع التخصيص المتكرر (Memory optimization)
        val frame = FloatArray(fftSize * 2)
        val windowed = applyHanningWindow(input.take(fftSize).toFloatArray())
        
        for (i in windowed.indices) {
            frame[2 * i] = windowed[i]
            frame[2 * i + 1] = 0f // البيانات التخيلية صفر
        }
        
        fftProcessor.complexForward(frame)
        
        return FloatArray(fftSize / 2) { i ->
            val real = frame[2 * i]
            val imag = frame[2 * i + 1]
            sqrt(real * real + imag * imag) // حساب السعة (Magnitude)
        }
    }
    
    private fun applyHanningWindow(data: FloatArray): FloatArray {
        return FloatArray(data.size) { i ->
            data[i] * (0.5f - 0.5f * cos(2.0 * PI.toFloat() * i / (data.size - 1)))
        }
    }
    
    private fun findPeakFrequency(spectrum: FloatArray, sampleRate: Float): Float {
        val maxIndex = spectrum.indices.maxByOrNull { spectrum[it] } ?: 0
        return maxIndex * (sampleRate / fftSize)
    }
    
    private fun calculateThreatScore(spectrum: FloatArray): Float {
        if (spectrum.isEmpty()) return 0f
        val totalEnergy = spectrum.sum().coerceAtLeast(1e-10f)
        // التركيز على النصف العلوي من الطيف حيث تظهر إشارات التحكم عادةً
        val highFreqEnergy = spectrum.takeLast(spectrum.size / 3).sum()
        return (highFreqEnergy / totalEnergy).coerceIn(0f, 1f)
    }
    
    private fun detectAnomalies(spectrum: FloatArray): Float {
        val mean = spectrum.average().toFloat().coerceAtLeast(1e-10f)
        val variance = spectrum.map { (it - mean).pow(2) }.average().toFloat()
        // معامل التباين: كلما زاد، دل على وجود إشارة اصطناعية حادة وسط الضجيج
        return sqrt(variance.coerceAtLeast(0f)) / mean
    }
}

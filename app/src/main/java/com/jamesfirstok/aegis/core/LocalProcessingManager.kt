package com.jamesfirstok.aegis.core

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.*

data class SignalAnalysisResult(
    val powerDb: Float,
    val peakFreq: Float,
    val threatLevel: Float,
    val anomalyScore: Float
)

class LocalProcessingManager {
    
    private val fftSize = 2048
    private val fftProcessor = FloatFFT_1D(fftSize)
    
    // [تعديل تكتيكي لحماية الذاكرة]: استخدام أحواض ذاكرة ثابتة (Memory Pools) 
    // لمنع التخصيص المتكرر وحماية التطبيق من التعليق المرئي على شاشة الـ HUD
    private val framePool = FloatArray(fftSize * 2)
    private val windowedPool = FloatArray(fftSize)
    private val spectrumPool = FloatArray(fftSize / 2)
    
    /**
     * تحليل الإشارة واستخراج مؤشرات التهديد (الوضع الاحتياطي للهاتف)
     */
    fun analyzeSignal(rawData: FloatArray, sampleRate: Float = 48000f): SignalAnalysisResult {
        if (rawData.size < fftSize) return SignalAnalysisResult(-100f, 0f, 0f, 0f)
        
        // حساب الطيف الطاقي باستخدام أحواض الذاكرة الآمنة
        computePowerSpectrumNonAllocating(rawData)
        
        // 1. حساب قوة الإشارة بالديسيبل مع الحماية من القيم اللانهائية
        val maxPower = (spectrumPool.maxOrNull() ?: 1e-10f).coerceAtLeast(1e-10f)
        val powerDb = 10f * log10(maxPower)
        
        // 2. تحديد التردد المهيمن (الذروة الترددية)
        val peakFreq = findPeakFrequency(sampleRate)
        
        // 3. تحليل درجة التهديد بناءً على توزيع الطاقة في النطاقات الحيوية
        val threatLevel = calculateThreatScore()
        
        // 4. كشف الشذوذ الطيفي الحاد لتمييز البث الاصطناعي للمسيرات
        val anomalyScore = detectAnomalies()
        
        return SignalAnalysisResult(powerDb, peakFreq, threatLevel, anomalyScore)
    }
    
    /**
     * معالجة طيفية خالية من تخصيص الذاكرة العشوائي (Zero-Allocation Spectrum)
     * تدعم قنوات الإدخال ثنائية الطور عند تفعيلها تكتيكياً
     */
    private fun computePowerSpectrumNonAllocating(input: FloatArray) {
        // تطبيق نافذة هانينج داخل حوض الذاكرة الثابت
        applyHanningWindowInline(input)
        
        // بناء مصفوفة الإدخال لـ JTransforms
        // المواقع الزوجية تمثل الجزء الحقيقي، والمواقع الفردية تمثل الجزء التخيلي (مستعدة لاستقبال قنوات I/Q)
        for (i in 0 until fftSize) {
            framePool[2 * i] = windowedPool[i]
            framePool[2 * i + 1] = 0f // تظل صفراً في وضع الهاتف وتستقبل بيانات Q من الـ SDR في الوضع الكامل
        }
        
        // تشغيل معالج الـ FFT السريع
        fftProcessor.complexForward(framePool)
        
        // حساب السعة الفيزيائية وحقنها مباشرة في حوض الطيف الثابت
        for (i in 0 until (fftSize / 2)) {
            val real = framePool[2 * i]
            val imag = framePool[2 * i + 1]
            spectrumPool[i] = sqrt(real * real + imag * imag)
        }
    }
    
    private fun applyHanningWindowInline(input: FloatArray) {
        val piFactor = 2.0 * PI / (fftSize - 1)
        for (i in 0 until fftSize) {
            windowedPool[i] = input[i] * (0.5f - 0.5f * cos(piFactor * i).toFloat())
        }
    }
    
    private fun findPeakFrequency(sampleRate: Float): Float {
        var maxIndex = 0
        var maxVal = spectrumPool[0]
        for (i in 1 until spectrumPool.size) {
            if (spectrumPool[i] > maxVal) {
                maxVal = spectrumPool[i]
                maxIndex = i
            }
        }
        return maxIndex * (sampleRate / fftSize)
    }
    
    private fun calculateThreatScore(): Float {
        var totalEnergy = 0f
        for (i in spectrumPool.indices) {
            totalEnergy += spectrumPool[i]
        }
        totalEnergy = totalEnergy.coerceAtLeast(1e-10f)
        
        // التركيز على الثلث العلوي من النطاق لالتقاط نبضات التحكم النبضي المشبوهة
        val sliceStart = (spectrumPool.size * 2) / 3
        var highFreqEnergy = 0f
        for (i in sliceStart until spectrumPool.size) {
            highFreqEnergy += spectrumPool[i]
        }
        
        return (highFreqEnergy / totalEnergy).coerceIn(0f, 1f)
    }
    
    private fun detectAnomalies(): Float {
        var sum = 0f
        for (i in spectrumPool.indices) {
            sum += spectrumPool[i]
        }
        val mean = (sum / spectrumPool.size).coerceAtLeast(1e-10f)
        
        var varianceSum = 0f
        for (i in spectrumPool.indices) {
            varianceSum += (spectrumPool[i] - mean).pow(2)
        }
        val variance = varianceSum / spectrumPool.size
        
        return sqrt(variance.coerceAtLeast(0f)) / mean
    }
}

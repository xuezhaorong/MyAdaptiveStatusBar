package com.example.project

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.palette.graphics.Palette


import com.example.project.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!::binding.isInitialized){
            binding = ActivityMainBinding.inflate(layoutInflater)
        }

        val displayMetrics = resources.displayMetrics
        val colorCount = 5
        val left = 0
        val top = 0
        val right = displayMetrics.widthPixels
        val bottom = getStatusBarHeight()

        // 获取背景颜色
        val typedValue = TypedValue()
        // 获取当前主题的 windowBackground 属性
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)


        // 使用 TypedArray 获取 windowBackground 的颜色值
        val backgroundResourceId = typedValue.resourceId
        val typedArray = obtainStyledAttributes(backgroundResourceId, intArrayOf(android.R.attr.colorBackground))
        val backgroundColor = typedArray.getColor(0, 0)
        typedArray.recycle()

        // 如果颜色值为 0，则表示获取失败，可以使用默认颜色
        val finalBackgroundColor = if (backgroundColor != 0) backgroundColor else Color.WHITE

        // finalBackgroundColor 包含了当前主题的 windowBackground 颜色
        val bitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)

        // 使用 Canvas 将颜色值填充到 Bitmap 上
        val canvas = Canvas(bitmap)
        canvas.drawColor(finalBackgroundColor)

        // 保存取得的纯色图片
        saveFile(bitmap)


        if (finalBackgroundColor == Color.WHITE){
            Toast.makeText(this,"白天模式",Toast.LENGTH_LONG).show()
            setLightStatusBar()

        }else{
            // 解析状态栏上的颜色
            Palette
                .from(bitmap)
                .maximumColorCount(colorCount)
                .setRegion(left, top, right, bottom)
                .generate {
                    it?.let { palette ->
                        var mostPopularSwatch: Palette.Swatch? = null
                        for (swatch in palette.swatches) {
                            if (mostPopularSwatch == null
                                || swatch.population > mostPopularSwatch.population) {
                                mostPopularSwatch = swatch
                            }
                        }
                        mostPopularSwatch?.let { swatch ->
                            val luminance = ColorUtils.calculateLuminance(swatch.rgb)
                            if (luminance < 0.5) {
                            Toast.makeText(this,"黑暗模式",Toast.LENGTH_LONG).show()
                                setDarkStatusBar()
                            } else {
                            Toast.makeText(this,"白天模式",Toast.LENGTH_LONG).show()
                                setLightStatusBar()
                            }
                        }
                    }
                }
        }
        setContentView(binding.root)
    }

    // 保存图片
    private fun saveFile(bitmap: Bitmap){
        val fileName = "my_image.png"

        // 获取应用的内部存储目录
        val directory: File = filesDir

        // 创建一个文件对象，表示要保存的文件
        val file = File(directory, fileName)

        // 创建一个输出流，将 Bitmap 写入文件
        try {
            FileOutputStream(file).use { out ->
                // 将 Bitmap 压缩为 PNG 格式，并将其写入输出流
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun setLightStatusBar() { // 状态栏亮色 图标为黑色
        WindowInsetsControllerCompat(window,window.decorView).isAppearanceLightStatusBars = true
    }


    private fun setDarkStatusBar() { // 状态栏黑色 图标白色
        WindowInsetsControllerCompat(window,window.decorView).isAppearanceLightStatusBars = false
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

}
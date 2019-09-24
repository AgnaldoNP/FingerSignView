package pereira.agnaldo.fingersign

import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            clear.id -> {
                fingerSign.drawClear()
            }
            save.id -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val permission = ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )

                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        fingerSign.save(File(Environment.getExternalStorageDirectory(),"fingerSignTest.png").absolutePath)
                    } else {
                        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                    }
                } else {
                    fingerSign.save(File(Environment.getExternalStorageDirectory(),"fingerSignTest.png").absolutePath)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clear.setOnClickListener(this)
        save.setOnClickListener(this)
    }
}

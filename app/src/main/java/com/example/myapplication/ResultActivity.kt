package com.example.myapplication
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var saveButton: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()

            if (name.isNotEmpty() && age.isNotEmpty()) {
                val user = hashMapOf(
                    "name" to name,
                    "age" to age.toInt()
                )

                db.collection("users")
                    .add(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to Save!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter both name and age", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
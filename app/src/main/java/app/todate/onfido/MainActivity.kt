package app.todate.onfido

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.todate.onfido.databinding.ActivityMainBinding
import com.onfido.android.sdk.capture.ExitCode
import com.onfido.workflow.OnfidoWorkflow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Built with https://github.com/onfido/onfido-android-sdk/blob/15.4.1/ONFIDO_STUDIO.md
 *  as reference
 */
class MainActivity : AppCompatActivity() {
    private val reqCode = 0x05

    private lateinit var binding: ActivityMainBinding
    private lateinit var onfidoWorkflow: OnfidoWorkflow

    private val onfidoHandler: OnfidoHandler = OnfidoHandler()

    private val firstName: String get() = binding.firstName.text.toString()
    private val lastName: String get() = binding.lastName.text.toString()
    private val workflowId: String get() = binding.workflowId.text.toString()
    private val apiToken: String get() = binding.apiToken.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //TODO - uncomment here to set details
//        setDebugValues(
//            firstName = "",
//            lastName = "",
//            workflowId = "",
//            apiToken = "",
//        )

        onfidoWorkflow = OnfidoWorkflow.create(this)

        binding.fab.setOnClickListener {
            startActivityForResult(
                onfidoWorkflow.createIntent(
                    onfidoHandler.config!!
                ), reqCode
            )
        }

        binding.initButton.setOnClickListener {
            binding.initButton.apply {
                text = "initialising..."
                isEnabled = false
            }
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    onfidoHandler.init(
                        firstName = firstName,
                        lastName = lastName,
                        workflowId = workflowId,
                        authHeader = "Token token=$apiToken",
                    )
                    binding.fab.isEnabled = true
                    binding.initButton.apply {
                        text = "initialised"
                        isEnabled = true
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onfidoWorkflow.handleActivityResult(resultCode, data, object : OnfidoWorkflow.ResultListener {
            override fun onUserCompleted() {
                Timber.d("user completed")
            }

            override fun onUserExited(exitCode: ExitCode) {
                Timber.d("user exited: $exitCode")
            }

            override fun onException(exception: OnfidoWorkflow.WorkflowException) {
                Timber.d("error: $exception")
            }
        })
    }

    private fun setDebugValues(
        firstName: String,
        lastName: String,
        workflowId: String,
        apiToken: String,
    ) {
        binding.firstName.setText(firstName, TextView.BufferType.EDITABLE)
        binding.lastName.setText(lastName, TextView.BufferType.EDITABLE)
        binding.workflowId.setText(workflowId, TextView.BufferType.EDITABLE)
        binding.apiToken.setText(apiToken, TextView.BufferType.EDITABLE)
    }
}
